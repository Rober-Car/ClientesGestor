"use strict";

const { test } = require("node:test");
const assert = require("node:assert/strict");

const {
  idNotificacionBaja,
  idNotificacionMorosidad,
  idNotificacionRecordatorioMorosidad,
  idBuzon,
  periodoDe24h,
} = require("../lib/ids");

test("idNotificacionBaja incluye cliente y fecha de baja", () => {
  assert.equal(idNotificacionBaja(3, 1700000000000), "baja_confirmada_3_1700000000000");
});

test("idNotificacionBaja distingue dos bajas con distinta fecha", () => {
  assert.notEqual(idNotificacionBaja(3, 1000), idNotificacionBaja(3, 2000));
});

test("idNotificacionMorosidad es determinista por cliente y fechaFinActual", () => {
  assert.equal(
    idNotificacionMorosidad(7, 1710000000000),
    "morosidad_7_1710000000000"
  );
  assert.equal(
    idNotificacionMorosidad(7, 1710000000000),
    idNotificacionMorosidad(7, 1710000000000)
  );
});

test("idNotificacionRecordatorioMorosidad es determinista por periodo", () => {
  assert.equal(
    idNotificacionRecordatorioMorosidad(5, 20260),
    "morosidad_recordatorio_5_20260"
  );
});

test("idBuzon sigue el patrón cliente_notificacion", () => {
  assert.equal(idBuzon(2, "n_1_abc"), "2_n_1_abc");
});

test("periodoDe24h agrupa instantes de la misma ventana", () => {
  const base = 1700000000000;
  assert.equal(periodoDe24h(base), periodoDe24h(base + 3600000));
  // Un día después cae en otra ventana.
  assert.notEqual(periodoDe24h(base), periodoDe24h(base + 86400000));
});
