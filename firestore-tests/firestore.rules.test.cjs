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

const PROJECT_ID = "gestorpro-rules-test";
const CLIENTE_UID = "Vnyht6hlR5EYJ1G0vxxl";
const OTRO_CLIENTE_UID = "otro-cliente-de-prueba";

let testEnvironment;

before(async () => {
    testEnvironment = await initializeTestEnvironment({
        projectId: PROJECT_ID,
        firestore: {
            rules: fs.readFileSync(
                path.resolve(__dirname, "..", "firestore.rules"),
                "utf8"
            )
        }
    });

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", CLIENTE_UID), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });

        await setDoc(doc(database, "clientes", "1"), {
            idCliente: 1,
            firebaseUid: OTRO_CLIENTE_UID,
            negocioId: "negocio-de-prueba"
        });
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
            negocioId: "negocio-a"
        });

        await setDoc(doc(database, "negocios", "negocio-a"), {
            adminUid
        });

        await setDoc(doc(database, "negocios", "negocio-b"), {
            adminUid: "admin-negocio-b"
        });

        await setDoc(doc(database, "clientes", "10"), {
            idCliente: 10,
            firebaseUid: "cliente-negocio-a",
            negocioId: "negocio-a"
        });

        await setDoc(doc(database, "clientes", "20"), {
            idCliente: 20,
            firebaseUid: "cliente-negocio-b",
            negocioId: "negocio-b"
        });
    });

    const database = testEnvironment.authenticatedContext(adminUid).firestore();

    await assertSucceeds(
        getDoc(doc(database, "clientes", "10"))
    );

    await assertFails(
        getDoc(doc(database, "clientes", "20"))
    );

    await assertFails(
        getDoc(doc(database, "negocios", "negocio-b"))
    );

    await assertSucceeds(
        getDoc(doc(database, "negocios", "negocio-a"))
    );
});

test("PRUEBA 4: un CLIENTE no puede leer movimientos y un ADMIN sí", async () => {
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", "cliente-economico-test"), {
            rol: "CLIENTE",
            activo: true,
            clienteId: 2,
            negocioId: "negocio-a"
        });

        await setDoc(doc(database, "usuarios", "admin-economico-test"), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId: "negocio-a"
        });

        await setDoc(doc(database, "movimientos", "movimiento-test"), {
            negocioId: "negocio-a"
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

test("PRUEBA 5: un codigo de vinculacion solo puede consumirse si es valido", async () => {
    const futuro = Timestamp.fromMillis(Date.now() + 60 * 60 * 1000);
    const caducado = Timestamp.fromMillis(Date.now() - 60 * 1000);

    const casos = [
        {
            uid: "cliente-vinculacion-usada-test",
            idCliente: 31,
            codigo: "codigo-usada-test",
            estado: "USADA",
            fechaExpiracion: futuro
        },
        {
            uid: "cliente-vinculacion-caducada-test",
            idCliente: 32,
            codigo: "codigo-caducado-test",
            estado: "PENDIENTE",
            fechaExpiracion: caducado
        },
        {
            uid: "cliente-vinculacion-valida-test",
            idCliente: 33,
            codigo: "codigo-valido-test",
            estado: "PENDIENTE",
            fechaExpiracion: futuro
        }
    ];

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        for (const caso of casos) {
            await setDoc(doc(database, "usuarios", caso.uid), {
                rol: "CLIENTE",
                activo: true,
                clienteId: null,
                negocioId: null
            });

            await setDoc(doc(database, "clientes", String(caso.idCliente)), {
                idCliente: caso.idCliente,
                firebaseUid: null,
                negocioId: null,
                codigoVinculacion: caso.codigo
            });

            await setDoc(doc(database, "vinculaciones", caso.codigo), {
                estado: caso.estado,
                fechaExpiracion: caso.fechaExpiracion,
                clienteId: caso.idCliente,
                negocioId: "negocio-vinculacion"
            });
        }
    });

    const intentarVinculacion = (caso) => {
        const database = testEnvironment
            .authenticatedContext(caso.uid)
            .firestore();

        const batch = writeBatch(database);

        batch.update(
            doc(database, "usuarios", caso.uid),
            {
                clienteId: caso.idCliente,
                negocioId: "negocio-vinculacion"
            }
        );

        batch.update(
            doc(database, "clientes", String(caso.idCliente)),
            {
                firebaseUid: caso.uid,
                negocioId: "negocio-vinculacion"
            }
        );

        batch.update(
            doc(database, "vinculaciones", caso.codigo),
            {
                estado: "USADA"
            }
        );

        return batch.commit();
    };

    await assertFails(
        intentarVinculacion(casos[0])
    );

    await assertFails(
        intentarVinculacion(casos[1])
    );

    await assertSucceeds(
        intentarVinculacion(casos[2])
    );

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();
        const usuario = await getDoc(
            doc(database, "usuarios", casos[2].uid)
        );
        const cliente = await getDoc(
            doc(database, "clientes", String(casos[2].idCliente))
        );
        const vinculacion = await getDoc(
            doc(database, "vinculaciones", casos[2].codigo)
        );

        assert.ok(usuario.exists());
        assert.ok(cliente.exists());
        assert.ok(vinculacion.exists());

        const usuarioData = usuario.data();
        const clienteData = cliente.data();
        const vinculacionData = vinculacion.data();

        assert.strictEqual(usuarioData.rol, "CLIENTE");
        assert.strictEqual(usuarioData.activo, true);
        assert.strictEqual(usuarioData.clienteId, casos[2].idCliente);
        assert.strictEqual(usuarioData.negocioId, "negocio-vinculacion");

        assert.strictEqual(clienteData.idCliente, casos[2].idCliente);
        assert.strictEqual(clienteData.firebaseUid, casos[2].uid);
        assert.strictEqual(clienteData.negocioId, "negocio-vinculacion");
        assert.strictEqual(clienteData.codigoVinculacion, casos[2].codigo);

        assert.strictEqual(vinculacionData.estado, "USADA");
        assert.strictEqual(vinculacionData.clienteId, casos[2].idCliente);
        assert.strictEqual(vinculacionData.negocioId, "negocio-vinculacion");
        assert.strictEqual(
            vinculacionData.fechaExpiracion.toMillis(),
            casos[2].fechaExpiracion.toMillis()
        );
        assert.ok(
            vinculacionData.fechaExpiracion.toMillis() > Timestamp.now().toMillis()
        );
    });
});

