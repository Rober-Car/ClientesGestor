# AGENTS.md - Contexto del proyecto GestorPro

Lee este archivo completo antes de modificar el proyecto.

## Proyecto

- **Nombre:** GestorPro
- **Descripción:** sistema Android para gestionar clientes, clases, reservas, cuotas, gastos y datos económicos de un negocio deportivo. Formado por **dos aplicaciones independientes** que comparten el mismo proyecto Firebase/Firestore:
  1. **GestorPro Admin** (`:app`) — gestión del negocio por el administrador.
  2. **GestorPro Cliente** (`:appCliente`) — registro, vinculación y perfil del cliente.
- **Plataforma:** Android.
- **Min SDK:** 26. **Target SDK:** 36. **Compile SDK:** 36.1.

### GestorPro Admin (`:app`)

- **Package:** `com.roberto.gestorpro`.
- **Application:** `GestorProApplication`.
- **Activity principal:** `MainActivity`.
- **Módulo:** `app`.

### GestorPro Cliente (`:appCliente`)

- **Package:** `com.roberto.gestorpro.cliente`.
- **Application:** `GestorProClienteApplication`.
- **Activity principal:** `MainActivity`.
- **Módulo:** `appCliente`.
- **google-services.json:** `appCliente/google-services.json` (no versionado; está en `.gitignore`).
- La app Cliente **no usa Room** (fuente de verdad = Firestore) ni Gson.

## Stack tecnológico

Las versiones oficiales del proyecto están en `gradle/libs.versions.toml`.

| Área | Tecnología | Versión configurada |
|---|---|---|
| Lenguaje | Kotlin | 2.2.10 |
| Android Gradle Plugin | AGP | 9.1.1 |
| UI | Jetpack Compose + Material 3 | BOM 2026.02.01 |
| Navegación | Navigation Compose | 2.9.3 |
| Inyección de dependencias | Hilt | 2.60.1 |
| Base de datos local (solo Admin) | Room | 2.8.4 |
| Preferencias | DataStore Preferences | 1.1.7 |
| Imágenes | Coil Compose | 3.3.0 |
| Serialización auxiliar (solo Admin) | Gson | 2.11.0 |
| Backend | Firebase Authentication + Firestore | Firebase BOM 34.16.0 |

Reglas relacionadas con dependencias:

- No añadir una dependencia sin avisar y explicar su necesidad.
- Comprobar primero si la funcionalidad ya está cubierta por una dependencia existente.
- Usar el catálogo `libs.versions.toml` para las nuevas dependencias cuando sea posible.
- Avisar antes de modificar `build.gradle.kts` o `gradle/libs.versions.toml`.
- No cambiar versiones por iniciativa propia.

## Arquitectura actual

GestorPro utiliza **MVVM con repositorios**, no una Clean Architecture estricta. La estructura vigente es:

```text
UI Compose -> ViewModel -> Repository -> Room / DataStore / Firebase
```

- En **Admin**, Room es la fuente de verdad local y Firestore el espejo remoto (réplica write-through).
- En **Cliente**, Firestore es la fuente de verdad; solo hay DataStore para preferencias locales.

No se debe exigir ni introducir una capa `domain/usecase` como requisito automático. Si una tarea necesita mejorar la arquitectura, debe proponerse primero y hacerse de forma incremental.

Reglas de arquitectura vigentes:

- La UI renderiza el estado y emite acciones del usuario.
- Los ViewModels coordinan el estado de pantalla y lanzan operaciones con `viewModelScope`.
- Los ViewModels acceden a datos mediante repositorios, no directamente mediante DAOs.
- Los DAOs y entidades Room pertenecen a `data`.
- Las clases de `model` contienen modelos compartidos de la aplicación.
- `AppModule` centraliza la configuración de Hilt, Room y repositorios (uno por aplicación).
- `ClientesDatabase` es la base de datos Room principal (solo Admin).
- `PreferencesRepository` encapsula DataStore (uno por aplicación).
- `MainActivity` es la única Activity de cada aplicación.
- `AppNavigation` y `Routes` centralizan la navegación (una pareja por aplicación).
- **La Vía B (enlace individual/deep link) está DESCARTADA.** No se implementa ni se reutiliza nada relacionado: sin `vinculaciones`, sin `codigoVinculacion`, sin deep links.

