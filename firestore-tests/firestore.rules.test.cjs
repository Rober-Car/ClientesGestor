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
    collection,
    deleteDoc,
    doc,
    getDoc,
    getDocs,
    query,
    setDoc,
    updateDoc,
    where,
    writeBatch
} = require("firebase/firestore");
const {
    ref: storageRef,
    uploadBytes,
    getBytes
} = require("firebase/storage");

const PROJECT_ID = "gestorpro-rules-test";
const CLIENTE_UID = "Vnyht6hlR5EYJ1G0vxxl";
const OTRO_CLIENTE_UID = "otro-cliente-de-prueba";
const NEGOCIO_A = "negocio-a";
const NEGOCIO_B = "negocio-b";

// Campos completos de la ficha publica de un cliente.
function fichaCliente(
    idCliente,
    negocioId,
    firebaseUid,
    dni,
    extra = {}
) {
    return {
        idCliente,
        negocioId,
        firebaseUid,
        nombre: extra.nombre ?? "Cliente",
        apellidos: extra.apellidos ?? "De Prueba",
        dni,
        telefono: extra.telefono ?? "600000000",
        email: extra.email ?? "cliente@test.com",
        foto: extra.foto ?? "",
        fechaNacimiento: extra.fechaNacimiento ?? 0,
        fechaRegistro: extra.fechaRegistro ?? 1,
        fechaAlta: extra.fechaAlta ?? null,
        fechaBaja: extra.fechaBaja ?? null,
        estado: extra.estado ?? "ACTIVO",
        tieneLlave: extra.tieneLlave ?? false,
        serviciosContratados: extra.serviciosContratados ?? [],
        fechaInicioActual: extra.fechaInicioActual ?? null,
        fechaFinActual: extra.fechaFinActual ?? null,
        ...extra
    };
}

function indiceId(negocioId, dni) {
    return `${negocioId}_${dni}`;
}

let testEnvironment;

before(async () => {
    testEnvironment = await initializeTestEnvironment({
        projectId: PROJECT_ID,
        firestore: {
            rules: fs.readFileSync(
                path.resolve(__dirname, "..", "firestore.rules"),
                "utf8"
            )
        },
        storage: {
            rules: fs.readFileSync(
                path.resolve(__dirname, "..", "storage.rules"),
                "utf8"
            )
        }
    });

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        // CLIENTE sin vínculo (para pruebas de aislamiento).
        await setDoc(doc(database, "usuarios", CLIENTE_UID), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });

        // Ficha ajena ya vinculada.
        await setDoc(
            doc(database, "clientes", "1"),
            fichaCliente(1, NEGOCIO_A, OTRO_CLIENTE_UID, "12345678A")
        );
    });
});

after(async () => {
    await testEnvironment?.cleanup();
});

test("PRUEBA 1: un CLIENTE no puede leer otro cliente", async () => {
    const database = testEnvironment.authenticatedContext(CLIENTE_UID).firestore();

    await assertSucceeds(
        getDoc(doc(database, "usuarios", CLIENTE_UID))
    );

    await assertFails(
        getDoc(doc(database, "clientes", "1"))
    );
});

test("PRUEBA 2: un CLIENTE no puede modificar sus permisos ni su vinculacion", async () => {
    const database = testEnvironment.authenticatedContext(CLIENTE_UID).firestore();
    const usuario = doc(database, "usuarios", CLIENTE_UID);

    await assertFails(
        updateDoc(usuario, { rol: "ADMIN" })
    );

    await assertFails(
        updateDoc(usuario, { activo: false })
    );

    await assertFails(
        updateDoc(usuario, { clienteId: 1 })
    );

    await assertFails(
        updateDoc(usuario, { negocioId: "otro-negocio" })
    );
});

test("PRUEBA 3: un ADMIN solo puede leer datos de su negocio", async () => {
    const adminUid = "admin-negocio-a";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", adminUid), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId: NEGOCIO_A
        });

        await setDoc(doc(database, "negocios", NEGOCIO_A), {
            adminUid
        });

        await setDoc(doc(database, "negocios", NEGOCIO_B), {
            adminUid: "admin-negocio-b"
        });

        await setDoc(
            doc(database, "clientes", "10"),
            fichaCliente(10, NEGOCIO_A, "cliente-negocio-a", "11111111A")
        );

        await setDoc(
            doc(database, "clientes", "20"),
            fichaCliente(20, NEGOCIO_B, "cliente-negocio-b", "22222222B")
        );
    });

    const database = testEnvironment.authenticatedContext(adminUid).firestore();

    await assertSucceeds(
        getDoc(doc(database, "clientes", "10"))
    );

    await assertFails(
        getDoc(doc(database, "clientes", "20"))
    );

    await assertFails(
        getDoc(doc(database, "negocios", NEGOCIO_B))
    );

    await assertSucceeds(
        getDoc(doc(database, "negocios", NEGOCIO_A))
    );
});

