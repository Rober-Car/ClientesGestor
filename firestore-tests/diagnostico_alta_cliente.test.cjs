/**
 * diagnostico_alta_cliente.test.cjs
 * ---------------------------------
 * TEST TEMPORAL DE DIAGNOSTICO (FASE 3.1) — NO forma parte del suite oficial.
 *
 * Objetivo: aislar cual de las tres escrituras del alta ADMIN provoca
 * PERMISSION_DENIED, reproduciendo EXACTAMENTE el payload real que envia
 * ClienteRemotoRepository.crearClienteRemoto() (mapaDeAlta con Timestamps).
 *
 * Casos:
 *   1. Escritura 1 sola (clientes/22)                 -> debe FALLAR (necesita indice en batch)
 *   2. Escritura 2 sola (indices_clientes)            -> debe FALLAR (necesita cliente en batch)
 *   3. Escritura 3 sola (clientes_privados)           -> debe PASAR
 *   4. Batch completo 1+2+3                           -> debe PASAR (replica el alta real)
 *   5. Batch completo con INDICE YA EXISTENTE         -> hipotesis de fallo real
 *   6. Batch completo con CLIENTES YA EXISTENTE       -> hipotesis de fallo real
 *   7. Batch completo con PRIVADOS YA EXISTENTE       -> hipotesis de fallo real
 */

const fs = require("node:fs");
const path = require("node:path");
const assert = require("node:assert/strict");
const { after, before, test } = require("node:test");

const {
    assertFails,
    assertSucceeds,
    initializeTestEnvironment
} = require("@firebase/rules-unit-testing");
const {
    Timestamp,
    doc,
    getDoc,
    setDoc,
    writeBatch
} = require("firebase/firestore");

const PROJECT_ID = "gestorpro-rules-test";
const ADMIN_UID = "admin-diagnostico-alta";
const NEGOCIO = "negocio-diagnostico";
const ID_CLIENTE = 22;
const DNI = "32323232Y";

// Payload EXACTO de mapaDeAlta() de ClienteRemotoRepository:
// fechas como Timestamp, email/foto reales, serviciosContratados lista vacia.
function mapaDeAltaReal(idCliente, negocioId, dni) {
    return {
        idCliente,
        negocioId,
        firebaseUid: null,
        nombre: "Cliente",
        apellidos: "De Prueba",
        dni,
        telefono: "600000000",
        email: "cliente@test.com",
        foto: "/data/user/0/com.roberto.gestorpro/files/foto.jpg",
        fechaNacimiento: Timestamp.fromMillis(946684800000),
        fechaRegistro: Timestamp.fromMillis(1600000000000),
        fechaAlta: Timestamp.fromMillis(1600000000000),
        fechaBaja: null,
        estado: "ACTIVO",
        serviciosContratados: [],
        fechaInicioActual: null,
        fechaFinActual: null
    };
}

// Payload del indice tal como envia crearClienteRemoto().
function indiceReal(negocioId, dni, idCliente) {
    return {
        negocioId,
        dni,
        clienteId: idCliente
    };
}

// Payload de clientes_privados tal como envia crearClienteRemoto().
function privadosReal(negocioId) {
    return { negocioId, observaciones: null };
}

function indiceDocId(negocioId, dni) {
    return `${negocioId}_${dni}`;
}

async function configurarAdmin(testEnvironment) {
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const db = context.firestore();
        await setDoc(doc(db, "usuarios", ADMIN_UID), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId: NEGOCIO
        });
        await setDoc(doc(db, "negocios", NEGOCIO), {
            adminUid: ADMIN_UID,
            nombre: "Negocio diagnostico"
        });
    });
}

let testEnvironment;

before(async () => {
    testEnvironment = await initializeTestEnvironment({
        projectId: PROJECT_ID,
        firestore: {
            rules: fs.readFileSync(
                path.resolve(__dirname, "firestore.rules.generated"),
                "utf8"
            )
        }
    });
    await configurarAdmin(testEnvironment);
});

after(async () => {
    await testEnvironment.cleanup();
});

async function fichaYaExiste(db, coleccion, id) {
    return (await getDoc(doc(db, coleccion, id))).exists();
}

test("1) SOLO clientes/22 -> debe FALLAR (el indice debe nacer en el mismo batch)", async () => {
    const db = testEnvironment.authenticatedContext(ADMIN_UID).firestore();
    const b = writeBatch(db);
    b.set(
        doc(db, "clientes", String(ID_CLIENTE)),
        mapaDeAltaReal(ID_CLIENTE, NEGOCIO, DNI)
    );
    await assertFails(b.commit());
});

test("2) SOLO indices_clientes/{negocio}_{dni} -> debe FALLAR (la ficha debe nacer en el mismo batch)", async () => {
    const db = testEnvironment.authenticatedContext(ADMIN_UID).firestore();
    const b = writeBatch(db);
    b.set(
        doc(db, "indices_clientes", indiceDocId(NEGOCIO, DNI)),
        indiceReal(NEGOCIO, DNI, ID_CLIENTE)
    );
    await assertFails(b.commit());
});