El código existente contiene deuda arquitectónica y no se debe refactorizar de forma masiva como parte de otra tarea. Si se detecta una inconsistencia, se informa y se propone por separado.

## Estructura principal

```text
app/                                     -> GestorPro Admin
└── src/main/java/com/roberto/gestorpro/
    ├── GestorProApplication.kt          -> Application de Hilt
    ├── MainActivity.kt                  -> única Activity y punto de entrada Compose
    ├── navigation/                      -> AppNavigation.kt y Routes.kt
    ├── model/                           -> modelos y enums
    ├── data/
    │   ├── converter/                   -> conversores de Room
    │   ├── dao/                         -> DAOs Room
    │   ├── database/                    -> ClientesDatabase
    │   ├── entity/                      -> entidades Room
    │   ├── export/                      -> exportación de datos
    │   ├── firebase/                    -> Firebase (Autenticacion, Negocio, ClienteRemoto)
    │   └── repository/                  -> repositorios de datos
    ├── di/AppModule.kt                  -> dependencias de Hilt
    └── ui/
        ├── auth/                        -> login y registro (ADMIN)
        ├── clases/                      -> clases, sesiones y reservas
        ├── clientes/                    -> clientes y perfiles
        ├── components/                  -> componentes Compose reutilizables
        ├── configuracion/               -> cuenta, negocio, preferencias y datos
        ├── economia/                    -> movimientos y gastos
        ├── home/                        -> inicio de administrador
        ├── theme/                       -> tema, colores y tipografía
        ├── utils/                       -> FotoUtils
        └── viewmodel/                   -> ViewModels

appCliente/                              -> GestorPro Cliente
└── src/main/java/com/roberto/gestorpro/cliente/
    ├── GestorProClienteApplication.kt   -> Application de Hilt
    ├── MainActivity.kt                  -> única Activity y punto de entrada Compose
    ├── navigation/                      -> AppNavigation.kt y Routes.kt
    ├── model/                           -> Cliente y EstadoCliente
    ├── data/
    │   ├── firebase/                    -> Autenticacion, Negocio, PerfilPendiente,
    │   │                                   Cliente, Vinculacion (VÍA 1 y VÍA 2)
    │   └── repository/                  -> PreferencesRepository (DataStore)
    ├── di/AppModule.kt                  -> dependencias de Hilt
    └── ui/
        ├── auth/                        -> login, registro, recuperar, inicio código+DNI,
        │                                   completar perfil, mi perfil, editar perfil, cuenta
        ├── components/                  -> MenuCard
        ├── home/                        -> inicio del cliente
        ├── theme/                       -> tema, colores y tipografía
        ├── utils/                       -> FotoUtils
        └── viewmodel/                   -> MainViewModel
```

## Funcionalidades principales

### GestorPro Admin
- Registro/login/logout reales con Firebase Authentication (rol `ADMIN` fijo).
- Creación y edición del negocio con código maestro (`negocios` + `negocios_publicos`).
- Alta, edición, consulta, archivado y restauración de clientes (Room + réplica Firestore con write-through).
- Réplica de clientes que crea `clientes/{idCliente}`, `indices_clientes/{negocio}_{dni}` y `clientes_privados/{idCliente}` en un único Batch.
- Cambio de DNI de un cliente manteniendo el índice atómico (borra el viejo y crea el nuevo en el mismo Batch).
- Gestión de clases, sesiones y reservas.
- Gestión de movimientos, cuotas y gastos.
- Configuración del negocio, logo, tema, cuenta y datos.
- Selección de foto de perfil desde galería o cámara.