test("PRUEBA 4: un CLIENTE no puede leer movimientos y un ADMIN sí", async () => {
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", "cliente-economico-test"), {
            rol: "CLIENTE",
            activo: true,
            clienteId: 2,
            negocioId: NEGOCIO_A
        });

        await setDoc(doc(database, "usuarios", "admin-economico-test"), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId: NEGOCIO_A
        });

        await setDoc(doc(database, "movimientos", "movimiento-test"), {
            negocioId: NEGOCIO_A
        });
    });

    const clienteDatabase = testEnvironment
        .authenticatedContext("cliente-economico-test")
        .firestore();

    await assertFails(
        getDoc(doc(clienteDatabase, "movimientos", "movimiento-test"))
    );

    const adminDatabase = testEnvironment
        .authenticatedContext("admin-economico-test")
        .firestore();

    await assertSucceeds(
        getDoc(doc(adminDatabase, "movimientos", "movimiento-test"))
    );
});

test("PRUEBA 5: VIA 2 - un CLIENTE crea su ficha con codigo maestro + DNI", async () => {
    const clienteUid = "cliente-via2-test";
    const otroUid = "cliente-via2-otro-test";
    const dni = "11111111A";
    const dniOtro = "22222222B";
    const idLibre = 74000000001;
    const idOcupado = 74000000002;
    const negocioId = "negocio-via2-5";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", clienteUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });

        await setDoc(doc(database, "usuarios", otroUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });

        // Perfil pendiente con el DNI del cliente.
        await setDoc(doc(database, "perfiles_pendientes", clienteUid), {
            nombre: "Ana",
            apellidos: "Lopez",
            dni,
            telefono: "611111111",
            email: "ana@test.com",
            foto: "",
            fechaNacimiento: 0
        });

        await setDoc(doc(database, "perfiles_pendientes", otroUid), {
            nombre: "Pepe",
            apellidos: "Perez",
            dni: dniOtro,
            telefono: "622222222",
            email: "pepe@test.com",
            foto: "",
            fechaNacimiento: 0
        });

        await setDoc(doc(database, "negocios_publicos", negocioId), {
            nombre: "Gimnasio Prueba",
            codigoMaestro: "MAESTRO-5"
        });

        // Ficha ocupada con OTRO DNI (indice propio), para no interferir con
        // el DNI que el cliente usara en el caso valido.
        await setDoc(
            doc(database, "clientes", String(idOcupado)),
            fichaCliente(idOcupado, negocioId, otroUid, "99999999Z")
        );
        await setDoc(
            doc(database, "indices_clientes", indiceId(negocioId, "99999999Z")),
            { negocioId, dni: "99999999Z", clienteId: idOcupado }
        );
    });

    const database = testEnvironment.authenticatedContext(clienteUid).firestore();

    // Caso valido: Transaction crea ficha + indice + actualiza usuarios.
    const batchValido = writeBatch(database);
    batchValido.set(
        doc(database, "clientes", String(idLibre)),
        fichaCliente(idLibre, negocioId, clienteUid, dni, { estado: "REGISTRADO" })
    );
    batchValido.set(
        doc(database, "indices_clientes", indiceId(negocioId, dni)),
        { negocioId, dni, clienteId: idLibre }
    );
    batchValido.update(doc(database, "usuarios", clienteUid), {
        clienteId: idLibre,
        negocioId
    });

    await assertSucceeds(batchValido.commit());

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();
        const usuario = await getDoc(doc(database, "usuarios", clienteUid));
        const ficha = await getDoc(doc(database, "clientes", String(idLibre)));

        assert.strictEqual(usuario.data().clienteId, idLibre);
        assert.strictEqual(usuario.data().negocioId, negocioId);
        assert.strictEqual(ficha.data().firebaseUid, clienteUid);
        assert.strictEqual(ficha.data().dni, dni);
    });

    // Caso invalido: ya existe un indice para ese negocio+DNI (duplicado).
    const batchDuplicado = writeBatch(database);
    batchDuplicado.set(
        doc(database, "clientes", String(idLibre + 100)),
        fichaCliente(idLibre + 100, negocioId, clienteUid, dni)
    );
    batchDuplicado.set(
        doc(database, "indices_clientes", indiceId(negocioId, dni)),
        { negocioId, dni, clienteId: idLibre + 100 }
    );
    batchDuplicado.update(doc(database, "usuarios", clienteUid), {
        clienteId: idLibre + 100,
        negocioId
    });

    await assertFails(batchDuplicado.commit());

    // Caso invalido: crear la ficha con un DNI distinto del perfil pendiente.
    const databaseOtro = testEnvironment.authenticatedContext(otroUid).firestore();
    const batchDniAjeno = writeBatch(databaseOtro);
    const idDniAjeno = 74000000003;
    batchDniAjeno.set(
        doc(databaseOtro, "clientes", String(idDniAjeno)),
        fichaCliente(idDniAjeno, negocioId, otroUid, dni) // dni de otro
    );
    batchDniAjeno.set(
        doc(databaseOtro, "indices_clientes", indiceId(negocioId, dni)),
        { negocioId, dni, clienteId: idDniAjeno }
    );
    batchDniAjeno.update(doc(databaseOtro, "usuarios", otroUid), {
        clienteId: idDniAjeno,
        negocioId
    });

    await assertFails(batchDniAjeno.commit());

    // Caso invalido: crear la ficha sin crear el indice en el mismo Batch.
    const idHuerfano = 74000000004;
    await assertFails(
        setDoc(
            doc(databaseOtro, "clientes", String(idHuerfano)),
            fichaCliente(idHuerfano, negocioId, otroUid, dniOtro)
        )
    );
});

