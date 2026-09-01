"use strict";

const { test } = require("node:test");
const assert = require("node:assert/strict");

const { esTokenInvalido, dividirEnLotes, MAX_TOKENS_POR_LOTE } = require("../lib/tokens");

test("esTokenInvalido reconoce tokens no registrados", () => {
  assert.equal(esTokenInvalido("messaging/unregistered"), true);
  assert.equal(esTokenInvalido("messaging/registration-token-not-registered"), true);
});

test("esTokenInvalido reconoce argumento inválido / no encontrado / sender mismatch", () => {
  assert.equal(esTokenInvalido("messaging/invalid-argument"), true);
  assert.equal(esTokenInvalido("messaging/not-found"), true);
  assert.equal(esTokenInvalido("messaging/sender-id-mismatch"), true);
});

test("esTokenInvalido NO marca errores de infraestructura como token inválido", () => {
  assert.equal(esTokenInvalido("messaging/quota-exceeded"), false);
  assert.equal(esTokenInvalido("messaging/internal-error"), false);
  assert.equal(esTokenInvalido("messaging/server-unavailable"), false);
});

test("esTokenInvalido tolera null / undefined / vacío", () => {
  assert.equal(esTokenInvalido(null), false);
  assert.equal(esTokenInvalido(undefined), false);
  assert.equal(esTokenInvalido(""), false);
});

test("dividirEnLotes parte por el máximo de FCM (500)", () => {
  const items = Array.from({ length: 1000 }, (_, i) => i);
  const lotes = dividirEnLotes(items, MAX_TOKENS_POR_LOTE);
  assert.equal(lotes.length, 2);
  assert.equal(lotes[0].length, 500);
  assert.equal(lotes[1].length, 500);
});

test("dividirEnLotes maneja el resto", () => {
  const items = Array.from({ length: 501 }, (_, i) => i);
  const lotes = dividirEnLotes(items);
  assert.equal(lotes.length, 2);
  assert.equal(lotes[0].length, 500);
  assert.equal(lotes[1].length, 1);
});

test("dividirEnLotes con lista vacía devuelve una lista vacía", () => {
  assert.deepEqual(dividirEnLotes([]), []);
});