### GestorPro Cliente
- Registro/login/logout reales con Firebase Authentication (rol `CLIENTE` fijo).
- Recuperación de contraseña (solo `FirebaseAuth.sendPasswordResetEmail`).
- Pantalla inicial "¿Tu gimnasio ya te ha registrado?" con código maestro + DNI.
- **VÍA 1:** vincular el UID a una ficha existente creada por el ADMIN (sin crear duplicados).
- **VÍA 2:** guardar perfil pendiente (`perfiles_pendientes/{uid}`) y crear la ficha + índice al introducir código maestro + DNI (si no existe).
- Ver y editar solo los datos personales de la propia ficha (nombre, apellidos, teléfono, email, foto, fecha de nacimiento).
- Nunca muestra ni edita `observaciones` ni datos administrativos.
- Persistencia local solo con DataStore.

## Contrato remoto de Firestore

Firestore es la capa remota compartida por ambas aplicaciones. En Admin, Room es la base de datos local y Firestore el espejo remoto (réplica write-through sin cola offline). En Cliente, Firestore es la fuente de verdad.

Colecciones remotas actuales:

```text
usuarios/{uid}
negocios/{negocioId}
negocios_publicos/{negocioId}
clientes/{idCliente}
clientes_privados/{idCliente}        <- observaciones y datos internos (solo ADMIN)
indices_clientes/{negocioId}_{dni}   <- unicidad y localización negocio+DNI
perfiles_pendientes/{uid}            <- perfil temporal del CLIENTE sin negocio
clases/{claseId}
sesiones/{sesionId}
reservas/{reservaId}
solicitudes/{solicitudId}
movimientos/{movimientoId}
```

**No existe** la colección `vinculaciones` (Vía B descartada) ni el campo `codigoVinculacion`.

Reglas de identidad y pertenencia:

- El UID de Firebase es el ID del documento `usuarios/{uid}`.
- Los roles remotos son exactamente `ADMIN` y `CLIENTE`.
- `negocioId` es un `String` en Firestore.
- `clienteId`, `idCliente` y `sesionId` se manejan como enteros (`int64`) cuando forman parte de los datos.
- Los documentos de `clientes` usan el identificador numérico convertido a texto en la ruta, por ejemplo `clientes/2`.
- `firebaseUid` es un `String`; en `clientes` nace `null` (ficha creada por el ADMIN) y solo lo rellena el CLIENTE al vincularse.
- `serviciosContratados` y `clientesPermitidos` son arrays de strings.
- Un administrador solo puede acceder a su negocio, identificado por `adminUid` y `negocioId`.
- Un cliente solo puede acceder a sus datos, sus reservas, sus solicitudes y las sesiones para las que esté autorizado.
- Los clientes nunca pueden acceder a `movimientos` ni a `clientes_privados`.

Estructura clave de documentos:

```text
usuarios/{uid} = { rol, activo, clienteId, negocioId }
clientes/{idCliente} = { idCliente, negocioId, firebaseUid, nombre, apellidos, dni,
                         telefono, email, foto, fechaNacimiento, fechaRegistro,
                         fechaAlta, fechaBaja, estado, tieneLlave,
                         serviciosContratados, fechaInicioActual, fechaFinActual }
                     (sin observaciones ni codigoVinculacion)
clientes_privados/{idCliente} = { negocioId, observaciones }
indices_clientes/{negocioId}_{dni} = { negocioId, dni, clienteId }
perfiles_pendientes/{uid} = VÍA 1: { dni, negocioId } | VÍA 2: { nombre, apellidos, dni, telefono, email, foto, fechaNacimiento }
```

Flujos funcionales remotos:

- Un ADMIN nuevo puede registrarse con `negocioId = null` y debe crear su propio negocio con código maestro.
- La creación del negocio, `negocios_publicos/{id}` y la asignación de `usuarios/{uid}.negocioId` deben ejecutarse en el mismo Batch.
- Las solicitudes solo representan altas y bajas. Sus valores remotos son `ALTA` y `BAJA`.
- Las clases definen servicios y horarios; las sesiones son instancias concretas; las reservas relacionan un cliente con una sesión mediante `sesionId`.
- Un cliente no solicita una clase mediante `solicitudes`; primero debe tener contratado el servicio y después puede reservar una sesión autorizada.