test("PRUEBA 6: VIA 1 - un CLIENTE vincula una ficha creada por el ADMIN", async () => {
    const adminUid = "admin-via1-test";
    const clienteUid = "cliente-via1-test";
    const dni = "33333333C";
    const idFicha = 741;
    const negocioId = "negocio-via1-6";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", adminUid), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId
        });

        await setDoc(doc(database, "usuarios", clienteUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });

        await setDoc(doc(database, "negocios_publicos", negocioId), {
            nombre: "Gimnasio Via1",
            codigoMaestro: "MAESTRO-6"
        });

        // Ficha creada por el ADMIN: sin UID y con su indice.
        await setDoc(
            doc(database, "clientes", String(idFicha)),
            fichaCliente(idFicha, negocioId, null, dni)
        );
        await setDoc(
            doc(database, "indices_clientes", indiceId(negocioId, dni)),
            { negocioId, dni, clienteId: idFicha }
        );

        // Ficha ya vinculada (no reclamable).
        await setDoc(
            doc(database, "clientes", "742"),
            fichaCliente(742, negocioId, "cliente-ya-vinculado", "44444444D")
        );
        await setDoc(
            doc(database, "indices_clientes", indiceId(negocioId, "44444444D")),
            { negocioId, dni: "44444444D", clienteId: 742 }
        );

        // Indice del MISMO dni pero en OTRO negocio (existe, para probar DENY).
        await setDoc(
            doc(database, "indices_clientes", indiceId("otro-negocio", dni)),
            { negocioId: "otro-negocio", dni, clienteId: 743 }
        );
    });

    // El CLIENTE declara su perfil pendiente con DNI + negocioId (VÍA 1) para
    // poder consultar el indice y reclamar su ficha.
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();
        await setDoc(doc(database, "perfiles_pendientes", clienteUid), {
            dni,
            negocioId
        });
    });

    const database = testEnvironment.authenticatedContext(clienteUid).firestore();

    // Puede leer el indice de SU DNI + SU negocio (VÍA 1 declarado).
    await assertSucceeds(
        getDoc(doc(database, "indices_clientes", indiceId(negocioId, dni)))
    );
    // No puede leer el indice de otro DNI (mismo negocio).
    await assertFails(
        getDoc(doc(database, "indices_clientes", indiceId(negocioId, "44444444D")))
    );
    // No puede leer el indice del mismo DNI en OTRO negocio.
    await assertFails(
        getDoc(doc(database, "indices_clientes", indiceId("otro-negocio", dni)))
    );
    // No puede listar indices.
    await assertFails(
        getDocs(collection(database, "indices_clientes"))
    );

    // Caso invalido: vincularse sin actualizar usuarios/{uid} en el Batch
    // (la ficha sigue libre, el batch dejaria documentos incoherentes).
    const batchSinUsuario = writeBatch(database);
    batchSinUsuario.update(doc(database, "clientes", String(idFicha)), {
        firebaseUid: clienteUid,
        negocioId
    });
    await assertFails(batchSinUsuario.commit());

    // Caso invalido: reclamar una ficha que ya tiene UID.
    const batchYaVinculada = writeBatch(database);
    batchYaVinculada.update(doc(database, "clientes", "742"), {
        firebaseUid: clienteUid,
        negocioId
    });
    batchYaVinculada.update(doc(database, "usuarios", clienteUid), {
        clienteId: 742,
        negocioId
    });
    await assertFails(batchYaVinculada.commit());

    // Vinculacion valida: ficha libre + usuarios/{uid}.
    const batchVinculacion = writeBatch(database);
    batchVinculacion.update(doc(database, "clientes", String(idFicha)), {
        firebaseUid: clienteUid,
        negocioId
    });
    batchVinculacion.update(doc(database, "usuarios", clienteUid), {
        clienteId: idFicha,
        negocioId
    });

    await assertSucceeds(batchVinculacion.commit());

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();
        const ficha = await getDoc(doc(database, "clientes", String(idFicha)));
        assert.strictEqual(ficha.data().firebaseUid, clienteUid);
    });
});