test("PRUEBA 6: un CLIENTE vinculado solo puede acceder a sus propios datos", async () => {
    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", "cliente-propio-test"), {
            rol: "CLIENTE",
            activo: true,
            clienteId: 10,
            negocioId: "negocio-a"
        });

        await setDoc(doc(database, "usuarios", "cliente-inactivo-test"), {
            rol: "CLIENTE",
            activo: false,
            clienteId: 30,
            negocioId: "negocio-a"
        });

        await setDoc(doc(database, "clientes", "10"), {
            idCliente: 10,
            firebaseUid: "cliente-propio-test",
            negocioId: "negocio-a",
            estado: "ACTIVO",
            fechaAlta: 1,
            fechaBaja: null,
            fechaInicioActual: 1,
            fechaFinActual: 2,
            serviciosContratados: ["Servicio A"],
            codigoVinculacion: null,
            nombre: "Cliente",
            apellidos: "Propio",
            dni: "11111111A",
            telefono: "600000000",
            email: "cliente@test.com",
            foto: "",
            fechaNacimiento: 0,
            observaciones: null
        });

        await setDoc(doc(database, "clientes", "20"), {
            idCliente: 20,
            firebaseUid: "cliente-otro-test",
            negocioId: "negocio-a",
            estado: "ACTIVO",
            fechaAlta: 1,
            fechaBaja: null,
            fechaInicioActual: 1,
            fechaFinActual: 2,
            serviciosContratados: ["Servicio B"],
            codigoVinculacion: null,
            nombre: "Otro",
            apellidos: "Cliente",
            dni: "22222222B",
            telefono: "611111111",
            email: "otro@test.com",
            foto: "",
            fechaNacimiento: 0,
            observaciones: null
        });

        await setDoc(doc(database, "clientes", "30"), {
            idCliente: 30,
            firebaseUid: "cliente-inactivo-test",
            negocioId: "negocio-a",
            estado: "ACTIVO",
            fechaAlta: 1,
            fechaBaja: null,
            fechaInicioActual: 1,
            fechaFinActual: 2,
            serviciosContratados: ["Servicio C"],
            codigoVinculacion: null,
            nombre: "Cliente inactivo",
            apellidos: "Prueba",
            dni: "33333333C",
            telefono: "622222222",
            email: "inactivo@test.com",
            foto: "",
            fechaNacimiento: 0,
            observaciones: null
        });
    });

    const database = testEnvironment
        .authenticatedContext("cliente-propio-test")
        .firestore();

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
        updateDoc(
            doc(database, "clientes", "20"),
            { nombre: "Intento de modificación" }
        )
    );

    const camposProtegidos = [
        { firebaseUid: "uid-alterado" },
        { negocioId: "otro-negocio" },
        { idCliente: 99 },
        { estado: "BAJA" },
        { fechaAlta: 3 },
        { fechaBaja: 4 },
        { fechaInicioActual: 3 },
        { fechaFinActual: 4 },
        { serviciosContratados: ["Servicio no contratado"] },
        { codigoVinculacion: "codigo-alterado" }
    ];

    for (const cambios of camposProtegidos) {
        await assertFails(
            updateDoc(
                doc(database, "clientes", "10"),
                cambios
            )
        );
    }

    await assertSucceeds(
        updateDoc(
            doc(database, "clientes", "10"),
            { nombre: "Nombre actualizado" }
        )
    );

    const databaseClienteInactivo = testEnvironment
        .authenticatedContext("cliente-inactivo-test")
        .firestore();

    await assertFails(
        getDoc(doc(databaseClienteInactivo, "clientes", "30"))
    );
});