### Alta y vinculación del CLIENTE — dos vías

- **VÍA 1 — ADMIN crea primero:**
  1. ADMIN crea la ficha → réplica en un Batch: `clientes/{idCliente}` (`firebaseUid: null`, `negocioId` del ADMIN) + `indices_clientes/{negocioId}_{dni}` + `clientes_privados/{idCliente}`.
  2. CLIENTE se registra/inicia sesión → `usuarios/{uid}` con `clienteId: null`, `negocioId: null`.
  3. Introduce código maestro + DNI → la app resuelve `negocioId` en `negocios_publicos`.
  4. **Declaración temporal de VÍA 1:** la app escribe en `perfiles_pendientes/{uid}` únicamente `{ dni, negocioId }` (no es un perfil ficticio; es el dato introducido en el momento) para que las Rules validen el acceso al índice y a la ficha.
  5. Lee `indices_clientes/{negocioId}_{dni}` → obtiene `clienteId`.
  6. Transaction de vinculación: `clientes/{idCliente}.firebaseUid = uid` + `usuarios/{uid}` → `{clienteId, negocioId}`. **No se crea segunda ficha.**
  7. Se borra `perfiles_pendientes/{uid}` (éxito o rechazo).
- **VÍA 2 — CLIENTE crea primero:**
  1. CLIENTE se registra → `usuarios/{uid}` con `clienteId: null`, `negocioId: null`.
  2. "No tengo código" → completa su perfil completo → `perfiles_pendientes/{uid}` (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento).
  3. Introduce código maestro + DNI:
     - Si **no existe** `indices_clientes/{negocioId}_{dni}` → Transaction: crear `clientes/{idCliente}` con los datos del perfil + crear el índice + `usuarios/{uid}` + borrar `perfiles_pendientes/{uid}`.
     - Si **existe** y `firebaseUid == null` → vincular a la ficha existente (VÍA 1).
     - Si **existe** y `firebaseUid != null` → rechazar ("ese DNI ya está vinculado").
- **`perfiles_pendientes/{uid}` admite DOS modos:** VÍA 1 (declaración mínima `{ dni, negocioId }`) y VÍA 2 (perfil completo). Siempre se borra al terminar la vinculación (éxito o rechazo).
- **Unicidad:** el documentId determinista `{negocioId}_{dni}` garantiza una única ficha por negocio+DNI. La Transaction sobre el índice serializa la concurrencia.
- Tras vincularse, el CLIENTE puede editar solo sus datos personales (`nombre`, `apellidos`, `telefono`, `email`, `foto`, `fechaNacimiento`). **No** puede modificar `dni`, `negocioId`, `firebaseUid`, `estado`, `serviciosContratados`, fechas administrativas, `tieneLlave` ni `observaciones`.
- El `dni` solo lo cambia el ADMIN, y al hacerlo debe mantener el índice atómico (borrar el índice viejo y crear el nuevo en el mismo Batch).
- El código maestro es independiente de las vinculaciones; cambiarlo no afecta a clientes ya vinculados.
- El mismo `idCliente: Int` se comparte entre Room y Firestore. Los clientes creados por el ADMIN se replican con write-through inmediato (sin cola offline); si la réplica falla, el dato local se conserva, se informa al ADMIN y queda preparada una operación de reintento manual. Las Rules prohíben `delete` en `clientes`: el borrado local se refleja como baja lógica remota.
- Los valores remotos de `clientes.estado` son exactamente los nombres del enum: `ACTIVO`, `BAJA`, `ARCHIVADO`, `REGISTRADO`. `MOROSO` se calcula desde movimientos y nunca se almacena.
- Las consultas de sesiones y reservas deben diseñarse para ser compatibles con las Security Rules; las Rules no funcionan como filtros posteriores.

Security Rules (`firestore.rules`):

