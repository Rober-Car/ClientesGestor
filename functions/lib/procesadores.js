"use strict";

const { logger } = require("firebase-functions/v2");
const { FieldValue, Timestamp } = require("firebase-admin/firestore");
const { db, leerConfiguracion, timestampAms } = require("./firestore");
const {
  resolverDestinatariosDesdeDoc,
  obtenerVinculados,
  crearBuzones,
  esperarBuzones,
} = require("./destinatarios");
const { enviarFCMaClientes } = require("./envio");
const {
  idNotificacionBaja,
  idNotificacionMorosidad,
  idNotificacionRecordatorioMorosidad,
  periodoDe24h,
} = require("./ids");

/**
 * procesadores.js
 * ---------------
 * Procesadores de las notificaciones (Fase E). Todos usan el mismo patrón:
 *   1) datos de Firestore (nunca Room ni UI);
 *   2) buzones con set() determinista (idempotente);
 *   3) CLAIM atómico en Transaction (solo la ejecución ganadora envía);
 *   4) envío FCM por cliente (máx. 500 tokens) + limpieza de tokens inválidos;
 *   5) escritura de diagnóstico (opcional, no cambia el estado).
 */

const HORAS_RECORDATORIO = 24;
const MILIS_HORA = 3600000;

/**
 * reclamarTransicion
 * ------------------
 * CLAIM atómico de una notificación: transita de `estadoEsperado` a ENVIADA
 * (con fechaEnvio) solo si el documento sigue en `estadoEsperado`. Devuelve:
 * 'reclamada' (esta ejecución es la ganadora), 'ya-procesada' o 'no-existe'.
 * Es la barrera de idempotencia del envío FCM.
 */
async function reclamarTransicion(notificacionId, estadoEsperado) {
  const ref = db().collection("notificaciones").doc(notificacionId);
  return db().runTransaction(async (t) => {
    const snap = await t.get(ref);
    if (!snap.exists) return "no-existe";
    if (snap.data().estado !== estadoEsperado) return "ya-procesada";
    t.update(ref, {
      estado: "ENVIADA",
      fechaEnvio: FieldValue.serverTimestamp(),
    });
    return "reclamada";
  });
}

/**
 * escribirDiagnostico
 * -------------------
 * Campos opcionales de diagnóstico en notificaciones/{id}. No cambia el
 * estado; si falla, solo se registra un aviso.
 */
async function escribirDiagnostico(notificacionId, res) {
  try {
    await db().collection("notificaciones").doc(notificacionId).update({
      dispositivosEnviados: res.enviados,
      dispositivosFallidos: res.fallidos,
      dispositivosEliminados: res.eliminados,
    });
  } catch (e) {
    logger.warn("No se pudo escribir el diagnóstico de envío", { notificacionId, error: e.message });
  }
}

/**
 * crearYEnviarAutomatica
 * ----------------------
 * Crea una notificación automática (MOROSIDAD / BAJA_CONFIRMADA /
 * recordatorio) con ID determinista, su buzón, el claim y el envío FCM.
 * Reutilizada por los triggers de clientes y el recordatorio.
 */
async function crearYEnviarAutomatica({
  notificacionId,
  negocioId,
  clienteId,
  titulo,
  mensaje,
  tipo,
  origen,
}) {
  const ahora = Timestamp.now();
  await db().collection("notificaciones").doc(notificacionId).set({
    negocioId,
    titulo,
    mensaje,
    tipo,
    origen,
    modoDestino: "INDIVIDUAL",
    idsClientes: [clienteId],
    clienteId,
    fechaCreacion: ahora,
    programada: false,
    estado: "PENDIENTE",
  });

  const vinculados = await obtenerVinculados([clienteId]);
  if (vinculados.length > 0) {
    await crearBuzones({
      negocioId,
      notificacionId,
      titulo,
      mensaje,
      tipo,
      origen,
      vinculados,
    });
  }

  const claim = await reclamarTransicion(notificacionId, "PENDIENTE");
  if (claim !== "reclamada") {
    logger.info("Notificación automática ya procesada", { notificacionId, claim });
    return;
  }

  const res = await enviarFCMaClientes({
    negocioId,
    notificacionId,
    titulo,
    mensaje,
    tipo,
    origen,
    clienteIds: [clienteId],
  });
  await escribirDiagnostico(notificacionId, res);
  logger.info("Notificación automática procesada", { notificacionId, tipo, ...res });
}