test("PRUEBA 7: un CLIENTE solo puede usar sesiones de servicios contratados", async () => {
    const clienteUid = "cliente-servicios-test";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", clienteUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: 10,
            negocioId: "negocio-a"
        });

        await setDoc(doc(database, "clientes", "10"), {
            idCliente: 10,
            firebaseUid: clienteUid,
            negocioId: "negocio-a",
            serviciosContratados: ["yoga"]
        });

        await setDoc(doc(database, "clases", "clase-yoga"), {
            negocioId: "negocio-a",
            servicio: "yoga"
        });

        await setDoc(doc(database, "sesiones", "sesion-yoga"), {
            sesionId: 201,
            idClase: 1,
            negocioId: "negocio-a",
            servicio: "yoga",
            fecha: 1,
            plazasDisponibles: 10,
            clientesPermitidos: [clienteUid]
        });

        await setDoc(doc(database, "sesiones", "sesion-spinning"), {
            sesionId: 202,
            idClase: 2,
            negocioId: "negocio-a",
            servicio: "spinning",
            fecha: 1,
            plazasDisponibles: 10,
            clientesPermitidos: [clienteUid]
        });

        await setDoc(doc(database, "sesiones", "sesion-yoga-otro-cliente"), {
            sesionId: 203,
            idClase: 1,
            negocioId: "negocio-a",
            servicio: "yoga",
            fecha: 1,
            plazasDisponibles: 10,
            clientesPermitidos: ["otro-cliente-test"]
        });

        await setDoc(doc(database, "sesiones", "sesion-otro-negocio"), {
            sesionId: 204,
            idClase: 1,
            negocioId: "negocio-b",
            servicio: "yoga",
            fecha: 1,
            plazasDisponibles: 10,
            clientesPermitidos: [clienteUid]
        });

        await setDoc(doc(database, "sesiones", "101"), {
            sesionId: 101,
            idClase: 1,
            negocioId: "negocio-a",
            servicio: "yoga",
            fecha: 1,
            plazasDisponibles: 10,
            clientesPermitidos: [clienteUid]
        });

        await setDoc(doc(database, "sesiones", "102"), {
            sesionId: 102,
            idClase: 2,
            negocioId: "negocio-a",
            servicio: "spinning",
            fecha: 1,
            plazasDisponibles: 10,
            clientesPermitidos: [clienteUid]
        });
    });

    const database = testEnvironment
        .authenticatedContext(clienteUid)
        .firestore();

    await assertSucceeds(
        getDoc(doc(database, "sesiones", "sesion-yoga"))
    );

    await assertFails(
        getDoc(doc(database, "sesiones", "sesion-spinning"))
    );

    await assertFails(
        getDoc(doc(database, "sesiones", "sesion-yoga-otro-cliente"))
    );

    await assertFails(
        getDoc(doc(database, "sesiones", "sesion-otro-negocio"))
    );

    await assertSucceeds(
        getDocs(
            query(
                collection(database, "sesiones"),
                where("negocioId", "==", "negocio-a"),
                where("clientesPermitidos", "array-contains", clienteUid),
                where("servicio", "in", ["yoga"])
            )
        )
    );

    await assertFails(
        getDocs(
            query(
                collection(database, "sesiones"),
                where("negocioId", "==", "negocio-a"),
                where("clientesPermitidos", "array-contains", clienteUid),
                where("servicio", "in", ["spinning"])
            )
        )
    );

    await assertSucceeds(
        setDoc(doc(database, "reservas", "reserva-yoga"), {
            negocioId: "negocio-a",
            clienteId: 10,
            sesionId: 101,
            fechaReserva: 1
        })
    );

    await assertFails(
        setDoc(doc(database, "reservas", "reserva-spinning"), {
            negocioId: "negocio-a",
            clienteId: 10,
            sesionId: 102,
            fechaReserva: 1
        })
    );

    await assertFails(
        setDoc(doc(database, "clases", "clase-nueva"), {
            negocioId: "negocio-a",
            servicio: "yoga"
        })
    );

    await assertFails(
        updateDoc(
            doc(database, "clases", "clase-yoga"),
            { servicio: "spinning" }
        )
    );

    await assertFails(
        deleteDoc(doc(database, "clases", "clase-yoga"))
    );

    await assertFails(
        setDoc(doc(database, "sesiones", "sesion-nueva"), {
            negocioId: "negocio-a",
            servicio: "yoga",
            clientesPermitidos: [clienteUid]
        })
    );

    await assertFails(
        updateDoc(
            doc(database, "sesiones", "sesion-yoga"),
            { servicio: "spinning" }
        )
    );

    await assertFails(
        deleteDoc(doc(database, "sesiones", "sesion-yoga"))
    );
});