- El archivo versionado del proyecto es `firestore.rules`.
- Todo está bloqueado por defecto salvo las rutas declaradas expresamente.
- Las funciones de Rules usan el documento `usuarios/{uid}` para resolver rol, estado, `clienteId` y `negocioId`; no se usan Custom Claims.
- `getAfter()` solo debe utilizarse en operaciones atómicas que actualicen todos los documentos relacionados.
- **VÍA 2:** la creación directa se valida con `creacionDirectaValida()` — el Batch/Transaction incluye la ficha nueva ligada al UID autenticado, su índice y deja `usuarios/{uid}` coherente post-operación.
- **VÍA 1:** la vinculación a una ficha existente se valida con `vinculacionDniValida()` — ficha libre (`firebaseUid == null`), pertenece al negocio indicado, DNI igual al del perfil pendiente y coherencia negocio/cliente/usuario post-Batch.
- **Índices:** `indices_clientes` se crea solo en el mismo Batch que su ficha (`indiceCreadoPorAdmin` / `indiceCreadoPorCliente`); `update` prohibido; `delete` solo ADMIN al cambiar el DNI; `list` prohibido; `get` restringido a que el **DNI y el `negocioId`** del índice coincidan con los declarados en `perfiles_pendientes/{uid}` (o ADMIN de su negocio).
- **`perfiles_pendientes/{uid}`:** `create/get/update/delete` solo del propio uid; `hasOnly` admite DOS modos — VÍA 1 `{ dni, negocioId }` (declaración temporal) o VÍA 2 perfil completo `{ nombre, apellidos, dni, telefono, email, foto, fechaNacimiento }`; `list` prohibido.
- **`clientes/get` CLIENTE VÍA 1 (sin vínculo):** regla adicional que permite a un CLIENTE con `clienteId == null && negocioId == null` leer **solo** la ficha cuyo `dni` y `negocioId` coinciden con su `perfiles_pendientes/{uid}` y cuyo documentId es su `idCliente`. No puede leer fichas arbitrarias, de otros negocios, de otros DNIs ni enumerar (`list` solo ADMIN). Imprescindible para que `transaction.get(clientes/{idCliente})` de la vinculación funcione.
- **`clientes_privados/{idCliente}`:** solo el ADMIN del negocio puede leer/crear/actualizar; `delete` prohibido.
- **`clientes`:** el CLIENTE solo puede leer su propia ficha (`firebaseUid == uid`) y editar solo sus campos personales; `list` solo ADMIN.
- Un CLIENTE solo puede vincularse una vez (`usuarios/{uid}` exige `clienteId == null` y `negocioId == null`).
- `negocios_publicos/{id}` permite `get/list` a cualquier autenticado; `create/update` solo el ADMIN del negocio.
- Una reserva de cliente debe apuntar a una sesión existente del mismo negocio y a una sesión cuyo `clientesPermitidos` contenga el UID autenticado.
- Las Rules deben probarse con los casos ADMIN, CLIENTE, VÍA 1 y VÍA 2 antes de publicarse (`npm --prefix firestore-tests test`).

Los datos reales de prueba, UIDs, códigos y `negocioId` no se documentan en estos archivos; se usan placeholders para evitar guardar identificadores concretos en el repositorio.

Existe una diferencia pendiente entre el modelo local y el contrato remoto: `TipoSolicitud.kt` y `SolicitudEntity.kt` todavía contienen `CLASE` y deben adaptarse posteriormente a `ALTA`/`BAJA` mediante una tarea específica de Room y su migración.

## Convenciones de código

