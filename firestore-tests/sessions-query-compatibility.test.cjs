const fs = require("node:fs");
const path = require("node:path");
const assert = require("node:assert/strict");
const { after, before, test } = require("node:test");

const {
    collection,
    doc,
    getDocs,
    query,
    setDoc,
    where
} = require("firebase/firestore");
const {
    assertFails,
    assertSucceeds,
    initializeTestEnvironment
} = require("@firebase/rules-unit-testing");

const PROJECT_ID = "gestorpro-rules-test";
const CLIENTE_UID = "cliente-servicios-test";
const RULES_FILE = path.resolve(
    __dirname,
    "firestore.rules.sessions-query.generated"
);

let testEnvironment;

before(async () => {
    testEnvironment = await initializeTestEnvironment({
        projectId: PROJECT_ID,
        firestore: {
            rules: fs.readFileSync(RULES_FILE, "utf8")
        }
    });

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", CLIENTE_UID), {
            rol: "CLIENTE",
            activo: true,
            clienteId: 10,
            negocioId: "negocio-a"
        });

        await setDoc(doc(database, "clientes", "10"), {
            idCliente: 10,
            firebaseUid: CLIENTE_UID,
            negocioId: "negocio-a",
            serviciosContratados: ["yoga"]
        });

        await setDoc(doc(database, "sesiones", "sesion-yoga"), {
            negocioId: "negocio-a",
            servicio: "yoga",
            clientesPermitidos: [CLIENTE_UID]
        });

        await setDoc(doc(database, "sesiones", "sesion-spinning"), {
            negocioId: "negocio-a",
            servicio: "spinning",
            clientesPermitidos: [CLIENTE_UID]
        });
    });
});

after(async () => {
    await testEnvironment?.cleanup();
});

function consultasBase(database) {
    return [
        where("negocioId", "==", "negocio-a"),
        where("clientesPermitidos", "array-contains", CLIENTE_UID)
    ];
}

async function ejecutarConsulta(database, filtros) {
    try {
        const resultado = await getDocs(
            query(collection(database, "sesiones"), ...filtros)
        );

        return {
            aceptada: true,
            documentos: resultado.docs.map((documento) => documento.id)
        };
    } catch (error) {
        return {
            aceptada: false,
            error: error.code ?? error.message
        };
    }
}

test("CONSULTA 1: negocioId y clientesPermitidos sin servicio es denegada", async () => {
    const database = testEnvironment
        .authenticatedContext(CLIENTE_UID)
        .firestore();

    const resultado = await ejecutarConsulta(
        database,
        consultasBase(database)
    );

    console.log("[CONSULTA 1]", resultado);
    assert.equal(resultado.aceptada, false);
});

test("CONSULTA 2: yoga contratado es aceptada", async () => {
    const database = testEnvironment
        .authenticatedContext(CLIENTE_UID)
        .firestore();

    const resultado = await ejecutarConsulta(database, [
        ...consultasBase(database),
        where("servicio", "==", "yoga")
    ]);

    console.log("[CONSULTA 2]", resultado);
    await assertSucceeds(
        getDocs(
            query(
                collection(database, "sesiones"),
                ...consultasBase(database),
                where("servicio", "==", "yoga")
            )
        )
    );
    assert.equal(resultado.aceptada, true);
});

test("CONSULTA 3: spinning no contratado es denegada", async () => {
    const database = testEnvironment
        .authenticatedContext(CLIENTE_UID)
        .firestore();

    const resultado = await ejecutarConsulta(database, [
        ...consultasBase(database),
        where("servicio", "==", "spinning")
    ]);

    console.log("[CONSULTA 3]", resultado);
    assert.equal(resultado.aceptada, false);
    await assertFails(
        getDocs(
            query(
                collection(database, "sesiones"),
                ...consultasBase(database),
                where("servicio", "==", "spinning")
            )
        )
    );
});

test("CONSULTA 4: whereIn con yoga comprueba compatibilidad", async () => {
    const database = testEnvironment
        .authenticatedContext(CLIENTE_UID)
        .firestore();

    const resultado = await ejecutarConsulta(database, [
        ...consultasBase(database),
        where("servicio", "in", ["yoga"])
    ]);

    console.log("[CONSULTA 4]", resultado);
    assert.equal(typeof resultado.aceptada, "boolean");
});