test("PRUEBA 8: un ADMIN solo puede escribir dentro de su negocio", async () => {
    const adminUid = "admin-negocio-a";
    const adminInactivoUid = "admin-inactivo-test";

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();
        const fechaExpiracion = Timestamp.fromMillis(
            Date.now() + 60 * 60 * 1000
        );

        await setDoc(doc(database, "usuarios", adminUid), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId: "negocio-a"
        });

        await setDoc(doc(database, "usuarios", adminInactivoUid), {
            rol: "ADMIN",
            activo: false,
            clienteId: null,
            negocioId: "negocio-a"
        });

        await setDoc(doc(database, "negocios", "negocio-a"), {
            adminUid,
            nombre: "Negocio A"
        });

        await setDoc(doc(database, "negocios", "negocio-b"), {
            adminUid: "admin-negocio-b",
            nombre: "Negocio B"
        });

        await setDoc(doc(database, "clases", "clase-a"), {
            negocioId: "negocio-a",
            nombre: "Clase A",
            servicio: "yoga"
        });

        await setDoc(doc(database, "clases", "clase-b"), {
            negocioId: "negocio-b",
            nombre: "Clase B",
            servicio: "yoga"
        });

        await setDoc(doc(database, "sesiones", "301"), {
            sesionId: 301,
            idClase: 1,
            negocioId: "negocio-a",
            servicio: "yoga",
            fecha: 1,
            plazasDisponibles: 10,
            clientesPermitidos: []
        });

        await setDoc(doc(database, "sesiones", "302"), {
            sesionId: 302,
            idClase: 2,
            negocioId: "negocio-b",
            servicio: "yoga",
            fecha: 1,
            plazasDisponibles: 10,
            clientesPermitidos: []
        });

        await setDoc(doc(database, "reservas", "reserva-a"), {
            negocioId: "negocio-a",
            clienteId: 10,
            sesionId: 301,
            fechaReserva: 1
        });

        await setDoc(doc(database, "reservas", "reserva-b"), {
            negocioId: "negocio-b",
            clienteId: 20,
            sesionId: 302,
            fechaReserva: 1
        });

        await setDoc(doc(database, "clientes", "cliente-a"), {
            idCliente: 10,
            firebaseUid: "cliente-a",
            negocioId: "negocio-a",
            serviciosContratados: []
        });

        await setDoc(doc(database, "clientes", "cliente-b"), {
            idCliente: 20,
            firebaseUid: "cliente-b",
            negocioId: "negocio-b",
            serviciosContratados: []
        });

        await setDoc(doc(database, "movimientos", "movimiento-a"), {
            negocioId: "negocio-a",
            servicio: "yoga",
            importe: 50
        });

        await setDoc(doc(database, "movimientos", "movimiento-b"), {
            negocioId: "negocio-b",
            servicio: "yoga",
            importe: 50
        });

        await setDoc(doc(database, "gastos", "gasto-a"), {
            negocioId: "negocio-a",
            concepto: "Alquiler",
            importe: 100
        });

        await setDoc(doc(database, "vinculaciones", "codigo-a"), {
            negocioId: "negocio-a",
            estado: "PENDIENTE",
            fechaExpiracion,
            clienteId: 10
        });

        await setDoc(doc(database, "vinculaciones", "codigo-b"), {
            negocioId: "negocio-b",
            estado: "PENDIENTE",
            fechaExpiracion,
            clienteId: 20
        });
    });

    const database = testEnvironment
        .authenticatedContext(adminUid)
        .firestore();

    await assertSucceeds(
        setDoc(doc(database, "clases", "clase-a-nueva"), {
            negocioId: "negocio-a",
            nombre: "Clase nueva A",
            servicio: "yoga"
        })
    );

    await assertSucceeds(
        updateDoc(
            doc(database, "clases", "clase-a"),
            { nombre: "Clase A modificada" }
        )
    );

    await assertSucceeds(
        deleteDoc(doc(database, "clases", "clase-a-nueva"))
    );

    await assertFails(
        setDoc(doc(database, "clases", "clase-b-nueva"), {
            negocioId: "negocio-b",
            nombre: "Clase nueva B",
            servicio: "yoga"
        })
    );

    await assertFails(
        updateDoc(
            doc(database, "clases", "clase-b"),
            { nombre: "Intento clase B" }
        )
    );

    await assertFails(
        deleteDoc(doc(database, "clases", "clase-b"))
    );

    await assertSucceeds(
        setDoc(doc(database, "sesiones", "sesion-a-nueva"), {
            sesionId: 303,
            idClase: 1,
            negocioId: "negocio-a",
            servicio: "yoga",
            fecha: 1,
            plazasDisponibles: 10,
            clientesPermitidos: []
        })
    );

    await assertSucceeds(
        updateDoc(
            doc(database, "sesiones", "301"),
            { plazasDisponibles: 9 }
        )
    );

    await assertSucceeds(
        deleteDoc(doc(database, "sesiones", "sesion-a-nueva"))
    );

    await assertFails(
        setDoc(doc(database, "sesiones", "sesion-b-nueva"), {
            sesionId: 304,
            idClase: 2,
            negocioId: "negocio-b",
            servicio: "yoga",
            fecha: 1,
            plazasDisponibles: 10,
            clientesPermitidos: []
        })
    );

    await assertFails(
        updateDoc(
            doc(database, "sesiones", "302"),
            { plazasDisponibles: 9 }
        )
    );

    await assertFails(
        deleteDoc(doc(database, "sesiones", "302"))
    );

    await assertSucceeds(
        setDoc(doc(database, "movimientos", "movimiento-a-nuevo"), {
            negocioId: "negocio-a",
            servicio: "yoga",
            importe: 60
        })
    );

    await assertSucceeds(
        updateDoc(
            doc(database, "movimientos", "movimiento-a"),
            { importe: 55 }
        )
    );

    await assertSucceeds(
        deleteDoc(doc(database, "movimientos", "movimiento-a-nuevo"))
    );

    await assertFails(
        setDoc(doc(database, "movimientos", "movimiento-b-nuevo"), {
            negocioId: "negocio-b",
            servicio: "yoga",
            importe: 60
        })
    );

    await assertFails(
        updateDoc(
            doc(database, "movimientos", "movimiento-b"),
            { importe: 55 }
        )
    );

    await assertFails(
        deleteDoc(doc(database, "movimientos", "movimiento-b"))
    );

    await assertSucceeds(
        setDoc(doc(database, "clientes", "cliente-a-nuevo"), {
            idCliente: 11,
            firebaseUid: "cliente-a-nuevo",
            negocioId: "negocio-a",
            serviciosContratados: []
        })
    );

    await assertSucceeds(
        updateDoc(
            doc(database, "clientes", "cliente-a"),
            { nombre: "Cliente A modificado" }
        )
    );

    await assertFails(
        deleteDoc(doc(database, "clientes", "cliente-a"))
    );

    await assertFails(
        setDoc(doc(database, "clientes", "cliente-b-nuevo"), {
            idCliente: 21,
            firebaseUid: "cliente-b-nuevo",
            negocioId: "negocio-b",
            serviciosContratados: []
        })
    );

    await assertFails(
        updateDoc(
            doc(database, "clientes", "cliente-b"),
            { nombre: "Intento cliente B" }
        )
    );

    await assertFails(
        deleteDoc(doc(database, "clientes", "cliente-b"))
    );

    await assertSucceeds(
        getDoc(doc(database, "negocios", "negocio-a"))
    );

    await assertFails(
        getDoc(doc(database, "negocios", "negocio-b"))
    );

    await assertSucceeds(
        updateDoc(
            doc(database, "negocios", "negocio-a"),
            { nombre: "Negocio A modificado" }
        )
    );

    await assertFails(
        updateDoc(
            doc(database, "negocios", "negocio-b"),
            { nombre: "Intento negocio B" }
        )
    );

    await assertFails(
        deleteDoc(doc(database, "negocios", "negocio-a"))
    );

    await assertFails(
        setDoc(doc(database, "negocios", "negocio-segundo"), {
            adminUid
        })
    );

    await assertSucceeds(
        setDoc(doc(database, "reservas", "reserva-a-nueva"), {
            negocioId: "negocio-a",
            clienteId: 10,
            sesionId: 301,
            fechaReserva: 1
        })
    );

    await assertSucceeds(
        updateDoc(
            doc(database, "reservas", "reserva-a"),
            { fechaReserva: 2 }
        )
    );

    await assertSucceeds(
        deleteDoc(doc(database, "reservas", "reserva-a-nueva"))
    );

    await assertFails(
        setDoc(doc(database, "reservas", "reserva-b-nueva"), {
            negocioId: "negocio-b",
            clienteId: 20,
            sesionId: 302,
            fechaReserva: 1
        })
    );

    await assertFails(
        updateDoc(
            doc(database, "reservas", "reserva-b"),
            { fechaReserva: 2 }
        )
    );

    await assertFails(
        deleteDoc(doc(database, "reservas", "reserva-b"))
    );

    await assertFails(
        getDoc(doc(database, "gastos", "gasto-a"))
    );

    await assertFails(
        setDoc(doc(database, "gastos", "gasto-a-nuevo"), {
            negocioId: "negocio-a",
            concepto: "Intento gasto",
            importe: 20
        })
    );

    await assertSucceeds(
        setDoc(doc(database, "vinculaciones", "codigo-a-nuevo"), {
            negocioId: "negocio-a",
            estado: "PENDIENTE",
            fechaExpiracion: Timestamp.fromMillis(
                Date.now() + 60 * 60 * 1000
            ),
            clienteId: 11
        })
    );

    // ADMIN no puede consumir vinculaciones; PENDIENTE -> USADA corresponde al CLIENTE mediante Batch.
    await assertFails(
        updateDoc(
            doc(database, "vinculaciones", "codigo-a"),
            { estado: "USADA" }
        )
    );

    await assertSucceeds(
        deleteDoc(doc(database, "vinculaciones", "codigo-a-nuevo"))
    );

    await assertFails(
        setDoc(doc(database, "vinculaciones", "codigo-b-nuevo"), {
            negocioId: "negocio-b",
            estado: "PENDIENTE",
            fechaExpiracion: Timestamp.fromMillis(
                Date.now() + 60 * 60 * 1000
            ),
            clienteId: 21
        })
    );

    await assertFails(
        updateDoc(
            doc(database, "vinculaciones", "codigo-b"),
            { estado: "USADA" }
        )
    );

    await assertFails(
        deleteDoc(doc(database, "vinculaciones", "codigo-b"))
    );

    const databaseInactivo = testEnvironment
        .authenticatedContext(adminInactivoUid)
        .firestore();

    await assertFails(
        setDoc(doc(databaseInactivo, "clases", "clase-inactivo"), {
            negocioId: "negocio-a",
            servicio: "yoga"
        })
    );

    await assertFails(
        setDoc(doc(databaseInactivo, "sesiones", "sesion-inactivo"), {
            sesionId: 305,
            negocioId: "negocio-a",
            servicio: "yoga",
            clientesPermitidos: []
        })
    );

    await assertFails(
        setDoc(doc(databaseInactivo, "movimientos", "movimiento-inactivo"), {
            negocioId: "negocio-a",
            importe: 10
        })
    );

    await assertFails(
        setDoc(doc(databaseInactivo, "clientes", "cliente-inactivo"), {
            idCliente: 30,
            negocioId: "negocio-a"
        })
    );

    await assertFails(
        updateDoc(
            doc(databaseInactivo, "negocios", "negocio-a"),
            { nombre: "Intento inactivo" }
        )
    );

    await assertFails(
        setDoc(doc(databaseInactivo, "gastos", "gasto-inactivo"), {
            negocioId: "negocio-a",
            importe: 10
        })
    );

    await assertFails(
        setDoc(doc(databaseInactivo, "vinculaciones", "codigo-inactivo"), {
            negocioId: "negocio-a",
            estado: "PENDIENTE",
            fechaExpiracion: Timestamp.fromMillis(
                Date.now() + 60 * 60 * 1000
            ),
            clienteId: 30
        })
    );
});