- Responder siempre en español.
- Mantener los nombres de clases, funciones, variables, rutas y paquetes existentes en español.
- No renombrar identificadores existentes para convertirlos al inglés salvo petición expresa.
- Clases y composables con PascalCase.
- Variables y funciones con camelCase.
- Comentarios y documentación en español.
- Seguir el estilo del archivo relacionado que ya exista.
- Preferir funciones pequeñas y responsabilidades claras.
- Los composables hijos deben recibir datos y lambdas; evitar pasarles ViewModels cuando se pueda.
- Usar `StateFlow` para estado observable y `collectAsStateWithLifecycle()` en nuevas pantallas o zonas modificadas.
- Usar `viewModelScope` para operaciones iniciadas por un ViewModel.
- Preferir recursos de `strings.xml` para textos de UI nuevos.
- Añadir KDoc útil a nuevas clases y funciones públicas, sin exigir una reescritura documental del código existente.

## Reglas de calidad y seguridad

- No usar el operador `!!` en código nuevo.
- No realizar operaciones de Room, DataStore, Firebase o red en el hilo principal.
- No almacenar contraseñas, tokens ni claves privadas en texto plano.
- No incluir claves secretas en el código cliente ni en archivos versionados (`google-services.json` está en `.gitignore`).
- No eliminar código existente sin explicar el motivo.
- No corregir bugs no solicitados si no son necesarios para la tarea actual; informar de ellos como deuda o riesgo.
- No introducir Activities adicionales sin justificarlo explícitamente.
- Respetar `allowBackup`, las migraciones Room y los datos de prueba; cualquier cambio de producción debe avisarse.
- El DNI identifica la ficha dentro del negocio y **nunca** debe poder modificarlo un CLIENTE; su cambio solo lo hace el ADMIN manteniendo el índice atómico.

El código actual tiene algunos usos de `!!`, `collectAsState()` y strings hardcodeados. Se consideran advertencias y deuda técnica: deben señalarse cuando afecten a una tarea, pero no se debe iniciar una migración general sin pedirlo.

## Comandos del proyecto

Ejecutar desde `C:\Users\Roberto\AndroidStudioProjects\GestorPro`:

```powershell
# Compilar Admin (debug)
.\gradlew.bat :app:assembleDebug

# Compilar Cliente (debug)
.\gradlew.bat :appCliente:assembleDebug

# Compilar ambos
.\gradlew.bat assembleDebug

# Tests de Firestore Rules (emulador)
npm --prefix firestore-tests test

# Despliegue de Rules (solo tras validar con los tests)
& ".\firestore-tests\node_modules\.bin\firebase.cmd" deploy --only firestore:rules

# Auditoría (DRY-RUN) previa al backfill de indices_clientes (solo lectura)
node firestore-tests/auditoria_backfill_indices.cjs
```

## Tests

- **Rules de Firestore:** `npm --prefix firestore-tests test` (16 pruebas en el emulador). Deben pasar **antes** de desplegar `firestore.rules`.
- Los tests de Android se mantienen para la fase final del proyecto salvo que el desarrollador los solicite expresamente antes. No crear archivos de test automáticamente durante una funcionalidad normal.

## Convenciones específicas de Firebase y navegación

- **Recuperación de contraseña:** usar exclusivamente `FirebaseAuth.sendPasswordResetEmail`. El mensaje de éxito debe ser **genérico** ("Si el email existe, recibirás un enlace…") para no revelar qué cuentas existen; ante errores de autenticación (usuario inexistente, email inválido…) se responde con el mismo mensaje genérico. Solo se comunican fallos reales (p. ej. sin conexión). Validar email no vacío y formato antes de llamar a Firebase (`android.util.Patterns.EMAIL_ADDRESS`).
- **Rutas con parámetros de query:** construir siempre sustituyendo el placeholder, nunca concatenando. En GestorPro Cliente no existen rutas con query (la Vía B está descartada); si se añade una ruta con placeholder, usar `Ruta.replace("{param}", valor)`.
- **Fotos:** la lógica de guardado vive en `ui/utils/FotoUtils.kt` (`guardaFotoEnInterna`, y en Admin además `crearFotoTemporal`, `uriDeFotoTemporal`, `guardarFotoDeCamara`). No duplicar esa función en pantallas. La cámara usa `TakePicture()` con `FileProvider` (`${applicationId}.fileprovider`, `res/xml/file_paths.xml`); el guardado se hace solo en el callback del resultado, nunca justo después de `launch()`. En Admin el selector común es `ui/components/BotonSelectorFoto.kt`.
- **Vinculación del CLIENTE:** el flujo de código maestro + DNI vive en `appCliente` (repositorio `VinculacionRepository`, pantalla `InicioScreen`). Las operaciones críticas (VÍA 1 y VÍA 2) deben ejecutarse en Transaction. Nunca reintroducir Vía B/deep links.