/**
 * procesarNotificacionInmediata
 * -----------------------------
 * Trigger: onDocumentCreated("notificaciones/{notificacionId}").
 * Solo actúa sobre inmediatas MANUAL (PENDIENTE, programada=false, origen
 * MANUAL). Los buzones ya los creó la app; se esperan brevemente y se envían.
 */
async function procesarNotificacionInmediata(event) {
  const notificacionId = event.params.notificacionId;
  const datos = event.data && event.data.data();
  if (!datos) return;
  if (datos.estado !== "PENDIENTE" || datos.programada !== false || datos.origen !== "MANUAL") {
    return;
  }
  const negocioId = datos.negocioId;
  if (!negocioId) return;

  logger.info("Procesando notificación inmediata", { notificacionId, negocioId });

  const buzones = await esperarBuzones(notificacionId);
  const clienteIds =
    buzones.length > 0
      ? buzones
      : resolverDestinatariosDesdeDoc(datos);

  const claim = await reclamarTransicion(notificacionId, "PENDIENTE");
  if (claim !== "reclamada") {
    logger.info("Notificación inmediata ya procesada", { notificacionId, claim });
    return;
  }

  const res = await enviarFCMaClientes({
    negocioId,
    notificacionId,
    titulo: datos.titulo,
    mensaje: datos.mensaje,
    tipo: datos.tipo,
    origen: datos.origen,
    clienteIds,
  });
  await escribirDiagnostico(notificacionId, res);
  logger.info("Notificación inmediata procesada", { notificacionId, ...res });
}

/**
 * procesarProgramadas
 * -------------------
 * Trigger: onSchedule("every 2 minutes"). Barrido de notificaciones
 * PROGRAMADA con fechaProgramada <= ahora. Requiere el índice compuesto
 * notificaciones(estado ASC, fechaProgramada ASC).
 */
async function procesarProgramadas() {
  const ahora = new Date();
  const snapshot = await db()
    .collection("notificaciones")
    .where("estado", "==", "PROGRAMADA")
    .where("fechaProgramada", "<=", ahora)
    .get();

  logger.info("Barrido de programadas", { encontradas: snapshot.size });

  for (const doc of snapshot.docs) {
    const notificacionId = doc.id;
    const datos = doc.data();
    const negocioId = datos.negocioId;
    if (!negocioId) continue;
    try {
      const clienteIds = resolverDestinatariosDesdeDoc(datos);
      const vinculados = await obtenerVinculados(clienteIds);

      if (vinculados.length > 0) {
        await crearBuzones({
          negocioId,
          notificacionId,
          titulo: datos.titulo,
          mensaje: datos.mensaje,
          tipo: datos.tipo,
          origen: datos.origen,
          vinculados,
        });
      }

      const claim = await reclamarTransicion(notificacionId, "PROGRAMADA");
      if (claim !== "reclamada") continue;

      const res = await enviarFCMaClientes({
        negocioId,
        notificacionId,
        titulo: datos.titulo,
        mensaje: datos.mensaje,
        tipo: datos.tipo,
        origen: datos.origen,
        clienteIds: vinculados.map((v) => v.idCliente),
      });
      await escribirDiagnostico(notificacionId, res);
      logger.info("Programada procesada", { notificacionId, ...res });
    } catch (e) {
      logger.error("Error procesando programada", { notificacionId, error: e.message });
    }
  }
}

/**
 * procesarEntradaMorosidad
 * ------------------------
 * Trigger: onDocumentUpdated("clientes/{clienteId}"). Detecta la entrada en
 * MOROSO usando únicamente datos de Firestore: cliente ACTIVO con
 * fechaFinActual < ahora. Se ignora si el período no cambió.
 */