test("PRUEBA 7: un CLIENTE vinculado solo puede editar sus datos personales", async () => {
    const clienteUid = "cliente-personal-test";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", clienteUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: 10,
            negocioId: NEGOCIO_A
        });

        await setDoc(
            doc(database, "clientes", "10"),
            fichaCliente(10, NEGOCIO_A, clienteUid, "11111111A", {
                estado: "ACTIVO",
                fechaAlta: 1,
                fechaBaja: null,
                fechaInicioActual: 1,
                fechaFinActual: 2,
                serviciosContratados: ["Servicio A"],
                nombre: "Cliente",
                apellidos: "Propio",
                dni: "11111111A",
                telefono: "600000000",
                email: "cliente@test.com",
                foto: "",
                fechaNacimiento: 0
            })
        );

        await setDoc(
            doc(database, "clientes", "20"),
            fichaCliente(20, NEGOCIO_A, "cliente-otro-test", "22222222B")
        );
    });

    const database = testEnvironment.authenticatedContext(clienteUid).firestore();

    await assertSucceeds(
        getDoc(doc(database, "clientes", "10"))
    );
    await assertFails(
        getDoc(doc(database, "clientes", "20"))
    );
    await assertFails(
        getDocs(collection(database, "clientes"))
    );
    await assertFails(
        updateDoc(doc(database, "clientes", "20"), { nombre: "Intento" })
    );

    const camposProtegidos = [
        { firebaseUid: "uid-alterado" },
        { negocioId: "otro-negocio" },
        { idCliente: 99 },
        { dni: "99999999Z" },
        { estado: "BAJA" },
        { fechaAlta: 3 },
        { fechaBaja: 4 },
        { fechaInicioActual: 3 },
        { fechaFinActual: 4 },
        { serviciosContratados: ["No contratado"] },
        { tieneLlave: true },
        { observaciones: "Intento de ver/editar observaciones" }
    ];

    for (const cambios of camposProtegidos) {
        await assertFails(
            updateDoc(doc(database, "clientes", "10"), cambios)
        );
    }

    await assertSucceeds(
        updateDoc(doc(database, "clientes", "10"), { nombre: "Nombre actualizado" })
    );
    await assertSucceeds(
        updateDoc(doc(database, "clientes", "10"), { telefono: "699999999" })
    );
});

test("PRUEBA 8: un CLIENTE no accede a clientes_privados ni a perfiles ajenos", async () => {
    const clienteUid = "cliente-privado-test";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", clienteUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: 10,
            negocioId: NEGOCIO_A
        });

        await setDoc(doc(database, "clientes_privados", "10"), {
            negocioId: NEGOCIO_A,
            observaciones: "Nota interna del gimnasio"
        });

        await setDoc(doc(database, "perfiles_pendientes", "cliente-ajeno"), {
            nombre: "Ajeno",
            dni: "11111111A"
        });
    });

    const database = testEnvironment.authenticatedContext(clienteUid).firestore();

    await assertFails(
        getDoc(doc(database, "clientes_privados", "10"))
    );
    await assertFails(
        updateDoc(doc(database, "clientes_privados", "10"), { observaciones: "x" })
    );
    await assertFails(
        getDoc(doc(database, "perfiles_pendientes", "cliente-ajeno"))
    );
});

test("PRUEBA 9: el ADMIN gestiona clientes e indices de su negocio", async () => {
    const adminUid = "admin-escritura-test";
    const adminOtroUid = "admin-otro-escritura-test";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", adminUid), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId: NEGOCIO_A
        });

        await setDoc(doc(database, "usuarios", adminOtroUid), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId: NEGOCIO_B
        });

        await setDoc(doc(database, "negocios", NEGOCIO_A), {
            adminUid,
            nombre: "Negocio A"
        });

        await setDoc(
            doc(database, "clientes", "31"),
            fichaCliente(31, NEGOCIO_A, "cliente-31", "31313131X")
        );
        await setDoc(
            doc(database, "indices_clientes", indiceId(NEGOCIO_A, "31313131X")),
            { negocioId: NEGOCIO_A, dni: "31313131X", clienteId: 31 }
        );
        await setDoc(doc(database, "clientes_privados", "31"), {
            negocioId: NEGOCIO_A,
            observaciones: "Cliente 31"
        });
    });

    const database = testEnvironment.authenticatedContext(adminUid).firestore();
    const databaseOtro = testEnvironment.authenticatedContext(adminOtroUid).firestore();

    // Crear una ficha nueva del propio negocio: clientes + indice + privados.
    const idNuevo = 32;
    const dniNuevo = "32323232Y";
    const batchCrear = writeBatch(database);
    batchCrear.set(
        doc(database, "clientes", String(idNuevo)),
        fichaCliente(idNuevo, NEGOCIO_A, null, dniNuevo)
    );
    batchCrear.set(
        doc(database, "indices_clientes", indiceId(NEGOCIO_A, dniNuevo)),
        { negocioId: NEGOCIO_A, dni: dniNuevo, clienteId: idNuevo }
    );
    batchCrear.set(doc(database, "clientes_privados", String(idNuevo)), {
        negocioId: NEGOCIO_A,
        observaciones: "Nuevo"
    });

    await assertSucceeds(batchCrear.commit());

    // Crear una ficha del negocio ajeno (NEGOCIO_A): denegado.
    const batchAjeno = writeBatch(databaseOtro);
    batchAjeno.set(
        doc(databaseOtro, "clientes", "33"),
        fichaCliente(33, NEGOCIO_A, null, "33333333C")
    );
    batchAjeno.set(
        doc(databaseOtro, "indices_clientes", indiceId(NEGOCIO_A, "33333333C")),
        { negocioId: NEGOCIO_A, dni: "33333333C", clienteId: 33 }
    );
    await assertFails(batchAjeno.commit());

    // Crear una ficha SIN indice: denegado (violaria unicidad).
    const batchSinIndice = writeBatch(database);
    batchSinIndice.set(
        doc(database, "clientes", "34"),
        fichaCliente(34, NEGOCIO_A, null, "34343434D")
    );
    await assertFails(batchSinIndice.commit());

    // Editar datos de gestion: permitido.
    await assertSucceeds(
        updateDoc(doc(database, "clientes", "31"), { nombre: "Editado" })
    );

    // Editar observaciones via clientes_privados: permitido al ADMIN.
    await assertSucceeds(
        updateDoc(doc(database, "clientes_privados", "31"), { observaciones: "Nueva nota" })
    );

    // Borrar clientes: prohibido (baja logica).
    await assertFails(deleteDoc(doc(database, "clientes", "31")));
});

