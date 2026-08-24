const fs = require("node:fs");
const path = require("node:path");
const { spawnSync } = require("node:child_process");

const testDirectory = __dirname;
const sourceRules = path.resolve(testDirectory, "..", "firestore.rules");
const generatedRules = path.resolve(
    testDirectory,
    "firestore.rules.sessions-query.generated"
);
const generatedConfig = path.resolve(
    testDirectory,
    "firebase.sessions-query.generated.json"
);
const emulatorLog = path.resolve(testDirectory, "firestore-debug.log");
const firebaseBinary = path.resolve(
    testDirectory,
    "node_modules",
    ".bin",
    process.platform === "win32" ? "firebase.cmd" : "firebase"
);

const helper = `
    function servicioContratadoPorCliente(servicio) {
      return usuarioActual().clienteId is int
        && servicio is string
        && get(
          /databases/$(database)/documents/clientes/$(usuarioActual().clienteId)
        ).data.serviciosContratados is list
        && servicio in get(
          /databases/$(database)/documents/clientes/$(usuarioActual().clienteId)
        ).data.serviciosContratados;
    }
`;

const sessionRule = `      allow get, list: if esCliente()
        && resource.data.negocioId == usuarioActual().negocioId
        && request.auth.uid in resource.data.clientesPermitidos;`;

const sessionRuleWithService = `      allow get, list: if esCliente()
        && resource.data.negocioId == usuarioActual().negocioId
        && request.auth.uid in resource.data.clientesPermitidos
        && servicioContratadoPorCliente(resource.data.servicio);`;

let exitCode = 1;
let rulesCreated = false;
let configCreated = false;

try {
    let rules = fs.readFileSync(sourceRules, "utf8").replace(/\r\n/g, "\n");

    if (rules.split(sessionRule).length !== 2) {
        throw new Error("No se encontro exactamente la regla de sesiones esperada.");
    }

    const helperMarker = "    function sesionDelNegocio(sesionId) {";
    if (rules.split(helperMarker).length !== 2) {
        throw new Error("No se encontro el punto de insercion de la funcion auxiliar.");
    }

    rules = rules.replace(sessionRule, sessionRuleWithService);
    rules = rules.replace(helperMarker, `${helper}${helperMarker}`);

    fs.writeFileSync(generatedRules, rules, "utf8");
    rulesCreated = true;

    fs.writeFileSync(
        generatedConfig,
        JSON.stringify(
            {
                firestore: {
                    rules: "firestore.rules.sessions-query.generated"
                },
                emulators: {
                    firestore: {
                        port: 8080
                    }
                }
            },
            null,
            2
        ),
        "utf8"
    );
    configCreated = true;

    const command = [
        `"${firebaseBinary}"`,
        "emulators:exec",
        "--config firebase.sessions-query.generated.json",
        "--project gestorpro-rules-test",
        "--only firestore",
        '"node --test sessions-query-compatibility.test.cjs"'
    ].join(" ");

    const result = spawnSync(command, {
        cwd: testDirectory,
        shell: true,
        stdio: "inherit"
    });

    if (result.error) {
        console.error(result.error);
    } else {
        exitCode = result.status ?? 1;
    }
} finally {
    if (configCreated) {
        fs.rmSync(generatedConfig, { force: true });
    }

    if (rulesCreated) {
        fs.rmSync(generatedRules, { force: true });
    }

    fs.rmSync(emulatorLog, { force: true });
}

process.exitCode = exitCode;
