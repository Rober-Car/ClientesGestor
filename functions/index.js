/**
 * Cloud Functions de GestorPro.
 *
 * FASE A (infraestructura): esqueleto del backend de notificaciones.
 * La aplicación inicializa firebase-admin aquí; los triggers reales se
 * implementarán en la Fase E:
 *   - onWrite(notificaciones_por_destinatario) -> envío de push FCM.
 *   - scheduler -> notificaciones PROGRAMADAS vencidas.
 *   - scheduler -> recordatorio de morosidad cada 24h.
 *   - onUpdate(clientes/{id}) -> "Baja confirmada" cuando estado -> BAJA.
 */
const { initializeApp } = require("firebase-admin/app");

initializeApp();