test("PRUEBA 10: el ADMIN cambia el DNI manteniendo el indice atomico", async () => {
    const adminUid = "admin-cambiodni-test";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", adminUid), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId: NEGOCIO_A
        });

        await setDoc(
            doc(database, "clientes", "41"),
            fichaCliente(41, NEGOCIO_A, null, "41414141A")
        );
        await setDoc(
            doc(database, "indices_clientes", indiceId(NEGOCIO_A, "41414141A")),
            { negocioId: NEGOCIO_A, dni: "41414141A", clienteId: 41 }
        );
    });

    const database = testEnvironment.authenticatedContext(adminUid).firestore();

    // Cambiar el DNI sin tocar el indice: denegado.
    await assertFails(
        updateDoc(doc(database, "clientes", "41"), { dni: "42424242B" })
    );

    // Cambiar el DNI con el indice atomico (borra el viejo, crea el nuevo).
    const batchCambioDni = writeBatch(database);
    batchCambioDni.update(doc(database, "clientes", "41"), { dni: "42424242B" });
    batchCambioDni.delete(
        doc(database, "indices_clientes", indiceId(NEGOCIO_A, "41414141A"))
    );
    batchCambioDni.set(
        doc(database, "indices_clientes", indiceId(NEGOCIO_A, "42424242B")),
        { negocioId: NEGOCIO_A, dni: "42424242B", clienteId: 41 }
    );

    await assertSucceeds(batchCambioDni.commit());

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();
        const nuevo = await getDoc(
            doc(database, "indices_clientes", indiceId(NEGOCIO_A, "42424242B"))
        );
        const viejo = await getDoc(
            doc(database, "indices_clientes", indiceId(NEGOCIO_A, "41414141A"))
        );
        assert.strictEqual(nuevo.data().clienteId, 41);
        assert.ok(!viejo.exists());
    });
});

test("PRUEBA 11: un CLIENTE registrado gestiona su perfil pendiente", async () => {
    const clienteUid = "cliente-perfilpendiente-test";
    const otroUid = "cliente-perfilpendiente-otro";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();
        await setDoc(doc(database, "usuarios", clienteUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });
        await setDoc(doc(database, "usuarios", otroUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });
    });

    const database = testEnvironment.authenticatedContext(clienteUid).firestore();

    await assertSucceeds(
        setDoc(doc(database, "perfiles_pendientes", clienteUid), {
            nombre: "Ana",
            apellidos: "Lopez",
            dni: "11111111A",
            telefono: "611111111",
            email: "ana@test.com",
            foto: "",
            fechaNacimiento: 0
        })
    );

    await assertSucceeds(
        updateDoc(doc(database, "perfiles_pendientes", clienteUid), {
            telefono: "699999999"
        })
    );

    // No puede escribir el perfil pendiente de otro usuario.
    await assertFails(
        setDoc(doc(database, "perfiles_pendientes", otroUid), {
            nombre: "Hack",
            dni: "11111111A"
        })
    );

    // No puede listar perfiles pendientes.
    await assertFails(
        getDocs(collection(database, "perfiles_pendientes"))
    );

    await assertSucceeds(
        deleteDoc(doc(database, "perfiles_pendientes", clienteUid))
    );
});