test("PRUEBA 9: ciclo de vida seguro de las vinculaciones", async () => {
    const adminUid = "admin-vinculaciones-test";
    const otroAdminUid = "admin-vinculaciones-otro-test";
    const adminInactivoUid = "admin-vinculaciones-inactivo-test";
    const clienteUid = "cliente-vinculacion-9-test";
    const clienteUsadaUid = "cliente-vinculacion-usada-9-test";
    const clienteCaducadaUid = "cliente-vinculacion-caducada-9-test";
    const negocioId = "negocio-vinculaciones-a-9";
    const otroNegocioId = "negocio-vinculaciones-b-9";
    const fechaFutura = Timestamp.fromMillis(
        Date.now() + 60 * 60 * 1000
    );
    const otraFechaFutura = Timestamp.fromMillis(
        Date.now() + 2 * 60 * 60 * 1000
    );
    const fechaPasada = Timestamp.fromMillis(
        Date.now() - 60 * 60 * 1000
    );

    await testEnvironment.withSecurityRulesDisabled(async (context) => {
        const database = context.firestore();

        await setDoc(doc(database, "usuarios", adminUid), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId
        });

        await setDoc(doc(database, "usuarios", otroAdminUid), {
            rol: "ADMIN",
            activo: true,
            clienteId: null,
            negocioId: otroNegocioId
        });

        await setDoc(doc(database, "usuarios", adminInactivoUid), {
            rol: "ADMIN",
            activo: false,
            clienteId: null,
            negocioId
        });

        await setDoc(doc(database, "usuarios", clienteUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });

        await setDoc(doc(database, "usuarios", clienteUsadaUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });

        await setDoc(doc(database, "usuarios", clienteCaducadaUid), {
            rol: "CLIENTE",
            activo: true,
            clienteId: null,
            negocioId: null
        });

        await setDoc(doc(database, "clientes", "901"), {
            idCliente: 901,
            firebaseUid: null,
            negocioId: null,
            codigoVinculacion: "codigo-consumo-9"
        });

        await setDoc(doc(database, "clientes", "902"), {
            idCliente: 902,
            firebaseUid: null,
            negocioId: null,
            codigoVinculacion: "codigo-usada-9"
        });

        await setDoc(doc(database, "clientes", "903"), {
            idCliente: 903,
            firebaseUid: null,
            negocioId: null,
            codigoVinculacion: "codigo-caducada-9"
        });

        await setDoc(doc(database, "vinculaciones", "codigo-pendiente-9"), {
            negocioId,
            estado: "PENDIENTE",
            fechaExpiracion: fechaFutura,
            clienteId: 904
        });

        await setDoc(doc(database, "vinculaciones", "codigo-pendiente-borrar-9"), {
            negocioId,
            estado: "PENDIENTE",
            fechaExpiracion: fechaFutura,
            clienteId: 905
        });

        await setDoc(doc(database, "vinculaciones", "codigo-consumo-9"), {
            negocioId,
            estado: "PENDIENTE",
            fechaExpiracion: fechaFutura,
            clienteId: 901
        });

        await setDoc(doc(database, "vinculaciones", "codigo-usada-9"), {
            negocioId,
            estado: "USADA",
            fechaExpiracion: fechaFutura,
            clienteId: 902
        });

        await setDoc(doc(database, "vinculaciones", "codigo-caducada-9"), {
            negocioId,
            estado: "PENDIENTE",
            fechaExpiracion: fechaPasada,
            clienteId: 903
        });

        await setDoc(doc(database, "vinculaciones", "codigo-admin-otro-9"), {
            negocioId,
            estado: "PENDIENTE",
            fechaExpiracion: fechaFutura,
            clienteId: 906
        });
    });

    const databaseAdmin = testEnvironment
        .authenticatedContext(adminUid)
        .firestore();

    const databaseOtroAdmin = testEnvironment
        .authenticatedContext(otroAdminUid)
        .firestore();

    const databaseAdminInactivo = testEnvironment
        .authenticatedContext(adminInactivoUid)
        .firestore();

    const databaseCliente = testEnvironment
        .authenticatedContext(clienteUid)
        .firestore();

    const databaseClienteUsada = testEnvironment
        .authenticatedContext(clienteUsadaUid)
        .firestore();

    const databaseClienteCaducada = testEnvironment
        .authenticatedContext(clienteCaducadaUid)
        .firestore();

    await assertSucceeds(
        setDoc(doc(databaseAdmin, "vinculaciones", "codigo-creado-9"), {
            negocioId,
            estado: "PENDIENTE",
            fechaExpiracion: fechaFutura,
            clienteId: 907
        })
    );

    await assertFails(
        setDoc(doc(databaseAdmin, "vinculaciones", "codigo-otro-negocio-9"), {
            negocioId: otroNegocioId,
            estado: "PENDIENTE",
            fechaExpiracion: fechaFutura,
            clienteId: 908
        })
    );

    await assertFails(
        setDoc(doc(databaseAdminInactivo, "vinculaciones", "codigo-inactivo-9"), {
            negocioId,
            estado: "PENDIENTE",
            fechaExpiracion: fechaFutura,
            clienteId: 909
        })
    );

    await assertFails(
        setDoc(doc(databaseAdmin, "vinculaciones", "codigo-sin-cliente-9"), {
            negocioId,
            estado: "PENDIENTE",
            fechaExpiracion: fechaFutura
        })
    );

    await assertFails(
        setDoc(doc(databaseAdmin, "vinculaciones", "codigo-cliente-string-9"), {
            negocioId,
            estado: "PENDIENTE",
            fechaExpiracion: fechaFutura,
            clienteId: "910"
        })
    );

    await assertFails(
        setDoc(doc(databaseAdmin, "vinculaciones", "codigo-sin-fecha-9"), {
            negocioId,
            estado: "PENDIENTE",
            clienteId: 911
        })
    );

    await assertFails(
        setDoc(doc(databaseAdmin, "vinculaciones", "codigo-caducado-9"), {
            negocioId,
            estado: "PENDIENTE",
            fechaExpiracion: fechaPasada,
            clienteId: 912
        })
    );

    await assertFails(
        setDoc(doc(databaseAdmin, "vinculaciones", "codigo-campo-extra-9"), {
            negocioId,
            estado: "PENDIENTE",
            fechaExpiracion: fechaFutura,
            clienteId: 913,
            campoExtra: true
        })
    );

    await assertSucceeds(
        updateDoc(
            doc(databaseAdmin, "vinculaciones", "codigo-pendiente-9"),
            { fechaExpiracion: otraFechaFutura }
        )
    );

    await assertFails(
        updateDoc(
            doc(databaseAdmin, "vinculaciones", "codigo-pendiente-9"),
            { clienteId: 914 }
        )
    );

    await assertFails(
        updateDoc(
            doc(databaseAdmin, "vinculaciones", "codigo-pendiente-9"),
            { negocioId: otroNegocioId }
        )
    );

    await assertFails(
        updateDoc(
            doc(databaseAdmin, "vinculaciones", "codigo-pendiente-9"),
            { estado: "USADA" }
        )
    );

    await assertFails(
        updateDoc(
            doc(databaseAdmin, "vinculaciones", "codigo-usada-9"),
            { fechaExpiracion: otraFechaFutura }
        )
    );

    await assertFails(
        updateDoc(
            doc(databaseAdmin, "vinculaciones", "codigo-usada-9"),
            { estado: "PENDIENTE" }
        )
    );

    await assertSucceeds(
        deleteDoc(
            doc(databaseAdmin, "vinculaciones", "codigo-pendiente-borrar-9")
        )
    );

    await assertFails(
        deleteDoc(doc(databaseAdmin, "vinculaciones", "codigo-usada-9"))
    );

    const batchCliente = writeBatch(databaseCliente);
    batchCliente.update(doc(databaseCliente, "usuarios", clienteUid), {
        clienteId: 901,
        negocioId
    });
    batchCliente.update(doc(databaseCliente, "clientes", "901"), {
        firebaseUid: clienteUid,
        negocioId
    });
    batchCliente.update(
        doc(databaseCliente, "vinculaciones", "codigo-consumo-9"),
        { estado: "USADA" }
    );

    await assertSucceeds(batchCliente.commit());

    const batchClienteUsada = writeBatch(databaseClienteUsada);
    batchClienteUsada.update(
        doc(databaseClienteUsada, "usuarios", clienteUsadaUid),
        {
            clienteId: 902,
            negocioId
        }
    );
    batchClienteUsada.update(
        doc(databaseClienteUsada, "clientes", "902"),
        {
            firebaseUid: clienteUsadaUid,
            negocioId
        }
    );
    batchClienteUsada.update(
        doc(databaseClienteUsada, "vinculaciones", "codigo-usada-9"),
        { estado: "USADA" }
    );

    await assertFails(batchClienteUsada.commit());

    const batchClienteCaducada = writeBatch(databaseClienteCaducada);
    batchClienteCaducada.update(
        doc(databaseClienteCaducada, "usuarios", clienteCaducadaUid),
        {
            clienteId: 903,
            negocioId
        }
    );
    batchClienteCaducada.update(
        doc(databaseClienteCaducada, "clientes", "903"),
        {
            firebaseUid: clienteCaducadaUid,
            negocioId
        }
    );
    batchClienteCaducada.update(
        doc(databaseClienteCaducada, "vinculaciones", "codigo-caducada-9"),
        { estado: "USADA" }
    );

    await assertFails(batchClienteCaducada.commit());

    await assertFails(
        updateDoc(
            doc(databaseOtroAdmin, "vinculaciones", "codigo-admin-otro-9"),
            { fechaExpiracion: otraFechaFutura }
        )
    );

    await assertFails(
        deleteDoc(
            doc(databaseOtroAdmin, "vinculaciones", "codigo-admin-otro-9")
        )
    );
});
