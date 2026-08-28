const fs = require("node:fs");
const path = require("node:path");
const { spawnSync } = require("node:child_process");

const testDirectory = __dirname;
const sourceRules = path.resolve(testDirectory, "..", "firestore.rules");
const generatedRules = path.resolve(testDirectory, "firestore.rules.generated");
const sourceStorageRules = path.resolve(testDirectory, "..", "storage.rules");
const generatedStorageRules = path.resolve(testDirectory, "storage.rules.generated");
const firebaseBinary = path.resolve(
    testDirectory,
    "node_modules",
    ".bin",
    process.platform === "win32" ? "firebase.cmd" : "firebase"
);

let exitCode = 1;
let rulesCreated = false;
let storageRulesCreated = false;

try {
    fs.copyFileSync(sourceRules, generatedRules);
    rulesCreated = true;
    fs.copyFileSync(sourceStorageRules, generatedStorageRules);
    storageRulesCreated = true;

    const command = [
        `"${firebaseBinary}"`,
        "emulators:exec",
        "--project gestorpro-rules-test",
        "--only firestore,storage",
        '"node --test firestore.rules.test.cjs"'
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
    if (rulesCreated) {
        fs.rmSync(generatedRules, { force: true });
    }
    if (storageRulesCreated) {
        fs.rmSync(generatedStorageRules, { force: true });
    }
}

process.exitCode = exitCode;