test("PRUEBA 12: concurrencia - dos CLIENTES con el mismo DNI no duplican ficha", async () => {
    const clienteA = "cliente-race-a";
    const clienteB = "cliente-race-b";
    const dni = "55555555E";
    const negocioId = "negocio-race-12";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", clienteA), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });
        await setDoc(doc(database, "usuarios", clienteB), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });
        await setDoc(doc(database, "perfiles_pendientes", clienteA), {
            dni, nombre: "A"
        });
        await setDoc(doc(database, "perfiles_pendientes", clienteB), {
            dni, nombre: "B"
        });
        await setDoc(doc(database, "negocios_publicos", negocioId), {
            codigoMaestro: "MAESTRO-12"
        });
    });

    const dbA = testEnvironment.authenticatedContext(clienteA).firestore();
    const dbB = testEnvironment.authenticatedContext(clienteB).firestore();

    // A crea la ficha + indice + usuarios.
    const batchA = writeBatch(dbA);
    batchA.set(
        doc(dbA, "clientes", "500"),
        fichaCliente(500, negocioId, clienteA, dni)
    );
    batchA.set(
        doc(dbA, "indices_clientes", indiceId(negocioId, dni)),
        { negocioId, dni, clienteId: 500 }
    );
    batchA.update(doc(dbA, "usuarios", clienteA), {
        clienteId: 500,
        negocioId
    });
    await assertSucceeds(batchA.commit());

    // B intenta crear su propia ficha con el MISMO indice: denegado.
    const batchB = writeBatch(dbB);
    batchB.set(
        doc(dbB, "clientes", "501"),
        fichaCliente(501, negocioId, clienteB, dni)
    );
    batchB.set(
        doc(dbB, "indices_clientes", indiceId(negocioId, dni)),
        { negocioId, dni, clienteId: 501 }
    );
    batchB.update(doc(dbB, "usuarios", clienteB), {
        clienteId: 501,
        negocioId
    });
    await assertFails(batchB.commit());

    // Verificar que solo existe una ficha con ese negocio+DNI.
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();
        const ficha = await getDoc(doc(database, "clientes", "500"));
        const fichaB = await getDoc(doc(database, "clientes", "501"));
        assert.strictEqual(ficha.data().firebaseUid, clienteA);
        assert.ok(!fichaB.exists());
    });
});

test("PRUEBA 13: un CLIENTE ya vinculado no puede volver a vincularse", async () => {
    const clienteUid = "cliente-repetido-13";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();
        await setDoc(doc(database, "usuarios", clienteUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: 600,
            negocioId: NEGOCIO_A
        });
        await setDoc(
            doc(database, "clientes", "600"),
            fichaCliente(600, NEGOCIO_A, clienteUid, "66666666F")
        );
        await setDoc(
            doc(database, "indices_clientes", indiceId(NEGOCIO_A, "66666666F")),
            { negocioId: NEGOCIO_A, dni: "66666666F", clienteId: 600 }
        );
    });

    const database = testEnvironment.authenticatedContext(clienteUid).firestore();

    // No puede re-vincularse (usuarios/{uid}.clienteId ya no es null).
    const batchReintento = writeBatch(database);
    batchReintento.update(doc(database, "usuarios", clienteUid), {
        clienteId: 601,
        negocioId: NEGOCIO_A
    });
    await assertFails(batchReintento.commit());
});

test("PRUEBA 14: el ADMIN puede leer y listar clientes_privados; CLIENTE nunca", async () => {
    const adminUid = "admin-privados-test";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();
        await setDoc(doc(database, "usuarios", adminUid), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId: NEGOCIO_A
        });
        await setDoc(doc(database, "clientes_privados", "70"), {
            negocioId: NEGOCIO_A,
            observaciones: "Nota"
        });
        await setDoc(doc(database, "clientes_privados", "71"), {
            negocioId: NEGOCIO_B,
            observaciones: "Nota B"
        });
    });

    const dbAdmin = testEnvironment.authenticatedContext(adminUid).firestore();
    const dbCliente = testEnvironment.authenticatedContext(CLIENTE_UID).firestore();

    await assertSucceeds(getDoc(doc(dbAdmin, "clientes_privados", "70")));
    await assertFails(getDoc(doc(dbAdmin, "clientes_privados", "71")));
    await assertFails(getDoc(doc(dbCliente, "clientes_privados", "70")));
});

test("PRUEBA 15: negocios_publicos es legible por cualquier autenticado", async () => {
    const negocioId = "negocio-publico-15";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();
        await setDoc(doc(database, "negocios_publicos", negocioId), {
            nombre: "Gimnasio Publico",
            codigoMaestro: "MAESTRO-15"
        });
    });

    const database = testEnvironment.authenticatedContext(CLIENTE_UID).firestore();

    await assertSucceeds(
        getDoc(doc(database, "negocios_publicos", negocioId))
    );

    const databaseAnonima = testEnvironment.unauthenticatedContext().firestore();
    await assertFails(
        getDoc(doc(databaseAnonima, "negocios_publicos", negocioId))
    );
});

test("PRUEBA 16: un CLIENTE no puede modificar negocios_publicos", async () => {
    const negocioId = "negocio-publico-16";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();
        await setDoc(doc(database, "negocios_publicos", negocioId), {
            nombre: "Gimnasio Ajeno",
            codigoMaestro: "MAESTRO-16"
        });
    });

    const database = testEnvironment.authenticatedContext(CLIENTE_UID).firestore();

    await assertFails(
        updateDoc(doc(database, "negocios_publicos", negocioId), {
            codigoMaestro: "CODIGO-MALICIOSO"
        })
    );
    await assertFails(
        setDoc(doc(database, "negocios_publicos", "negocio-falso-16"), {
            nombre: "Falso",
            codigoMaestro: "FALSO"
        })
    );
    await assertFails(
        deleteDoc(doc(database, "negocios_publicos", negocioId))
    );
});

