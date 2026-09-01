"use strict";

/**
 * tokens.js
 * ---------
 * Helpers puros para el manejo de tokens FCM: clasificación de tokens
 * inválidos y división en lotes de máximo 500. Sin dependencias: testeable
 * sin firebase.
 */

const MAX_TOKENS_POR_LOTE = 500;

/**
 * Subcadenas de los códigos de error de FCM que indican que un token ya no es
 * válido y conviene eliminarlo del documento del dispositivo. Se comparan por
 * coincidencia (incluye) para tolerar variantes del SDK.
 */
const SUBSTRINGS_TOKEN_INVALIDO = [
  "unregistered",
  "invalid-argument",
  "not-found",
  "sender-id-mismatch",
  "invalid-registration-token",
  "registration-token-not-registered",
];

/**
 * esTokenInvalido
 * ---------------
 * Devuelve true si el código de error de FCM corresponde a un token que ya no
 * es válido. Un token inválido no debe impedir el envío al resto.
 */
function esTokenInvalido(codigo) {
  if (!codigo || typeof codigo !== "string") return false;
  const c = codigo.toLowerCase();
  return SUBSTRINGS_TOKEN_INVALIDO.some((s) => c.includes(s));
}

/**
 * dividirEnLotes
 * --------------
 * Divide una lista en lotes de como máximo `tamano` elementos (por defecto el
 * límite de FCM: 500 tokens por sendEachForMulticast).
 */
function dividirEnLotes(items, tamano = MAX_TOKENS_POR_LOTE) {
  const lotes = [];
  for (let i = 0; i < items.length; i += tamano) {
    lotes.push(items.slice(i, i + tamano));
  }
  return lotes;
}

module.exports = { MAX_TOKENS_POR_LOTE, esTokenInvalido, dividirEnLotes };