test("3) SOLO clientes_privados/22 -> debe PASAR", async () => {
    const db = testEnvironment.authenticatedContext(ADMIN_UID).firestore();
    const b = writeBatch(db);
    b.set(
        doc(db, "clientes_privados", String(ID_CLIENTE)),
        privadosReal(NEGOCIO)
    );
    await assertSucceeds(b.commit());
});

test("4) BATCH COMPLETO 1+2+3 (alta real) -> debe PASAR", async () => {
    const db = testEnvironment.authenticatedContext(ADMIN_UID).firestore();
    const b = writeBatch(db);
    b.set(
        doc(db, "clientes", String(ID_CLIENTE)),
        mapaDeAltaReal(ID_CLIENTE, NEGOCIO, DNI)
    );
    b.set(
        doc(db, "indices_clientes", indiceDocId(NEGOCIO, DNI)),
        indiceReal(NEGOCIO, DNI, ID_CLIENTE)
    );
    b.set(
        doc(db, "clientes_privados", String(ID_CLIENTE)),
        privadosReal(NEGOCIO)
    );
    await assertSucceeds(b.commit());
});

test("5) BATCH COMPLETO con INDICE YA EXISTENTE -> hipotesis de fallo real", async () => {
    // Preparar indice previo (como si un alta anterior hubiera dejado el indice).
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const db = context.firestore();
        await setDoc(
            doc(db, "indices_clientes", indiceDocId(NEGOCIO, DNI)),
            { negocioId: NEGOCIO, dni: DNI, clienteId: ID_CLIENTE }
        );
    });

    const db = testEnvironment.authenticatedContext(ADMIN_UID).firestore();
    const b = writeBatch(db);
    b.set(
        doc(db, "clientes", String(ID_CLIENTE)),
        mapaDeAltaReal(ID_CLIENTE, NEGOCIO, DNI)
    );
    b.set(
        doc(db, "indices_clientes", indiceDocId(NEGOCIO, DNI)),
        indiceReal(NEGOCIO, DNI, ID_CLIENTE)
    );
    b.set(
        doc(db, "clientes_privados", String(ID_CLIENTE)),
        privadosReal(NEGOCIO)
    );
    // El indice ya existe -> batch.set() = UPDATE -> allow update:false -> DENEGADO.
    await assertFails(b.commit());
});

test("6) BATCH COMPLETO con CLIENTES YA EXISTENTE -> hipotesis de fallo real", async () => {
    // Preparar ficha previa (cliente ya existente en Firestore).
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const db = context.firestore();
        await setDoc(
            doc(db, "clientes", String(ID_CLIENTE)),
            mapaDeAltaReal(ID_CLIENTE, NEGOCIO, "99999999Z")
        );
    });

    const db = testEnvironment.authenticatedContext(ADMIN_UID).firestore();
    const b = writeBatch(db);
    b.set(
        doc(db, "clientes", String(ID_CLIENTE)),
        mapaDeAltaReal(ID_CLIENTE, NEGOCIO, DNI)
    );
    b.set(
        doc(db, "indices_clientes", indiceDocId(NEGOCIO, DNI)),
        indiceReal(NEGOCIO, DNI, ID_CLIENTE)
    );
    b.set(
        doc(db, "clientes_privados", String(ID_CLIENTE)),
        privadosReal(NEGOCIO)
    );
    // clientes/22 ya existe -> set() = UPDATE -> hasOnly de edicion NO incluye
    // firebaseUid/idCliente/negocioId/fechaRegistro -> DENEGADO.
    await assertFails(b.commit());
});

test("7) BATCH COMPLETO con PRIVADOS YA EXISTENTE -> debe FALLAR (update con set completo incluye negocioId y la Rule update de privados exige hasOnly([observaciones]))", async () => {
    // Preparar privados previo.
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const db = context.firestore();
        await setDoc(
            doc(db, "clientes_privados", String(ID_CLIENTE)),
            privadosReal(NEGOCIO)
        );
    });

    const db = testEnvironment.authenticatedContext(ADMIN_UID).firestore();
    const b = writeBatch(db);
    b.set(
        doc(db, "clientes", String(ID_CLIENTE)),
        mapaDeAltaReal(ID_CLIENTE, NEGOCIO, DNI)
    );
    b.set(
        doc(db, "indices_clientes", indiceDocId(NEGOCIO, DNI)),
        indiceReal(NEGOCIO, DNI, ID_CLIENTE)
    );
    b.set(
        doc(db, "clientes_privados", String(ID_CLIENTE)),
        privadosReal(NEGOCIO)
    );
    // clientes_privados ya existe -> set() = UPDATE; la Rule de privados update
    // exige hasOnly(["observaciones"]), pero el set completo reescribe tambien
    // negocioId -> DENEGADO (aunque negocioId no cambie, el set completo lo
    // incluye en request.resource.data y el update solo admite observaciones).
    await assertFails(b.commit());
});