test("PRUEBA 17: VIA 1 - la lectura del indice exige dni y negocioId declarados en perfiles_pendientes", async () => {
    const clienteUid = "cliente-via1-decl-test";
    const negocioId = "negocio-via1-decl";
    const otroNegocioId = "otro-negocio-via1-decl";
    const dni = "77777777G";
    const idFicha = 780;

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", clienteUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });

        await setDoc(
            doc(database, "clientes", String(idFicha)),
            fichaCliente(idFicha, negocioId, null, dni)
        );
        await setDoc(
            doc(database, "indices_clientes", indiceId(negocioId, dni)),
            { negocioId, dni, clienteId: idFicha }
        );
        await setDoc(
            doc(database, "indices_clientes", indiceId(otroNegocioId, dni)),
            { negocioId: otroNegocioId, dni, clienteId: 781 }
        );
        // Indice existente con OTRO dni en el mismo negocio (para probar DENY).
        await setDoc(
            doc(database, "indices_clientes", indiceId(negocioId, "88888888H")),
            { negocioId, dni: "88888888H", clienteId: 782 }
        );
    });

    const database = testEnvironment.authenticatedContext(clienteUid).firestore();

    // 1. VIA 1 valida: declara { dni, negocioId } y puede leer el indice.
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        await setDoc(doc(context.firestore(), "perfiles_pendientes", clienteUid), {
            dni,
            negocioId
        });
    });
    await assertSucceeds(
        getDoc(doc(database, "indices_clientes", indiceId(negocioId, dni)))
    );

    // 2. DNI distinto al declarado -> DENY.
    await assertFails(
        getDoc(doc(database, "indices_clientes", indiceId(negocioId, "88888888H")))
    );

    // 3/4. indice del MISMO dni pero de OTRO negocio (no declarado) -> DENY.
    await assertFails(
        getDoc(doc(database, "indices_clientes", indiceId(otroNegocioId, dni)))
    );

    // 5. list de indices_clientes -> DENY.
    await assertFails(
        getDocs(collection(database, "indices_clientes"))
    );

    // 6. Cambiar la declaracion a otro negocio permite leer ese indice (no el anterior).
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        await setDoc(doc(context.firestore(), "perfiles_pendientes", clienteUid), {
            dni,
            negocioId: otroNegocioId
        });
    });
    await assertSucceeds(
        getDoc(doc(database, "indices_clientes", indiceId(otroNegocioId, dni)))
    );
    await assertFails(
        getDoc(doc(database, "indices_clientes", indiceId(negocioId, dni)))
    );

    // 7. El CLIENTE puede borrar su perfil pendiente (tras vincular o rechazar).
    await assertSucceeds(
        deleteDoc(doc(database, "perfiles_pendientes", clienteUid))
    );
});

test("PRUEBA 18: VIA 1 - el CLIENTE no vinculado solo lee la ficha que declaro", async () => {
    const clienteUid = "cliente-via1-lectura-test";
    const clienteVinculadoUid = "cliente-via1-vinculado-test";
    const negocioId = "negocio-via1-lectura";
    const otroNegocioId = "otro-negocio-via1-lectura";
    const dni = "99999999Z";
    const idFicha = 800;
    const idOtraFicha = 801;
    const idFichaVinculado = 802;

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", clienteUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });
        await setDoc(doc(database, "usuarios", clienteVinculadoUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: idFichaVinculado,
            negocioId
        });

        // Ficha libre del negocio declarado.
        await setDoc(
            doc(database, "clientes", String(idFicha)),
            fichaCliente(idFicha, negocioId, null, dni)
        );
        // Ficha de OTRO DNI en el mismo negocio.
        await setDoc(
            doc(database, "clientes", String(idOtraFicha)),
            fichaCliente(idOtraFicha, negocioId, null, "88888888X")
        );
        // Ficha de otro negocio con el mismo DNI.
        await setDoc(
            doc(database, "clientes", "805"),
            fichaCliente(805, otroNegocioId, null, dni)
        );
        // Ficha propia de un CLIENTE ya vinculado.
        await setDoc(
            doc(database, "clientes", String(idFichaVinculado)),
            fichaCliente(idFichaVinculado, negocioId, clienteVinculadoUid, "77777777W")
        );
    });

    // CLIENTE sin vínculo con declaracion correcta.
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        await setDoc(doc(context.firestore(), "perfiles_pendientes", clienteUid), {
            dni,
            negocioId
        });
    });

    const database = testEnvironment.authenticatedContext(clienteUid).firestore();

    // 1. Declaracion correcta + ficha correspondiente -> ALLOW.
    await assertSucceeds(
        getDoc(doc(database, "clientes", String(idFicha)))
    );

    // 2. Ficha de OTRO DNI en el mismo negocio -> DENY.
    await assertFails(
        getDoc(doc(database, "clientes", String(idOtraFicha)))
    );

    // 3. Ficha del MISMO dni en OTRO negocio -> DENY.
    await assertFails(
        getDoc(doc(database, "clientes", "805"))
    );

    // 4. CLIENTE ya vinculado intentando leer ficha ajena -> DENY.
    const dbVinculado = testEnvironment.authenticatedContext(clienteVinculadoUid).firestore();
    await assertFails(
        getDoc(doc(dbVinculado, "clientes", String(idFicha)))
    );

    // 5. CLIENTE no vinculado SIN perfiles_pendientes -> DENY.
    const clienteSinDeclaracion = "cliente-via1-sin-declaracion-test";
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        await setDoc(doc(context.firestore(), "usuarios", clienteSinDeclaracion), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });
    });
    const dbSinDecl = testEnvironment.authenticatedContext(clienteSinDeclaracion).firestore();
    await assertFails(
        getDoc(doc(dbSinDecl, "clientes", String(idFicha)))
    );

    // 6. list de clientes -> DENY (el CLIENTE no puede enumerar).
    await assertFails(
        getDocs(collection(database, "clientes"))
    );

    // 7. CLIENTE vinculado sigue leyendo SOLO su propia ficha.
    await assertSucceeds(
        getDoc(doc(dbVinculado, "clientes", String(idFichaVinculado)))
    );
    await assertFails(
        getDoc(doc(dbVinculado, "clientes", "805"))
    );
});