## Estado actual y pendientes (2026-08-27)

Implementado y compilado (BUILD SUCCESSFUL de `:app` y `:appCliente`):

- **Dos aplicaciones independientes:** `:app` (Admin) y `:appCliente` (Cliente) en el mismo proyecto Gradle, con el mismo Firebase (`gestorpro-50e83`) compartido.
- **`firestore.rules` reescrita** con el nuevo modelo: `indices_clientes`, `perfiles_pendientes`, `clientes_privados`, VÍA 1 (`vinculacionDniValida` + declaración temporal en `perfiles_pendientes` + regla `clientes/get` VÍA 1), VÍA 2 (`creacionDirectaValida`), edición personal del CLIENTE, índice atómico al cambiar DNI. Sin `vinculaciones`.
- **Declaración temporal VÍA 1 (Opción B):** al introducir código maestro + DNI, `VinculacionRepository` escribe `perfiles_pendientes/{uid}` = `{ dni, negocioId }` antes de consultar el índice; las Rules exigen que el índice (y la ficha) coincidan con esa declaración. Se borra al terminar (éxito o rechazo).
- **Lectura de la ficha VÍA 1 (Opción A):** regla `clientes/get` que permite a un CLIENTE sin vínculo leer solo la ficha declarada en `perfiles_pendientes/{uid}` (dni + negocioId + documentId == idCliente); necesaria para `transaction.get(clientes/{idCliente})`.
- **Tests de Rules:** 18/18 OK (`npm --prefix firestore-tests test`).
- **Admin (`:app`):** réplica de clientes crea `clientes` + `indices_clientes` + `clientes_privados` en Batch; edición mantiene el índice al cambiar DNI. Eliminadas las pantallas de CLIENTE y Vía B. Rol ADMIN fijo.
- **Cliente (`:appCliente`):** módulo nuevo con login/registro/recuperar, pantalla inicio código+DNI, completar perfil (VÍA 2), vinculación por Transaction (VÍA 1 y VÍA 2), mi perfil y edición de datos personales. Sin Room, sin Vía B.
- **`google-services.json` del Cliente** colocado en `appCliente/google-services.json` (paquete `com.roberto.gestorpro.cliente`, proyecto `gestorpro-50e83`); el plugin se aplica de forma incondicional en `appCliente/build.gradle.kts`.
- **Rules desplegadas en producción:** `gestorpro-50e83` — ruleset con Opción B + Opción A (verificado idéntico al local).

Pendiente para continuar:

1. **Pruebas manuales en dispositivo:** registro y vinculación VÍA 1 y VÍA 2 con las Rules Opción B/A desplegadas, edición de perfil, recuperación de contraseña, y que `:app` (Admin) siga funcionando con su APK.
2. **Backfill de `indices_clientes`** para las fichas existentes con DNI (DRY-RUN realizado: 2 índices, sin colisiones; script `firestore-tests/auditoria_backfill_indices.cjs`). No ejecutar hasta aprobación. OJO: la ficha `clientes/1` pertenece al negocio `7X1KyM8...` (sin `negocios_publicos` vigente) → no localizable con el código maestro actual; decisión aparte.
3. **Verificar en Android Studio** que `:appCliente` aparece como aplicación ejecutable en el selector de Run (módulo ya vinculado en `.idea/gradle.xml`).
4. **Commits pendientes:** toda la sesión está en working tree sin commitear (dos apps, Rules, tests, docs).
5. Limpieza de basura versionada: `build_*.txt` en raíz y `firestore-tests/firestore-debug.log`.