async function procesarEntradaMorosidad(event) {
  const clienteId = event.params.clienteId;
  const before = event.data.before.data();
  const after = event.data.after.data();
  if (!before || !after) return;
  if (after.estado !== "ACTIVO") return;

  const fechaFinMillis = timestampAms(after.fechaFinActual);
  if (fechaFinMillis === null || fechaFinMillis >= Date.now()) return;
  const antesMillis = timestampAms(before.fechaFinActual);
  if (antesMillis === fechaFinMillis) return;

  const negocioId = after.negocioId;
  if (!negocioId) return;

  const config = await leerConfiguracion(negocioId);
  if (!config || config.morosidad?.activa !== true) return;

  const notificacionId = idNotificacionMorosidad(clienteId, fechaFinMillis);
  await crearYEnviarAutomatica({
    notificacionId,
    negocioId,
    clienteId: Number(clienteId),
    titulo: "Alerta de morosidad",
    mensaje: "Se ha detectado un periodo de pago vencido en tu cuenta.",
    tipo: "MOROSIDAD",
    origen: "PRECONFIGURADA",
  });
}

/**
 * procesarRecordatorioMorosidad
 * -----------------------------
 * Trigger: onSchedule("every 1 hour"). Clientes ACTIVO con fechaFinActual <
 * ahora, cuya configuración tenga morosidad.activa y recordatorioHoras == 24.
 * Usa ultimoRecordatorioMorosidad como claim atómico para enviar como mucho
 * uno cada 24 horas.
 */
async function procesarRecordatorioMorosidad() {
  const ahora = Date.now();
  const snapshot = await db().collection("clientes").where("estado", "==", "ACTIVO").get();

  const candidatos = snapshot.docs
    .map((d) => ({ id: Number(d.id), data: d.data() }))
    .filter((c) => {
      const f = timestampAms(c.data.fechaFinActual);
      return f !== null && f < ahora;
    });

  logger.info("Barrido de recordatorios de morosidad", { candidatos: candidatos.length });

  for (const c of candidatos) {
    const negocioId = c.data.negocioId;
    if (!negocioId) continue;
    const config = await leerConfiguracion(negocioId);
    if (!config || config.morosidad?.activa !== true || config.morosidad.recordatorioHoras !== 24) {
      continue;
    }

    const ref = db().collection("clientes").doc(String(c.id));
    const ganador = await db().runTransaction(async (t) => {
      const snap = await t.get(ref);
      if (!snap.exists) return false;
      const ultimo = timestampAms(snap.data().ultimoRecordatorioMorosidad);
      if (ultimo !== null && ahora - ultimo < HORAS_RECORDATORIO * MILIS_HORA) return false;
      t.update(ref, { ultimoRecordatorioMorosidad: Timestamp.now() });
      return true;
    });
    if (!ganador) continue;

    const notificacionId = idNotificacionRecordatorioMorosidad(c.id, periodoDe24h(ahora));
    await crearYEnviarAutomatica({
      notificacionId,
      negocioId,
      clienteId: c.id,
      titulo: "Recordatorio de morosidad",
      mensaje: "Sigue pendiente tu pago. Recuerda regularizar tu situación.",
      tipo: "MOROSIDAD",
      origen: "PRECONFIGURADA",
    });
  }
}

/**
 * procesarBajaConfirmada
 * ----------------------
 * Trigger: onDocumentUpdated("clientes/{clienteId}"). Detecta la transición
 * a BAJA (estado anterior != BAJA y estado nuevo == BAJA) y, si la
 * configuración lo permite, crea la notificación de baja confirmada.
 */
async function procesarBajaConfirmada(event) {
  const clienteId = event.params.clienteId;
  const before = event.data.before.data();
  const after = event.data.after.data();
  if (!before || !after) return;
  if (before.estado === "BAJA" || after.estado !== "BAJA") return;

  const negocioId = after.negocioId;
  if (!negocioId) return;

  const config = await leerConfiguracion(negocioId);
  if (!config || config.bajaConfirmada?.activa !== true) return;

  const fechaBajaMillis = timestampAms(after.fechaBaja);
  const base = fechaBajaMillis !== null ? fechaBajaMillis : Date.now();
  const notificacionId = idNotificacionBaja(clienteId, base);

  await crearYEnviarAutomatica({
    notificacionId,
    negocioId,
    clienteId: Number(clienteId),
    titulo: "Baja confirmada",
    mensaje: "Tu baja en el gimnasio ha sido confirmada.",
    tipo: "BAJA_CONFIRMADA",
    origen: "PRECONFIGURADA",
  });
}

module.exports = {
  procesarNotificacionInmediata,
  procesarProgramadas,
  procesarRecordatorioMorosidad,
  procesarEntradaMorosidad,
  procesarBajaConfirmada,
};