test("PRUEBA 19: Storage - el ADMIN propietario sube su logo y el resto no puede", async () => {
    const adminA = "admin-logo-a";
    const adminB = "admin-logo-b";
    const clienteUid = "cliente-logo-storage";
    const negocioA = "negocio-logo-a";
    const ruta = "negocios/negocio-logo-a/logo.jpg";
    const bytes = new Uint8Array([1, 2, 3, 4]);

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();
        await setDoc(doc(database, "usuarios", adminA), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId: negocioA
        });
        await setDoc(doc(database, "usuarios", adminB), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId: "negocio-logo-b"
        });
        await setDoc(doc(database, "usuarios", clienteUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: negocioA
        });
    });

    const storageAdminA = testEnvironment.authenticatedContext(adminA).storage();
    const storageAdminB = testEnvironment.authenticatedContext(adminB).storage();
    const storageCliente = testEnvironment.authenticatedContext(clienteUid).storage();
    const storageNoAuth = testEnvironment.unauthenticatedContext().storage();

    // 1. ADMIN propietario puede subir su logo.
    await assertSucceeds(
        uploadBytes(storageRef(storageAdminA, ruta), bytes)
    );

    // 2. ADMIN de otro negocio no puede escribir en ese logo.
    await assertFails(
        uploadBytes(storageRef(storageAdminB, ruta), bytes)
    );

    // 3. CLIENTE no puede escribir el logo.
    await assertFails(
        uploadBytes(storageRef(storageCliente, ruta), bytes)
    );

    // 4. Usuario no autenticado no puede escribir.
    await assertFails(
        uploadBytes(storageRef(storageNoAuth, ruta), bytes)
    );

    // 5. CLIENTE autenticado puede leer el logo.
    await assertSucceeds(
        getBytes(storageRef(storageCliente, ruta))
    );

    // 6. Usuario no autenticado no puede leer.
    await assertFails(
        getBytes(storageRef(storageNoAuth, ruta))
    );
});

test("PRUEBA 20: el ADMIN guarda el logo en negocios y negocios_publicos; el CLIENTE no", async () => {
    const adminUid = "admin-logo-firestore";
    const clienteUid = "cliente-logo-firestore";
    const negocioId = "negocio-logo-firestore";
    const url = "https://firebasestorage.googleapis.com/v0/b/x/o/logo.jpg";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();
        await setDoc(doc(database, "usuarios", adminUid), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId
        });
        await setDoc(doc(database, "negocios", negocioId), {
            adminUid,
            nombre: "Gimnasio",
            codigoMaestro: "MAESTRO-LOGO"
        });
        await setDoc(doc(database, "negocios_publicos", negocioId), {
            nombre: "Gimnasio",
            codigoMaestro: "MAESTRO-LOGO"
        });
        await setDoc(doc(database, "usuarios", clienteUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId
        });
    });

    const database = testEnvironment.authenticatedContext(adminUid).firestore();

    // El ADMIN puede añadir el logo a negocios_publicos.
    await assertSucceeds(
        updateDoc(doc(database, "negocios_publicos", negocioId), { logo: url })
    );

    // El ADMIN puede añadir el logo a negocios.
    await assertSucceeds(
        updateDoc(doc(database, "negocios", negocioId), { logo: url })
    );

    // Un CLIENTE no puede modificar negocios_publicos (ni el logo).
    const dbCliente = testEnvironment.authenticatedContext(clienteUid).firestore();
    await assertFails(
        updateDoc(doc(dbCliente, "negocios_publicos", negocioId), { logo: url })
    );
});
