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
| Almacenamiento de imágenes (solo Admin) | Firebase Storage | Firebase BOM 34.16.0 |

- `firebase-storage` está registrado en el catálogo (`libs.firebase.storage`) y se aplica **solo en `:app`** (el Cliente carga las URLs por HTTP con Coil, sin SDK de Storage).

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
    │   ├── firebase/                    -> Firebase (Autenticacion, Negocio, ClienteRemoto, ServicioRemoto, SesionRemoto, ReservaRemoto)
    │   └── repository/                  -> repositorios de datos
    ├── di/AppModule.kt                  -> dependencias de Hilt
    └── ui/
        ├── auth/                        -> login y registro (ADMIN)
        ├── clases/                      -> código ANTIGUO de Clase/SesionClase (transitorio)
        ├── clientes/                    -> clientes y perfiles (+ diálogo de servicios contratados)
        ├── components/                  -> componentes Compose reutilizables
        ├── configuracion/               -> cuenta, negocio, preferencias y datos
        ├── economia/                    -> movimientos y gastos
        ├── home/                        -> inicio de administrador
        ├── servicios/                   -> SERVICIOS y SESIONES (nuevo modelo)
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
        │                                   completar perfil, mi perfil, editar perfil, cuenta,
        │                                   configuración
        ├── components/                  -> MenuCard, BotonSelectorFoto
        ├── home/                        -> HomeScreen y ClasesScreen (placeholder sin Firestore)
        ├── theme/                       -> tema, colores y tipografía
        ├── utils/                       -> FotoUtils (galería + cámara)
        └── viewmodel/                   -> MainViewModel
```

## Funcionalidades principales

### GestorPro Admin
- Registro/login/logout reales con Firebase Authentication (rol `ADMIN` fijo).
- Creación y edición del negocio con código maestro (`negocios` + `negocios_publicos`).
- Alta, edición, consulta, archivado y restauración de clientes (Room + réplica Firestore con write-through).
- Réplica de clientes que crea `clientes/{idCliente}`, `indices_clientes/{negocio}_{dni}` y `clientes_privados/{idCliente}` en un único Batch.
- Cambio de DNI de un cliente manteniendo el índice atómico (borra el viejo y crea el nuevo en el mismo Batch).
- **Gestión de SERVICIOS y SESIONES (nuevo modelo):** `ServiciosScreen` (ACTIVOS/DE BAJA, crear/editar/dar de baja/reactivar/eliminar), detalle del servicio con la sesión del día, `ProgramarSesionesScreen` (desde/hasta + día con hora propia + duración + capacidad), `EditarSesionScreen` ("Ver / editar sesión") y `SesionReservasScreen` (clientes reservados). Modelo `Cliente → Servicio → Sesión → Reserva` (sin entidad Clase). `ClaseEntity`/`SesionClaseEntity` siguen en Room solo de forma TRANSITORIA.
- **Servicios contratados del cliente (Room, perfil ADMIN):** `ClienteEntity.serviciosContratados: List<Int>` (ids de `ServicioEntity`); selector multi-servicio en el perfil del cliente (solo servicios activos; los ids inactivos se conservan).
- **Reservas en Room:** creación/cancelación ATÓMICAS con `RoomDatabase.withTransaction` (comprueba sesión, plazas > 0, servicio activo, sin duplicado por `(idSesion, idCliente)`); cascadas de reservas al regenerar/desactivar/eliminar servicios y al eliminar sesiones. Movimientos independientes.
- **Reservas en Firestore:** `reservas/{clienteId}_{sesionId}` con Transaction (crear = reserva + `plazasDisponibles-1`; cancelar = delete + `+1`, sin superar capacidad); `ReservaRemotoRepository`.
- Gestión de movimientos, cuotas y gastos.
- Configuración del negocio, nombre, logo, tema, cuenta y datos.
- Subida del logo del negocio a Firebase Storage (`negocios/{negocioId}/logo.jpg`) y guardado de su URL en `negocios` + `negocios_publicos` (mismo WriteBatch).
- Selección de foto de perfil desde galería o cámara.

### GestorPro Cliente
- Registro/login/logout reales con Firebase Authentication (rol `CLIENTE` fijo).
- Recuperación de contraseña (solo `FirebaseAuth.sendPasswordResetEmail`).
- Pantalla inicial "¿Tu gimnasio ya te ha registrado?" con la opción **"No tengo vinculación"**.
- **Sin vínculo:** el CLIENTE puede completar su perfil (`perfiles_pendientes/{uid}`), entrar al **Home sin vincular** (aviso visible) y navegar (Mi perfil, Clases placeholder, Mi cuenta, Configuración) sin consultar Firestore para clases.
- **Vincular con mi gimnasio** (código maestro + DNI) desde el Home:
  - **VÍA 1:** vincular el UID a una ficha existente creada por el ADMIN (sin crear duplicados).
  - **VÍA 2:** si `indices_clientes/{negocioId}_{dni}` NO existe, crear la ficha con los datos de `perfiles_pendientes/{uid}` + índice + `usuarios/{uid}` en la misma Transaction. **NO es un error** que el índice no exista.
- Ver y editar los datos personales: sin vínculo se lee/escribe `perfiles_pendientes/{uid}` (el **DNI es editable**); vinculado se lee/escribe `clientes/{idCliente}` y el **DNI queda bloqueado**.
- El perfil pendiente se borra **solo cuando la vinculación se completa con éxito** (nunca ante errores).
- Nunca muestra ni edita `observaciones` ni datos administrativos.
- Persistencia local solo con DataStore (caché; la fuente de verdad remota es Firestore, incluidos `negocios_publicos/{id}` para nombre y logo).
- Refresco de datos públicos del negocio al arrancar con sesión restaurada (`cargarEstadoLocal()` en `destinoInicial()`); si no hay conexión se conserva la caché.

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
servicios/{idServicio}               <- SERVICIOS del negocio (nuevo modelo)
sesiones/{idSesion}                  <- sesiones de un servicio (sin Clase)
reservas/{clienteId}_{sesionId}      <- reserva atómica cliente+sesión
solicitudes/{solicitudId}
movimientos/{movimientoId}
```

**No existe** la colección `vinculaciones` (Vía B descartada) ni el campo `codigoVinculacion`.

Reglas de identidad y pertenencia:

- El UID de Firebase es el ID del documento `usuarios/{uid}`.
- Los roles remotos son exactamente `ADMIN` y `CLIENTE`.
- `negocioId` es un `String` en Firestore (el `negocioId` del ADMIN es su UID).
- `clienteId`, `idCliente`, `idServicio`, `idSesion` y `sesionId` se manejan como enteros (`int64`) cuando forman parte de los datos.
- Los documentos de `clientes` y `servicios` usan el identificador numérico convertido a texto en la ruta, por ejemplo `clientes/2`, `servicios/7`.
- `firebaseUid` es un `String`; en `clientes` nace `null` (ficha creada por el ADMIN) y solo lo rellena el CLIENTE al vincularse.
- `serviciosContratados` es un array de **enteros** (ids de `servicios`). **`clientesPermitidos` ya NO se usa** en sesiones (el acceso del CLIENTE se calcula en Rules con `get(clientes)` + `get(servicios)`).
- Un administrador solo puede acceder a su negocio, identificado por `adminUid` y `negocioId`.
- Un cliente solo puede acceder a sus datos, sus reservas, sus solicitudes y las sesiones de servicios que tenga contratados y activos.
- Los clientes nunca pueden acceder a `movimientos` ni a `clientes_privados`.

Estructura clave de documentos:

```text
usuarios/{uid} = { rol, activo, clienteId, negocioId }
negocios/{negocioId} = { adminUid, nombre, codigoMaestro, logo }
negocios_publicos/{negocioId} = { nombre, codigoMaestro, logo }
clientes/{idCliente} = { idCliente, negocioId, firebaseUid, nombre, apellidos, dni,
                         telefono, email, foto, fechaNacimiento, fechaRegistro,
                         fechaAlta, fechaBaja, estado, tieneLlave,
                         serviciosContratados: [int...], fechaInicioActual, fechaFinActual }
                     (sin observaciones ni codigoVinculacion)
clientes_privados/{idCliente} = { negocioId, observaciones }
indices_clientes/{negocioId}_{dni} = { negocioId, dni, clienteId }
perfiles_pendientes/{uid} = VÍA 1: { dni, negocioId } | VÍA 2: { nombre, apellidos, dni, telefono, email, foto, fechaNacimiento }
servicios/{idServicio} = { idServicio, negocioId, nombre, descripcion, activo }
sesiones/{idSesion} = { idSesion, negocioId, idServicio, fecha, hora,
                        duracionMinutos, capacidad, plazasDisponibles }
reservas/{clienteId}_{sesionId} = { idReserva, negocioId, sesionId, clienteId, fechaReserva }
```

### Almacenamiento remoto de imágenes (Firebase Storage, solo Admin)

- Ruta del logo del negocio: `negocios/{negocioId}/logo.jpg` (`negocioId` = uid del ADMIN). Al cambiar el logo se sobrescribe; no se conservan históricos.
- **No se guarda la imagen en Firestore**: solo la URL de descarga (`logo`) en `negocios` + `negocios_publicos` (mismo WriteBatch, atómico).
- **Bucket:** el por defecto de `google-services.json` (`project_info.storage_bucket`). **PENDIENTE en producción:** el bucket `gestorpro-50e83.firebasestorage.app` debe crearse/habilitarse en Firebase Console antes de subir el primer logo; hasta entonces la subida falla con "Object does not exist at location".
- Reglas de Storage (`storage.rules`, versionado): lectura para cualquier usuario autenticado; escritura solo para el ADMIN propietario (`usuarios/{uid}.rol == "ADMIN" && usuarios/{uid}.negocioId == negocioId`). Un ADMIN no puede escribir el logo de otro negocio; CLIENTE y no autenticados, denegado. Resto del bucket bloqueado.
- El Cliente descarga el logo por su URL con Coil (HTTP), sin SDK de Storage.

Flujos funcionales remotos:

- Un ADMIN nuevo puede registrarse con `negocioId = null` y debe crear su propio negocio con código maestro.
- La creación del negocio, `negocios_publicos/{id}` y la asignación de `usuarios/{uid}.negocioId` deben ejecutarse en el mismo Batch.
- Las solicitudes solo representan altas y bajas. Sus valores remotos son `ALTA` y `BAJA`.
- Los servicios definen el catálogo del negocio; las sesiones son instancias concretas de un servicio (`sesiones/{id}.idServicio`); las reservas relacionan un cliente con una sesión mediante `sesionId`.
- Un cliente no solicita una clase mediante `solicitudes`; primero debe tener contratado el servicio y después puede reservar una sesión autorizada.

### Alta y vinculación del CLIENTE — dos vías

- **VÍA 1 — ADMIN crea primero:**
  1. ADMIN crea la ficha → réplica en un Batch: `clientes/{idCliente}` (`firebaseUid: null`, `negocioId` del ADMIN) + `indices_clientes/{negocioId}_{dni}` + `clientes_privados/{idCliente}`.
  2. CLIENTE se registra/inicia sesión → `usuarios/{uid}` con `clienteId: null`, `negocioId: null`.
  3. Desde el Home sin vincular pulsa "Vincular con mi gimnasio" e introduce código maestro + DNI → la app resuelve `negocioId` en `negocios_publicos`.
  4. **Declaración temporal de VÍA 1:** la app escribe en `perfiles_pendientes/{uid}` únicamente `{ dni, negocioId }` (con `SetOptions.merge()`, sin destruir un perfil completo previo) para que las Rules validen el acceso al índice y a la ficha.
  5. Lee `indices_clientes/{negocioId}_{dni}` → obtiene `clienteId`.
  6. Transaction de vinculación: `clientes/{idCliente}.firebaseUid = uid` + `usuarios/{uid}` → `{clienteId, negocioId}`. **No se crea segunda ficha.**
  7. Se borra `perfiles_pendientes/{uid}` **solo al completar la vinculación con éxito**.
- **VÍA 2 — CLIENTE crea primero:**
  1. CLIENTE se registra → `usuarios/{uid}` con `clienteId: null`, `negocioId: null`.
  2. "No tengo vinculación" → completa su perfil completo → `perfiles_pendientes/{uid}` (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento). Se guarda y pasa directamente al **Home sin vincular** (no busca ficha ni ejecuta vinculación todavía).
  3. Desde el Home pulsa "Vincular con mi gimnasio" e introduce código maestro + DNI:
     - Si **no existe** `indices_clientes/{negocioId}_{dni}` → Transaction: crear `clientes/{idCliente}` con los datos del perfil + crear el índice + actualizar `usuarios/{uid}` + borrar `perfiles_pendientes/{uid}`.
     - Si **existe** y `firebaseUid == null` → vincular a la ficha existente (VÍA 1).
     - Si **existe** y `firebaseUid != null` → rechazar ("ese DNI ya está vinculado").
- **`perfiles_pendientes/{uid}` admite DOS modos:** VÍA 1 (declaración mínima `{ dni, negocioId }`) y VÍA 2 (perfil completo). **Solo se borra al completar la vinculación con éxito**; ante errores (falta de perfil, red, permisos, fallo intermedio) se conserva para no perder los datos del usuario.
- **Cliente autenticado sin vínculo:** puede tener perfil pendiente y navegar por el Home sin vincular (aviso visible, Clases placeholder sin consultar Firestore). `destinoInicial`/`destinoTrasAutenticar` llevan a Home si existe ficha o perfil pendiente; a la pantalla inicial solo si no hay ninguno.
- **DNI editable pre-vinculación:** mientras no está vinculado el CLIENTE edita su perfil en `perfiles_pendientes/{uid}` (el DNI puede cambiar). Una vez vinculado, la ficha vive en `clientes/{idCliente}` y el DNI queda bloqueado para el CLIENTE; solo el ADMIN lo cambia manteniendo el índice atómico.
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
- **`clientes/get` CLIENTE VÍA 2 (`resource == null`):** regla adicional que permite a un CLIENTE sin vínculo con perfil pendiente leer un documento **inexistente** (`resource == null`), necesaria para que la Transaction de VÍA 2 compruebe que `clientes/{idCliente}` aún no existe. No permite leer los datos de fichas existentes de otros.
- **`clientes_privados/{idCliente}`:** solo el ADMIN del negocio puede leer/crear/actualizar; `delete` prohibido.
- **`clientes`:** el CLIENTE solo puede leer su propia ficha (`firebaseUid == uid`) y editar solo sus campos personales; `list` solo ADMIN.
- Un CLIENTE solo puede vincularse una vez (`usuarios/{uid}` exige `clienteId == null` y `negocioId == null`).
- `negocios_publicos/{id}` permite `get/list` a cualquier autenticado; `create/update` solo el ADMIN del negocio (campos `codigoMaestro`, `nombre`, `logo`).
- **`servicios/{idServicio}`:** solo el ADMIN del negocio crea/modifica/elimina; el CLIENTE vinculado puede `get` servicios ACTIVOS de su negocio (necesario para la Transaction de reserva). create/update validan `hasOnly`, tipos y `negocioId`.
- **`sesiones/{idSesion}`:** solo el ADMIN del negocio crea/modifica/elimina; la creación exige un servicio existente, del negocio y ACTIVO (`servicioValidoParaSesion`). El CLIENTE vinculado puede `get/list` solo sesiones de servicios contratados y activos de su negocio. `resource == null` en get ADMIN para comprobación de existencia.
- **`reservas/{clienteId}_{sesionId}` (documentId determinista):** creación y cancelación ATÓMICAS (Transaction). El CLIENTE crea su reserva (validada contra Firestore con `reservaCreaValida`: negocio, servicio contratado+activo, `plazas == anterior-1 && >= 0`) y la elimina devolviendo la plaza (`reservaEliminadaValida`: `== anterior+1 && <= capacidad`). El decremento/incremento de `plazasDisponibles` de la sesión solo se permite al CLIENTE si la Transaction crea/elimina la reserva (`reservaCreadaEnTransaccion` / `reservaEliminadaEnTransaccion`). El ADMIN consulta y elimina reservas de su negocio con ajuste de plazas. `update` de CLIENTE: false.
- **Storage Rules (`storage.rules`):** lectura para cualquier autenticado; escritura solo para el ADMIN propietario (`negocios/{negocioId}/logo.jpg`, `negocioId == usuarios/{uid}.negocioId`). Resto del bucket bloqueado.
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

# Tests de Firestore + Storage Rules (emulador: firestore y storage)
npm --prefix firestore-tests test

# Despliegue de Rules (solo tras validar con los tests)
& ".\firestore-tests\node_modules\.bin\firebase.cmd" deploy --only firestore:rules
& ".\firestore-tests\node_modules\.bin\firebase.cmd" deploy --only storage:rules

# Auditoría (DRY-RUN) previa al backfill de indices_clientes (solo lectura)
node firestore-tests/auditoria_backfill_indices.cjs
```

## Tests

- **Rules de Firestore + Storage:** `npm --prefix firestore-tests test` (**123 pruebas** en los emuladores `--only firestore,storage`, incluidas PRUEBA 6B/6C (Vía A), 9B, 33A–33H, 77–81, 82–88 (`horaDesdeReserva`), **99–108 (solicitudes de baja)** y **109–112 (bloqueo de BAJA en sesiones/reservas)**). Deben pasar **antes** de desplegar las Rules.
- **Test unitario appCliente:** `:appCliente:testDebugUnitTest` cubre el rechazo de Vía A cuando no existe el índice `negocioId_DNI`.
- **Tests unitarios de helpers de Cloud Functions:** `node --test functions/test/ids.test.js functions/test/tokens.test.js` (**13/13**, sin necesidad de `npm install` en `functions/`; solo cubren los módulos puros `ids.js` y `tokens.js`).
- **Test de aislamiento del alta (temporal, Sesión XVIII):** `firestore-tests/diagnostico_alta_cliente.test.cjs` (7/7) reproduce el payload real de `crearClienteRemoto()` contra las Rules locales para aislar el PERMISSION_DENIED del alta. Ejecutar con: `cd firestore-tests; Copy-Item ..\firestore.rules firestore.rules.generated -Force; .\node_modules\.bin\firebase.cmd emulators:exec --project gestorpro-rules-test --only firestore "node --test diagnostico_alta_cliente.test.cjs"`. No forma parte del suite oficial.
- Los tests de Android se mantienen para la fase final del proyecto salvo que el desarrollador los solicite expresamente antes. No crear archivos de test automáticamente durante una funcionalidad normal.

## Convenciones específicas de Firebase y navegación

- **Recuperación de contraseña:** usar exclusivamente `FirebaseAuth.sendPasswordResetEmail`. El mensaje de éxito debe ser **genérico** ("Si el email existe, recibirás un enlace…") para no revelar qué cuentas existen; ante errores de autenticación (usuario inexistente, email inválido…) se responde con el mismo mensaje genérico. Solo se comunican fallos reales (p. ej. sin conexión). Validar email no vacío y formato antes de llamar a Firebase (`android.util.Patterns.EMAIL_ADDRESS`).
- **Rutas con parámetros de query:** construir siempre sustituyendo el placeholder, nunca concatenando. En GestorPro Cliente no existen rutas con query (la Vía B está descartada); si se añade una ruta con placeholder, usar `Ruta.replace("{param}", valor)`.
- **Fotos:** la lógica de guardado vive en `ui/utils/FotoUtils.kt` (`guardaFotoEnInterna`; además `crearFotoTemporal`, `uriDeFotoTemporal`, `guardarFotoDeCamara` en Admin y también en appCliente). No duplicar esa función en pantallas. La cámara usa `TakePicture()` con `FileProvider` (`${applicationId}.fileprovider`, `res/xml/file_paths.xml`); el guardado se hace solo en el callback del resultado, nunca justo después de `launch()`. El selector común es `ui/components/BotonSelectorFoto.kt`.
- **Vinculación del CLIENTE:** el flujo de código maestro + DNI vive en `appCliente` (repositorio `VinculacionRepository`, pantalla de vinculación accesible desde el Home). Las operaciones críticas (VÍA 1 y VÍA 2) deben ejecutarse en Transaction. Nunca reintroducir Vía B/deep links.
- **Logo del negocio:** la subida vive en `NegocioRepository.guardarLogoRemoto()` (`:app`): `putFile` a `negocios/{uid}/logo.jpg` → `downloadUrl` → WriteBatch con `logo` en `negocios` + `negocios_publicos`. El Cliente lee `negocios_publicos/{id}.logo` y lo muestra con Coil. Requiere el bucket habilitado en Firebase Console.

## Estado actual y pendientes (2026-09-02)

> ACTUALIZACIÓN (2026-09-0X — CONTINUACIÓN DESDE AUDITORÍA FINAL DE BOTONES). HEAD actual del
> desarrollador: `3b94164 "mejoras y correciones"` (hay cambios del desarrollador posteriores a la
> anterior ACTUALIZACIÓN de 2026-09-02). Working tree con 29 cambios SIN commit. Esta actualización
> es un CHECKPOINT: documenta lo realizado en esta conversación y las inconsistencias detectadas en
> el árbol para retomar con precisión.
>
> **VERIFICADO EN EL ÁRBOL ACTUAL (al cierre):**
> - Rules de Firestore: la colección de tests sigue con **135 tests** (`npm --prefix firestore-tests test`);
>   las PRUEBA 121–129 (economía/resumen, campos vacíos, fecha opcional) **NO están presentes** en el
>   ruleset local (parece revertido por commits del desarrollador). `firestore.rules` mantiene
>   `match /movimientos/...` pero NO contiene las claves `moroso`/`fechaEntradaMorosidad`/`deuda` en el
>   update ADMIN de `clientes`.
> - Economía FASE 6 (Room→Firestore): presente: `util/MovimientoFirestore.kt`,
>   `data/firebase/MovimientoRemotoRepository.kt`, DAO insert→Long, `MovimientoRepository` con
>   sincronización de movimiento+resumen, `ClienteRemotoRepository.actualizarResumenEconomicoRemoto`,
>   `AppModule`. **OJO:** si el ruleset desplegado/actual no autoriza las claves del resumen, reaparece
>   el aviso "Cliente X: No tienes permisos para sincronizar esta ficha" (en esta conversación se
>   DESPLEGARON las Rules una vez autorizado `deploy --only firestore:rules`; verificar estado desplegado
>   vs. local antes de continuar).
> - Alta/identidad: presente `util/IdCliente.kt` (ids altos en el alta del ADMIN) y la reconciliación
>   Firestore→Room (`buscarClienteEnNubePorDni`, `obtenerClientesRemotosDelNegocio`,
>   `incorporarClientesRemotos` en ClientesScreen). La reactivación de la VÍA 2 en appCliente
>   (`VinculacionRepository`) puede haber quedado afectada por el revert: revisar `ResultadoIndice.NoExiste`.
> - `fechaNacimiento` OPCIONAL: **NO está vigente** en el árbol actual: `AñadirClienteScreen` vuelve a
>   exigirla (`errorFechaNacimiento = fechaNacimiento == null`) y `CompletarPerfilScreen` la pide. Decidir
>   si se reintroduce (fue un cambio de esta conversación posteriormente revertido).
> - Botones App*: `Botones.kt` existe en `:app` y `:appCliente`. En `:app` se fijó `AppPrimaryButton` y
>   `AppDialogConfirmButton` al azul corporativo `#1E88E5` (`AzulPrimarioGestPro`) porque el tema usa
>   Material You (primary dinámico/verde). `:appCliente` mantiene `AppPrimaryButton` con color del tema
>   (no modificado por nosotros). `ui/components/DetalleVisuales.kt` NO existe actualmente (eliminado/revert).
> - Se re-unificaron a App* estas pantallas (última verificación: 0 botones Material 3 directos):
>   `:app` ClientesScreen, EditarServicioScreen, DetalleServicioScreen, EditarSesionScreen,
>   AñadirClienteScreen (principales), PerfilClienteAdministradorScreen (botones principales/diálogos;
>   quedan excepciones justificadas: WhatsApp, DatePickers, Archivar/Restaurar semántico, selector método).
>   `:appCliente` CompletarPerfilScreen, EditarPerfilScreen, InicioScreen, MiPerfilScreen,
>   ConfiguracionScreen, HomeScreen.
> - Política de privacidad implementada y accesible en ambas apps
>   (`PoliticaPrivacidadScreen.kt` en `:app` y `:appCliente`; rutas/enlaces OK).
>
> **PARA REANUDAR (próximos pasos sugeridos):**
> 1. Verificar/conciliar el ruleset: local (135 tests, sin PRUEBA 121–129 ni claves del resumen) frente al
>    desplegado en producción; decidir si se reintroduce la sección movimientos completa + `moroso`/`deuda`/
>    `fechaEntradaMorosidad` y sus tests (esta conversación llegó a 144 tests antes del revert).
> 2. Revisar si la VÍA 2 de appCliente sigue reactivada y el flujo de fecha de nacimiento opcional.
> 3. Completar la unificación de botones en las pantallas activas que aún usan Material 3 directo
>    (inventario previo: auth de `:app`, Perfil de cliente restante, EconomiaScreen y sus diálogos,
>    ProgramarSesionesScreen, MiNegocio/CrearNegocio/Cuenta/Datos/Preferencias, notificaciones, solicitudes,
>    y varias de `:appCliente` Cuenta/ListaNotificaciones/etc.). NO tocar `ui/clases/*` (legacy).
> 4. Decidir si `DetalleVisuales.kt` (kit de detalle) se recupera o se elimina definitivamente.
> 5. Commits agrupados del working tree y limpieza (`firestore-debug.log`, basura versionada).


> ACTUALIZACIÓN 2026-09-02 (ECONOMÍA FASES 1–5 + AJUSTES LLAVE/CARDS + SELECTOR NOTIFICACIÓN):
> HEAD = `3b113e6 "impplementando codigo para cuando contrate balze2"` (SIN commits nuevos; todo lo
> de esta tanda está en el working tree). Detalle cronológico en `CONVERSACION_EXPORTADA.md`
> (Sesiones XXVIII en adelante).
>
> **Cambios cerrados en esta tanda (todo compilando, sin commit ni deploy):**
> 1. **Llave como servicio normal:** eliminado `tieneLlave` (Room v13→14 con recreación de
>    `cliente` sin la columna), de `model/Cliente`, de la UI (AñadirCliente/Perfil), de la réplica a
>    Firestore y de las Rules (hasOnly). Sin migrar valores antiguos (decisión: descartar). La
>    "llave" se gestiona como un servicio normal contratado. appCliente solo conserva cambios
>    previos sin commitear (parser sin el campo).
> 2. **ServiciosScreen:** card COMPACTO (sin chip "ACTIVO/DE BAJA" dentro) y ahora muestra el
>    **precio del servicio** (`30 €` / `12,50 €`).
> 3. **FASE 1 — Modelos Room (Room v14):** `ServicioEntity.precio: Double = 0.0`;
>    `MovimientoEntity` pasa a `servicios: List<Int>` + `precioFinal: Double` + `metodoPago:
>    MetodoPago?` (desaparecen `servicio`/`precio`); nuevos `model/MetodoPago` (EFECTIVO/BIZUM/
>    TRANSFERENCIA) y `MetodoPagoConverter`. `MIGRACION_13_14` no destructiva (recrea `servicio`
>    con precio=0 y `movimiento` con precioFinal=precio, metodoPago=NULL y servicios mapeado por
>    nombre exacto único; si no hay correspondencia segura → `[]`).
> 4. **FASE 2 — Precio de servicios (UI + Firestore):** campo Precio en EditarServicioScreen
>    (≥0); ServiciosScreen muestra el precio; VM/Repo remoto envían `precio`; Rules `servicios`
>    aceptan `precio` (number) en create/update (en update solo se exige si se toca). Rules
>    **135/135** (PRUEBA 21B/21C/25B/25C nuevas).
> 5. **FASE 3 — Movimientos multi-servicio:** Nuevo movimiento con checkboxes de ACTIVOS
>    (nombre+precio) y propuesta `precioFinal` = suma (flag manual que NO se sobrescribe; botón
>    "Usar precio calculado"). Edición multi; inactivos históricos se conservan bloqueados.
>    Crear/editar NO modifica `serviciosContratados`. Helpers `util/MovimientoPrecio` (10 tests).
> 6. **FASE 4 — Pagos en el movimiento:** `util/MovimientoPago.resolver` (12 tests): PENDIENTE→
>    PAGADO fija fechaPago (hoy salvo fecha elegida); PAGADO→PENDIENTE limpia fechaPago+metodoPago;
>    método opcional; edición conserva fecha/método existentes (bug de pérdida corregido); detalle
>    de EconomiaScreen muestra fecha y método.
> 7. **FASE 5 — Deuda y morosidad en Room (Room v15):** `ClienteEntity`/`Cliente` con
>    `moroso: Boolean` y `fechaEntradaMorosidad: Long?`. Motor ÚNICO `util/MovimientoMorosidad`
>    (17 tests): deuda = PENDIENTES exigibles (`fechaFin <= ahora`); ACTIVO moroso si deuda>0 o
>    perdió continuidad PAGADA; BAJA moroso SOLO por deuda; fechaEntrada = fechaFin del periodo y
>    NO se reinicia al recalcular. `ClienteDao.obtenerIdsMorosos()` ahora lee `moroso=1`;
>    `MovimientoRepository` recalcula tras cada CRUD y expone `recalcularMorosidadDeCliente` (se
>    llama también en bajas/restauración). NO se eliminó `EstadoCliente.MOROSO` todavía; sin
>    `MOROSO_BAJA`, PagoEntity ni pagos parciales.
> 8. **Corrección notificaciones:** el selector INDIVIDUAL ya NO abre diálogo: reutiliza la
>    pantalla completa `SeleccionarClientesScreen` con `ModoSeleccion.UNO` (selección única;
>    "Continuar" fija `NotificacionesViewModel.seleccionIndividual`; volver atrás no lo modifica).
>    GRUPO sin cambios (ruta `seleccionar_clientes?modo=grupo|individual`). `DialogoSeleccionarClientes`
>    queda sin uso (no eliminado).
>
> **Verificación:** `:app`/`:appCliente` BUILD SUCCESSFUL; `:app:testDebugUnitTest` **45/45**
> (Example 1 + MovimientoPrecio 10 + MovimientoPago 12 + MovimientoMorosidad 17 +
> NotificacionConfig 5); Rules **135/135** (sin cambios tras Fase 2).
> **No tocado:** Firestore de movimientos, Functions, appCliente (salvo previos sin commitear),
> notificaciones de envío. Room v15 con migraciones 11→12,12→13,13→14,14→15 (fallback destructivo
> aún presente como respaldo, no usado en estas rutas).
> **Pendiente inmediato (decisión del propietario):** siguiente fase de economía (¿resumen/deuda
> en UI de Economía?, ¿sincronización Firestore de movimientos o resumen?, retirar
> `EstadoCliente.MOROSO`, alinear default de config app↔Functions) + revisión/commit del working
> tree y limpieza de basura (Rules desplegadas siguen obsoletas: sin `clientePuedeAcceder`,
> `solicitudes/delete` ni `SOLICITUD_BAJA`/`precio`).

> ACTUALIZACIÓN 2026-09-02 (CORRECCIONES/FUNCIONALIDADES + DIAGNÓSTICO ECONOMÍA): HEAD =
> `3b113e6 "impplementando codigo para cuando contrate balze2"`. Todo el trabajo
> posterior a `244db1e` sigue en el working tree SIN commit (incluidas las 2 fases de
> correcciones de esta sesión). Rules **131/131** (123 + PRUEBA 113–120). Unit `:app`
> con `NotificacionConfigTest` (5). Detalle en `CONVERSACION_EXPORTADA.md` (Sesiones XXV–XXVII).
>
> **Working tree actual (NO revertir, sin commit):**
> - `app/.../ui/viewmodel/SesionViewModel.kt` — fix regresión PERMISSION_DENIED al generar sesiones.
> - `app/.../ui/solicitudes/SolicitudesScreen.kt` — búsqueda por cliente + eliminar resueltas + scroll.
> - `app/.../ui/viewmodel/SolicitudesViewModel.kt` + `app/.../data/firebase/SolicitudRemotoRepository.kt` — eliminar solicitud resuelta; aviso SOLICITUD_BAJA al ADMIN.
> - `app/.../data/firebase/NotificacionRemotoRepository.kt` — `crearNotificacionSolicitudBaja`, `existeNotificacion`, config con default `bajaConfirmada.activa=true`.
> - `app/.../data/firebase/BajaClienteRemotoRepository.kt` — BAJA_CONFIRMADA con fecha + idempotencia + default activo.
> - `app/.../ui/notificaciones/GestionNotificacionesScreen.kt` — tipo "Solicitud de baja".
> - `appCliente/.../ui/home/HomeScreen.kt` — aviso de morosidad como texto (no Card), borde rojo en Card de estado.
> - `firestore.rules` — `solicitudes/delete` no PENDIENTE; tipo `SOLICITUD_BAJA` permitido al ADMIN.
> - `firestore-tests/firestore.rules.test.cjs` — PRUEBA 113–120.
> - `app/src/test/.../NotificacionConfigTest.kt` (nuevo, 5 tests).
> - Basura: `firestore-tests/firestore-debug.log`, `.idea/shelf/...`.
>
> **Correcciones y funcionalidades cerradas (working tree):**
> 1. **Regresión sesiones (PERMISSION_DENIED al generar):** causa = el servicio no estaba
>    replicado en Firestore (negocio actual `6YFNg1...` con `servicios` vacía); la regla
>    `sesiones/create` exige `servicioValidoParaSesion`. Fix: `SesionViewModel.generarSesiones`
>    replica el servicio (`crearServicioRemoto`, idempotente) antes de la cascada/réplica.
>    NO se abrieron permisos. PRUEBA 113–115.
> 2. **SolicitudesScreen:** búsqueda por datos REALES del cliente (nombre+apellidos/DNI/
>    teléfono/email/id); eliminar ACEPTADA/RECHAZADA con confirmación; PENDIENTE no
>    eliminable (UI y Rules); lista con `weight(1f)`. PRUEBA 116–117.
> 3. **Home appCliente:** aviso de morosidad como texto (color error, "aquí" en primario/
>    negrita/subrayado, solo "aquí" clicable → `Routes.CUENTA`); Card de estado con borde
>    `colorScheme.error` cuando PAGO_VENCIDO (contenido/fondo intactos).
> 4. **Notificación CLIENTE→ADMIN por solicitud de baja:** la crea el ADMIN al cargar
>    solicitudes PENDIENTES (`SolicitudesViewModel.generarAvisosDeSolicitudesPendientes` →
>    `crearNotificacionSolicitudBaja`, `notificaciones/solicitud_baja_{clienteId}_{fecha}`,
>    tipo SOLICITUD_BAJA, origen AUTOMATICA). Se RETIRÓ la creación desde el CLIENTE (las
>    Rules desplegadas solo permiten `esAdmin()` en `notificaciones`). Rules: `SOLICITUD_BAJA`
>    añadido al `tipo` del create ADMIN. PRUEBA 118–120.
> 5. **BAJA_CONFIRMADA:** mensaje con fecha ("Tu baja se ha realizado con fecha dd/MM/yyyy."),
>    **default `bajaConfirmada.activa = true`** (config inexistente/campo ausente → activa;
>    el ADMIN puede desactivarla en Configuración), idempotencia con `existeNotificacion` e
>    ID determinista `baja_confirmada_{clienteId}_{fechaBaja}` (igual que CF).
>
> **DIAGNÓSTICO ECONOMÍA (2026-09-02, SOLO LECTURA, nada implementado):** ver
> `CONVERSACION_EXPORTADA.md` Sesión XXVII. Resumen: NO existe circuito económico (no hay
> Cuota/Pago/Descuento/Tarifa/método de pago/prorrateo). Un movimiento Room es la "cuota"
> manual (servicio texto + precio manual + fechaInicio/fechaFin + estado + fechaPago solo en
> renovación). Solo se replica a Firestore `clientes/{id}.fechaInicioActual/fechaFinActual`
> (derivados del movimiento con mayor fechaFin). Morosidad derivada y NO persistida, con DOS
> fuentes (Room: ACTIVO fechaFin<ahora / BAJA PENDIENTE; appCliente/Functions: fechaFinActual).
> La regla del "cuarto día hábil" NO está implementada en ningún sitio. `movimientos/{id}` tiene
> reglas Firestore pero la colección no se usa. CF (sin deploy) depende solo de
> `clientes/{id}.fechaFinActual`. BUG confirmado: editar un movimiento resetea `fechaPago`
> (`PerfilClienteAdministradorScreen.kt:2094-2109`). Divergencia app/CF en default de config.
> Hay **10 decisiones de negocio pendientes** (sección 24 del informe / Sesión XXVII).
>
> **Pendiente INMEDIATO (decisión del propietario, NO programar hasta decidir):**
> 1. **ECONOMÍA — decisiones (Sesión XXVII §24):** cuota=movimiento o entidad; Pago
>    independiente vs fechaPago; tarifas en Servicio; descuentos (estudiante/familia/
>    jubilado + edad estudiante); altas/prorrateos (tramos, cargo, 0,25 €/día, llave-tarifa);
>    regla exacta de morosidad ("cuarto día hábil"); BAJA+deuda; replicación mínima a
>    Firestore; default config app↔CF; appCliente económico o no.
> 2. **Rules desplegadas OBSOLETAS (01/09 15:18):** no incluyen `clientePuedeAcceder`,
>    `solicitudes/delete` restringido ni `SOLICITUD_BAJA`. Pendiente de desplegar cuando se
>    autorice (hasta entonces, en producción el aviso SOLICITUD_BAJA no se puede crear).
> 3. **Alta Admin PERMISSION_DENIED (Sesión XVIII):** `[DIAG alta]` en `ClienteRemotoRepository`
>    (hipótesis: documento huérfano existente); retirar logging al cerrar.
> 4. **Regenerar sesiones en el negocio actual** y confirmar índice `READY`; retirar logs
>    `ClasesDiagnostico`/`[DIAG sesiones]`.
> 5. Blaze/Storage/Functions siguen pendientes (sin activar).

> ACTUALIZACIÓN 2026-09-01 (FASES D + E LOCAL + SOLICITUDES DE BAJA + CORRECCIÓN BAJA): HEAD sin
> cambios funcionales nuevos del desarrollador (todo el trabajo posterior a
> `244db1e` está en el working tree, SIN commit). Rules **123/123** (119 + 4 de
> bloqueo de BAJA). Detalle en `CONVERSACION_EXPORTADA.md` (Sesiones XII–XX) y en los
> informes de Fase D/E/Solicitudes/BAJA.
>
> **CLOUD FUNCTIONS PREPARADAS EN LOCAL, SIN DESPLEGAR (proyecto en plan Spark):
> `billingEnabled=false` comprobado por API (2026-09-01).** No activar Blaze sin
> decisión explícita. Hasta entonces no se despliega nada (Functions, Storage,
> FCM real).
>
> **Solicitud de baja del cliente (completada):** colección `solicitudes`
> (reutilizada, NO `solicitudes_baja`) con `idSolicitud, negocioId, idCliente,
> firebaseUid, fechaSolicitud, estado (PENDIENTE/ACEPTADA/RECHAZADA), tipo
> (ALTA/BAJA), fechaResolucion, resueltaPor, motivo`. DocumentId determinista
> `baja_{clienteId}_{fechaSolicitud}`. Rules reforzadas (create CLIENTE con
> hasOnly + `esClientePuedeSolicitarBaja` (no BAJA/ARCHIVADO); update solo ADMIN
> desde PENDIENTE y solo campos de resolución). Admin: `SolicitudesScreen` +
> card Home + `SolicitudesViewModel` + `SolicitudRemotoRepository` (acepta con
> Transaction: solicitud ACEPTADA + `clientes/{id}` BAJA + fechaBaja, y actualiza
> Room). appCliente: `CuentaScreen` ("Solicitar baja" + estado) +
> `SolicitudRepository` (sin duplicado PENDIENTE). Al aceptar, si
> `configuracion_notificaciones/{negocioId}.bajaConfirmada.activa` está activa,
> se crea la notificación **BAJA_CONFIRMADA** reutilizando
> `NotificacionRemotoRepository` con ID determinista
> `baja_confirmada_{clienteId}_{fechaBaja}` (el mismo que usará Cloud Functions).
> El FCM real queda para Cloud Functions. PRUEBA 99–108.
>
> **Corrección del flujo de BAJA (completada, auditada):** la auditoría confirmó
> que `CuentaScreen` era **inalcanzable** (nadie navegaba a `Routes.CUENTA`) y que
> un cliente BAJA **podía** leer sesiones y reservar (app y Rules). Corregido:
> (1) appCliente `ConfiguracionScreen` ahora enlaza a **"Mi cuenta"** →
> `Routes.CUENTA`; (2) **Rules**: helper `clientePuedeAcceder` (estado != "BAJA")
> en `sesiones get/list` CLIENTE y `reservaCreaValida` → PRUEBA 109–112, **123/123**;
> (3) appCliente: `SesionesClienteViewModel` no carga sesiones si BAJA,
> `ClasesScreen` muestra el aviso, `HomeScreen` oculta la card "Clases",
> `ReservaRepository.crearReserva` rechaza BAJA; (4) **baja efectiva UNIFICADA**
> en `BajaClienteRemotoRepository` (cancela reservas FUTURAS en Room y Firestore
> liberando plazas, conserva las pasadas y los `serviciosContratados`, y genera
> `BAJA_CONFIRMADA` con ID determinista): la **baja directa**
> (`ClienteViewModel.darDeBaja` + confirmación en `AñadirClienteScreen`) y la
> **aceptación de solicitud** (`SolicitudesViewModel`) convergen en la misma
> lógica. `fechaBaja` coherente en Room/Firestore.
>
> **Fase D — Notificaciones ADMIN (completada):** `GestionNotificacionesScreen`
> (lista `notificaciones/{id}`), `CrearNotificacionScreen` (individual/grupo/
> todos + programadas), `SeleccionarClientesScreen` (selección grupal con filtros
> reutilizando ClienteItem/FilterChipItem/FiltroClientes), `ConfigNotificaciones
> Screen` (morosidad, recordatorio 24h = 0/24, baja confirmada), `DialogoSeleccion
> arClientes` (individual). Modelo `NotificacionAdmin` + `ConfiguracionNotificaciones`
> + `ModoDestino` + `DestinatarioResuelto`. La creación inmediata crea buzones y
> deja `PENDIENTE`; el estado final (ENVIADA/ERROR) y el push los resuelve Cloud
> Functions.
>
> **Fase E — preparación LOCAL (pendiente Blaze):** Cloud Functions 2ª gen en
> `functions/` (`index.js` + `lib/{ids,tokens,firestore,destinatarios,envio,
> procesadores}.js` + `test/`): triggers de notificación inmediata, programadas
> (onSchedule 2 min, índice `notificaciones(estado, fechaProgramada)` pendiente),
> recordatorio de morosidad (onSchedule 1 h, campo `ultimoRecordatorioMorosidad`),
> morosidad (onUpdate clientes con `fechaFinActual`), baja confirmada. Claims
> atómicos PENDIENTE→ENVIADA / PROGRAMADA→ENVIADA, IDs deterministas, lotes de
> ≤500 tokens (`sendEachForMulticast`), tokens inválidos eliminados, respeto de
> `notificacionesActivadas` por dispositivo (appCliente: `DispositivoRepository`
> guarda el campo + `actualizarNotificacionesActivadas`). `storage.rules` local
> ampliada para fotos (`clientes/{clienteId}/foto.jpg`, image/* ≤5MB, ADMIN del
> negocio escribe, CLIENTE solo su propia foto); helper `rutaFotoClienteEnStorage`.
> **Sin deploy. Sin Blaze.**
>
> **Persistencia/Sincronización:** fuente de verdad = Room en Admin (Firestore =
> espejo write-through para clientes/servicios/sesiones/reservas/notificaciones/
> solicitudes). El CLIENTE usa Firestore directo (sin Room). Los MOVIMIENTOS solo
> se replican parcialmente (período `fechaInicioActual`/`fechaFinActual`): **la
> auditoría de economía es el siguiente bloque pendiente** (ver abajo).
>
> **Pendiente actual (2026-09-01):**
> 1. **AUDITORÍA DE ECONOMÍA (siguiente bloque):** verificar el circuito completo
>    cuota→movimiento→pago→morosidad→baja. Preguntas clave: ¿cuál es la fuente de
>    verdad del movimiento (Room/Firestore/ambas)? ¿Se replica lo suficiente para
>    que Cloud Functions calcule la morosidad desde Firestore? ¿Regla real de
>    entrada/salida de MOROSO ("cuarto día hábil")? ¿Qué ocurre con BAJA + deuda
>    pendiente y si hace falta `MOROSO_BAJA`? NO tocar código hasta decidirlo.
> 2. **Reservas del CLIENTE:** verificar que la cancelación está conectada a la UI
>    de appCliente (repositorio existe) y la consistencia atómica reserva+plazas.
> 3. **Blaze (cuando se decida):** activar facturación → crear índice
>    `notificaciones(estado, fechaProgramada)` → `npm install` en `functions/` →
>    desplegar Functions y `storage.rules` → probar FCM real.
> 4. **Habilitar bucket de Storage** (logo y futuras fotos) y probar `storage.rules`.
> 5. **Auditorías finales:** Admin (clientes/servicios/sesiones/reservas/
>    solicitudes/notificaciones), seguridad (aislamiento negocio/cliente),
>    persistencia/sincronización entidad por entidad.
> 6. **Pendientes heredados:** backfill de `indices_clientes` (solo con
>    aprobación), limpieza definitiva de `Clase`/`SesionClase`/`ServicioItem`,
>    retirar logging temporal `[DIAG alta]`/`ClasesDiagnostico`, commits agrupados
>    y limpieza de basura versionada (`build_*.txt`, `firestore-debug.log`).
>
> Histórico (2026-08-31, Sesión XIX) a continuación — se conserva como referencia.
>
> ---

> ACTUALIZACIÓN 2026-08-31 (SESIÓN XIX): HEAD = `244db1e "Conectando las sesiones"` (commit del
> desarrollador; incluye la fase `horaDesdeReserva` completa). Tests de Rules **99/99**. Detalle en
> `CONVERSACION_EXPORTADA.md` (Sesiones XII–XIX).
>
> **ÍNDICES COMPUESTOS CREADOS en `gestorpro-50e83`** (causa raíz de que `sesiones` estuviera VACÍA
> en Firestore y appCliente no viera clases): `sesiones(idServicio, negocioId)`,
> `reservas(clienteId, negocioId)`, `reservas(sesionId, negocioId)`. El emulador no exige índices,
> por eso los tests pasaban mientras la réplica Admin fallaba silenciosamente en producción.
> PENDIENTE: confirmar `READY` y **regenerar las sesiones desde el Admin** para que se repliquen.
>
> El working tree tiene **5 archivos sin commitear** (NO revertir): `PerfilClienteAdministradorScreen`
> (campo "Servicio" en detalle del movimiento), `DetalleServicioScreen` (fix fecha sesión de hoy),
> `EditarSesionScreen` (fix DatePicker UTC), `ProgramarSesionesScreen` (lista de sesiones +
> navegación a edición), `SesionesClienteViewModel` (logs `ClasesDiagnostico` + `esDeHoy()`).
>
> Diagnóstico en curso del **PERMISSION_DENIED en el alta Admin** (Sesión XVIII): el payload y la lógica de `crearClienteRemoto()` son correctos contra las Rules locales (test de aislamiento `firestore-tests/diagnostico_alta_cliente.test.cjs` = 7/7). La causa más probable es que **uno de los 3 documentos del batch ya existe** en Firestore (índice/ficha/privado de un intento anterior), lo que convierte `batch.set()` en `update` y las Rules lo deniegan. Hay logging temporal `[DIAG alta]` en `ClienteRemotoRepository.crearClienteRemoto()` (incluye `existencia previa`) para confirmarlo; retirarlo al cerrar la causa.

Implementado y compilado (`:app` y `:appCliente` BUILD SUCCESSFUL; `:app:compileDebugKotlin` EXITCODE 0; Rules **92/92 OK**):

- **Dos aplicaciones independientes:** `:app` (Admin) y `:appCliente` (Cliente) en el mismo proyecto Gradle, con el mismo Firebase (`gestorpro-50e83`) compartido.
- **Nuevo modelo SERVICIOS/SESIONES/RESERVAS (Fases 1–5C):** `Cliente → Servicio → Sesión → Reserva`, sin entidad Clase en el flujo nuevo.
  - **Room (Fase 1, 2, 3, 5B):** `ServicioEntity`, `SesionEntity`, `serviciosContratados: List<Int>` en `ClienteEntity`, reservas atómicas con `RoomDatabase.withTransaction` y cascadas.
  - **Firestore (Fases 4A, 4B, 5C):** réplica de `servicios/{id}`, `sesiones/{id}` y `reservas/{clienteId}_{sesionId}` con write-through; Transaction atómica reservar/cancelar (plazas±1). `negocioId` remoto = UID del ADMIN.
- **Sync `serviciosContratados` Admin → Firestore (Fase 6, 28-29/08):** `ClienteRemotoRepository.actualizarServiciosContratadosRemoto(idCliente, ids)` (write-through ints); `ClienteViewModel` con reintento manual (`reintentarSincronizacion`) y bandera de pendiente; `PerfilClienteAdministradorScreen` muestra banner + botón reintentar. `appCliente` `Cliente.kt` pasa a `List<Int>`; parser en `ClienteRepository` y `VinculacionRepository` corregidos. **PRUEBA 9B** (ADMIN update serviciosContratados) → 77/77.
- **Pantalla "Clases de hoy" del Cliente (Fase 7, 28-29/08):** modelo nuevo en `appCliente` (`model/Servicio.kt`, `model/Sesion.kt`), `data/firebase/SesionRepository.kt` (`obtenerServicioActivo`, `obtenerSesionesPorServicio`), `ui/viewmodel/SesionesClienteViewModel.kt` (filtra `fecha == inicioDeHoy()`, orden por hora, estados noVinculado/cargando/error/sinServicios/sinSesionesHoy) y `ui/home/ClasesScreen.kt` funcional. **Reservar/ver/cancelar reservas del CLIENTE aún NO implementado.**
- **Cascadas administrativas de reservas (Fase 8, 28-29/08):** el borrado masivo `batch.delete` anterior fallaba en Rules (`reservaEliminadaValida` exige plazas+1). Sustituido por `runTransaction` por sesión con reintento de query fresca (3 intentos, `MAX_RESERVAS_POR_SESION = 498`), idempotente.
  - **Fase 1 (Android):** `ReservaRemotoRepository.kt` (`eliminarSesionConReservasRemoto`, `eliminarSesionesFuturasConReservasRemoto`, `eliminarTodasLasSesionesConReservasRemoto`); `ServicioViewModel.replicarDesactivacionRemota`/`replicarEliminacionRemota` y `SesionViewModel.eliminarSesion`/`generarSesiones` las usan. `:app:assembleDebug` BUILD SUCCESSFUL.
  - **Fase 2 (Rules + tests):** helper `cascadaEliminaSesion(sesionId)` y rama OR en `reservas/delete` ADMIN. **PRUEBA 77-81** → 82/82 OK. Rules desplegadas verificadas idénticas al local.
- **Diagnóstico de Rules en producción (28-29/08):** se descubrió que el ruleset desplegado **no contenía `match /servicios/{servicioId}`** (Rules antiguas), causa de `PERMISSION_DENIED` en alta/baja de servicios. Tras redeploy, el ruleset coincide con el local. **Aclaración de UID:** el admin real en producción es `aSiZI8YWlLYOWhj2TXlznZWJP5O2` (minúscula `l` en posición 9 y 18: `WlLYO` y `TXlzn`); el `negocioId` de los servicios (`aSiZI8YWlLYOWhj2TXlznZWJP5O2`) es coherente con ese UID. La cadena `aSiZI8YWILYOWhj2TXIznZWJP5O2` (mayúscula `I`) que venía manejándose era un **typo I/l** del humano, no el UID real.
- **Resto validado:** flujo CLIENTE sin vínculo/VÍA 1/VÍA 2, sync nombre de negocio, logo con Storage (pendiente bucket), dos apps.
- **Sincronización de períodos Admin → Firestore (30/08):** `MovimientoRepository` persiste primero en Room, recalcula `fechaInicioActual`/`fechaFinActual` desde los movimientos persistidos y replica el período después de insertar, actualizar o eliminar. Los errores quedan pendientes para reintento manual y `ClienteRemotoRepository` registra la operación y el código de error Firebase. El alta Admin asigna `fechaAlta` a clientes creados como `ACTIVO` y `ClienteViewModel` solo confirma el alta/edición cuando la réplica remota termina correctamente.
- **Vía A código maestro + DNI (30/08):** corregida la regresión del commit `653f117de71a169dcb9f2f75e2dcdf6b6d4c44f5`. `VinculacionRepository` busca exactamente `indices_clientes/{negocioId}_{dni}`; si no existe devuelve `No existe ningún cliente registrado con ese DNI.` y no llama a `crearFicha()`. La vinculación de una ficha existente mantiene la Transaction de `clientes/{idCliente}` + `usuarios/{uid}`. El código de Vía 2 permanece conservado, pero no se ejecuta desde esta entrada Vía A. Añadidas PRUEBA 6B, PRUEBA 6C y test unitario de rechazo.
- **Card de estado del Home Cliente (validado 30/08):** no modificar. Un cliente `ACTIVO` sin movimientos muestra el estado sin fecha; después de crear un movimiento, `fechaInicioActual`/`fechaFinActual` llegan desde Firestore y el card muestra correctamente la fecha de fin del período. La lógica actual queda validada manualmente.
- **`ClaseEntity`/`SesionClaseEntity` y su UI/DAOs/repositorios/ViewModel siguen TRANSITORIOS** (Fase 5B desconectados); NO eliminar sin tarea específica.
- **`horaDesdeReserva` (Sesión XIX, Fase 3/3.1):** campo `String? = null` en `SesionEntity`/`Sesion`/`sesiones/{id}`; Room v12 con `MIGRACION_11_12`; Admin (ProgramarSesionesScreen por día + EditarSesionScreen con TimePicker); appCliente (`reservable`/`aperturaAlcanzada` + botón deshabilitado "Reservas abren a las HH:mm"); Rules `sesiones/create`/`sesiones/update` + capa C en `reservaCreaValida` (`reservaAbierta`/`minutosReserva` con `split(":")` + `int(...)`). `null` = reservas abiertas desde el inicio del día. Tests 99/99.

Pendiente para continuar:

0. **ÍNDICES DE `sesiones`/`reservas` en producción (Sesión XIX):** creados vía API REST en `gestorpro-50e83` (`sesiones(idServicio, negocioId)`, `reservas(clienteId, negocioId)`, `reservas(sesionId, negocioId)`). Confirmar `READY` en consola y **regenerar las sesiones desde el Admin** (Gestionar sesiones → Generar sesiones) para que se repliquen a Firestore. Tras esto, appCliente debe mostrar las clases. Retirar los logs `ClasesDiagnostico` de `SesionesClienteViewModel` al confirmarlo.
1. **PERMISSION_DENIED en alta Admin — EN DIAGNÓSTICO (Sesión XVIII):** el payload de `crearClienteRemoto()` pasa las Rules locales (test `diagnostico_alta_cliente.test.cjs` 7/7). La hipótesis principal es que uno de los 3 documentos del batch (`clientes/{id}`, `indices_clientes/{negocioId}_{dni}`, `clientes_privados/{id}`) **ya existe** en Firestore de un intento anterior, convirtiendo el `batch.set()` en `update` que las Rules deniegan. Siguiente paso: reproducir el alta con el logging temporal `[DIAG alta]` (incluye `existencia previa -> ...=true`) para confirmar cuál documento existe; después limpiar el documento huérfano (con aprobación) o ajustar la réplica, y retirar el logging temporal.
2. **Diagnóstico PERMISSION_DENIED en baja/eliminación de servicios — RESUELTO a nivel de Rules (Sesión XIII):** la causa era que las queries administrativas de cascada (`reservas` por `sesionId`, `sesiones` por `idServicio`) no incluían `negocioId`, así las reglas `sesiones/list` y `reservas/list` las negaban (rules-are-not-filters). Se corrigió en `ReservaRemotoRepository`/`SesionRemotoRepository` (filtro `negocioId` + fail-closed si la sesión no existe pero tiene reservas) y se añadieron 8 pruebas de regresión (PRUEBA 33A–33H). Tests **90/90**, `:app:assembleDebug` BUILD SUCCESSFUL. **Riesgo abierto:** el crash de la app en alta/reactivación no se aisló (no se aportó stacktrace/logcat); conviene validar en dispositivo con el build corregido y, si persiste, capturar el logcat.
3. **Reservas del CLIENTE (implementadas en commits previos a Sesión XIX, pendientes de validación real):** `Reserva` model + `ReservaRepository`/`ReservasClienteViewModel`/`MisReservasScreen` (Transaction `reservas/{clienteId}_{sesionId}`), reservar/cancelar en `ClasesScreen`. Requieren que `sesiones`/`reservas` tengan índices compuestos (creados en Sesión XIX) y que las sesiones se repliquen.
4. **Habilitar el bucket de Storage** y desplegar `storage.rules` (hasta entonces el logo falla).
5. **Backfill de `indices_clientes`** (DRY-RUN: 2 índices). NO ejecutar sin aprobación. Relacionado con el pendiente 1: un índice huérfano podría ser el que provoca el PERMISSION_DENIED del alta.
6. **Limpieza definitiva de `Clase`/`SesionClase`** y de `ServicioItem` (sin uso).
7. **Commits pendientes** (5 archivos del working tree de la Sesión XIX: PerfilClienteAdministradorScreen, DetalleServicioScreen, EditarSesionScreen, ProgramarSesionesScreen, SesionesClienteViewModel) y limpieza de basura versionada (`build_*.txt`, `firestore-tests/firestore-debug.log`).
8. Pendientes heredados: crear negocio con `PERMISSION_DENIED` (hipótesis token) y validar `rol == "ADMIN"` en el login de Admin (hoy solo exige doc existente + activo).

---

# HOJA DE RUTA DEL PROYECTO (2026-09-01)

> Documento vivo. Reconstrucción del estado funcional REAL a partir de arquitectura,
> entidades, DAOs, repositorios, ViewModels, pantallas, Rules, Functions locales y
> tests (no es una búsqueda de TODO/FIXME). Úsalo para tachar funcionalidad por
> funcionalidad sin reinventar ni tocar lo ya terminado. Actualizar al cerrar cada
> bloque.

## 1. Ya implementado y verificado (funciona y compila)

- Dos aplicaciones independientes (`:app` Admin, `:appCliente` Cliente) sobre el mismo Firebase (`gestorpro-50e83`).
- Autenticación real (Firebase Auth) en ambas; roles remotos `ADMIN`/`CLIENTE`.
- Vinculación CLIENTE por **código maestro + DNI** (VÍA 1 y VÍA 2) con `indices_clientes`, `perfiles_pendientes`, `clientes_privados`; sin Vía B/deep links.
- Negocio: crear/editar nombre y código maestro; logo (pendiente bucket).
- **Clientes Admin:** alta, edición, búsqueda, filtros (Todos/Activos/Bajas/Morosos/Archivados), archivar/restaurar, servicios contratados (Room + Firestore write-through), foto local, banners de sincronización con reintento.
- **Servicios/Sesiones/Reservas:** modelo `Cliente → Servicio → Sesión → Reserva`; Room atómico (`withTransaction`, plazas±1, cascadas) + réplica Firestore write-through; generación de sesiones con apertura global y edición individual; índices compuestos creados en producción.
- **Economía base:** movimientos y gastos en Room; `fechaInicioActual`/`fechaFinActual` replicados a `clientes/{id}`; morosidad derivada (Room); `EconomiaScreen` (resumen + CRUD de gastos + lectura de movimientos); movimientos por cliente (crear/editar/eliminar/renovar).
- **Notificaciones (Fases B/C/D):** Admin (lista, crear individual/grupo/todos/programadas, configuración de preconfiguradas, selección grupal), bandeja del cliente, leído/no leído, toggle por dispositivo, buzón `notificaciones_por_destinatario`.
- **Solicitudes de baja:** flujo completo Cliente → PENDIENTE → Admin (Aceptar=BAJA / Rechazar) + notificación `BAJA_CONFIRMADA` (si config activa).
- **Baja de cliente (corregida):** navegación a `CuentaScreen` ("Mi cuenta" desde Configuración); bloqueo de BAJA en app (SesionesCliente/ReservaRepository/Home) y en Rules (`clientePuedeAcceder` en sesiones get/list y `reservaCreaValida`); baja directa y aceptación de solicitud convergen en `BajaClienteRemotoRepository` (cancelación de reservas futuras Room+Firestore liberando plazas, conservación de pasadas y `serviciosContratados`, BAJA_CONFIRMADA con ID determinista).
- **Rules Firestore+Storage:** **123/123** (`npm --prefix firestore-tests test`).
- Tests helpers de Functions: `node --test functions/test/ids.test.js functions/test/tokens.test.js` → **13/13**.

## 2. Implementado pero pendiente de pruebas reales (dispositivo / producción)

- **Reservas del CLIENTE:** `ReservaRepository` (Transactions atómicas) y `ReservasClienteViewModel` cableados en `ClasesScreen` (reservar/cancelar con confirmación). Requieren índices `READY` y sesiones replicadas. **No hay pantalla "Mis reservas"** (`reservasVisibles`/`esProxima()` sin consumidor).
- **Réplica Room→Firestore de sesiones:** conectada (`sincronizarSesionesGeneradas`), pero hubo fallo silencioso por `idSesion=0` (fix local en working tree); validar regenerando sesiones y confirmando documentos en Firestore.
- **Alta Admin con `[DIAG alta]`:** pendiente de confirmar la causa (hipótesis: documento huérfano ya existente en el batch); retirar logging al cerrar.
- **Logo Storage:** implementado (`NegocioRepository.guardarLogoRemoto`), bloqueado por bucket (ver §5).

## 3. Implementado parcialmente

- **ECONOMÍA (bloque crítico pendiente de decidir/cerrar):**
  - No existe entidad Pago/Cuota: el "pago" es `Movimiento.estado` (PENDIENTE/PAGADO) + `fechaPago`.
  - **Bug conocido:** al editar un movimiento desde el perfil, `fechaPago` se resetea a `null` (se reconstruye el objeto sin ese campo).
  - `fechaPago` solo se rellena en "Renovar". No hay métodos de pago (efectivo/Bizum/transferencia) ni validación de pago.
  - No existen reglas de negocio económicas: descuentos, tramos, tarifas (estudiante/familia/jubilado/llave como tarifa), cargo de alta, prorrateo a mitad de mes, "cuarto día hábil".
  - Morosidad 100% derivada (Room): ACTIVO con `fechaFin < ahora`, o BAJA con movimiento PENDIENTE. `EstadoCliente.MOROSO` nunca se persiste; sin fecha de entrada en moroso.
  - Replicación a Firestore SOLO de `fechaInicioActual`/`fechaFinActual`; **no existe** colección remota `movimientos`/`gastos`. Insuficiente para que Cloud Functions calcule morosidad/precio con precisión.
  - `EconomiaScreen` es solo lectura para movimientos (gestión en el perfil del cliente).
- **Reservas ADMIN:** `SesionReservasScreen` es solo lectura; el ADMIN no puede cancelar la reserva de un cliente (solo cascadas de servicios/sesiones).
- **Sesiones:** `eliminarSesion` (ViewModel + remoto) existe pero **sin botón en la UI** (`EditarSesionScreen` solo guarda cambios).
- **Clientes:** `eliminarCliente` (Room) sin botón en UI (baja lógica por diseño). "Dar de baja" directo solo vía switch de edición o aceptar solicitud. Reactivación `BAJA→ACTIVO` solo vía switch (no hay método dedicado).
- **Cuenta Admin:** el diálogo "Cambiar contraseña" es un **placeholder** (no llama a `FirebaseAuth.updatePassword`).
- **appCliente economía:** solo indicador de estado en Home (`PAGO_VENCIDO` derivado de `fechaFinActual`); **sin** módulo de cuotas/movimientos/pagos (¿deseado? decisión de producto).
- **Persistencia/sincronización:** entidad por entidad hay diferencias (ver §10): cliente/servicio/sesión/reserva write-through; movimiento parcial; gasto solo Room; solicitud antigua Room inerte + nueva Firestore; notificación/dispositivo Firestore.

## 4. Pendiente de implementar (funcional)

- Decidir y cerrar el **circuito económico completo** (cuota → movimiento → pago → morosidad → baja) y su replicación para Functions.
- Pantalla **"Mis reservas"** del CLIENTE (si se aprueba).
- **Cancelar reserva desde ADMIN** (`SesionReservasScreen`).
- Botón **eliminar sesión** en `EditarSesionScreen`.
- **Cambiar contraseña real** en Cuenta Admin.
- (Si se aprueba) módulo económico del CLIENTE (deuda/cuotas/historial).
- Auditorías finales: seguridad (aislamiento negocio/cliente) y persistencia/sincronización entidad por entidad.

## 5. Bloqueado por Blaze/Firebase (infraestructura real)

- **Cloud Functions 2ª gen** (código local listo en `functions/`): inmediatas, programadas (onSchedule 2 min), recordatorio morosidad (1 h), morosidad, baja confirmada. Requiere: Blaze → índice `notificaciones(estado ASC, fechaProgramada ASC)` → `npm install` en `functions/` → `firebase deploy --only functions`.
- **FCM real** (push a dispositivo).
- **Firebase Storage:** bucket + `storage.rules` (logo; fotos de clientes preparadas en rules locales, sin desplegar).
- **Backfill de `indices_clientes`** (solo con aprobación; DRY-RUN: 2 índices).
- (Opcional) migración de fotos locales → Storage (`rutaFotoClienteEnStorage`).

## 6. Decisiones pendientes (el desarrollador debe decidir ANTES de programar)

1. **Economía (prioritario):** ¿cuál es la fuente de verdad del movimiento (Room, Firestore o ambas)? ¿Replicar más datos económicos a Firestore (colección `movimientos` o flag `moroso` + `fechaEntradaMoroso`) para que Functions calcule morosidad? ¿Regla real de entrada/salida de MOROSO (se acordó "cuarto día hábil")? ¿Pago como entidad propia con método de pago, o mantener estado+fechaPago?
2. **BAJA + deuda:** ¿basta `BAJA` + deuda pendiente (movimiento PENDIENTE) o se necesita estado `MOROSO_BAJA`? ¿Cómo afecta a notificaciones? (hoy la morosidad BAJA+PENDIENTE no es replicable desde Firestore).
3. **appCliente economía:** ¿debe tener cuotas/movimientos/pagos o solo el estado/deuda derivado?
4. **Reservas:** ¿pantalla "Mis reservas"? ¿el ADMIN puede cancelar reservas desde `SesionReservasScreen`?
5. **Sesiones:** ¿botón eliminar sesión en la UI?
6. **Fotos:** ¿migrar a Storage cuando haya bucket?
7. **Backfill de índices** (aprobación explícita).

## 7. Orden recomendado de implementación

1. **ECONOMÍA:** auditoría + decisiones (§6.1/6.2) → cerrar circuito (pagos, reglas, morosidad real) → replicación a Firestore para Functions.
2. **Reservas del CLIENTE:** validar índices/sesiones, pantalla "Mis reservas" (si aprobada), retirar logs `ClasesDiagnostico`.
3. **Cierres menores Admin:** eliminar sesión, cancelar reserva admin, cambiar contraseña, reactivar desde BAJA.
4. **Auditorías:** seguridad + persistencia/sincronización entidad por entidad + completitud funcional Admin.
5. **Blaze:** activar facturación → índice programadas → `npm install` → deploy Functions + `storage.rules` → FCM real → Storage (logo/fotos).
6. **Pruebas finales integradas** (admin+cliente+varios dispositivos, estados, morosidad, baja, reservas, notificaciones).

## 8. Dependencias entre funcionalidades

- Morosidad/recordatorio (Functions) dependen de datos en Firestore: hoy solo `fechaFinActual`; si se exige la regla acordada, hay que replicar más o persistir `moroso`.
- `BAJA_CONFIRMADA` depende de: config `bajaConfirmada.activa` + `clientes/{id}.estado == "BAJA"` (ya conectado por solicitud y preparado en Functions).
- Notificaciones programadas dependen del índice `notificaciones(estado, fechaProgramada)` + Blaze.
- Reservas del CLIENTE dependen de índices `sesiones`/`reservas` READY + sesiones replicadas.
- Solicitud de baja → BAJA → BAJA_CONFIRMADA ya conectado (ID determinista compartido con Functions).
- Fotos en Storage dependen del bucket; la app sigue funcionando con fotos locales hasta entonces.

## 9. Tests existentes y qué cubren

- **Rules Firestore+Storage — 123/123** (`npm --prefix firestore-tests test`):
  - PRUEBA 1–18: clientes, permisos, VÍA 1/VÍA 2, índices, `perfiles_pendientes`, `clientes_privados`, `negocios_publicos`, cambio de DNI.
  - PRUEBA 19–20: Storage logo + `negocios_publicos` logo.
  - PRUEBA 21–33: servicios (CRUD + aislamiento negocio).
  - PRUEBA 33A–33H: queries admin con `negocioId` (sesiones/reservas) + cascadas.
  - PRUEBA 34–53: sesiones (CRUD, servicio activo/contratado, apertura).
  - PRUEBA 54–76: reservas (crear/cancelar, plazas±1, transacciones atómicas, duplicado, sesión llena).
  - PRUEBA 77–81: cascadas administrativas de reservas/sesiones.
  - PRUEBA 82–88: `horaDesdeReserva`.
  - PRUEBA 89–98: notificaciones, `configuracion_notificaciones`, buzón, token FCM por dispositivo.
  - PRUEBA 99–108: solicitudes de baja (create/get/list/aceptar/rechazar, aislamiento, datos inválidos).
  - PRUEBA 109–112: bloqueo de BAJA (no lee/no lista sesiones, no crea reserva; ACTIVO sigue permitido).
- **Functions helpers:** `functions/test/ids.test.js` + `tokens.test.js` (13/13) — IDs deterministas y clasificación de tokens/lotes.
- **Unit appCliente:** rechazo de VÍA A cuando no existe el índice `negocioId_DNI`.
- **Diagnóstico (temporal):** `firestore-tests/diagnostico_alta_cliente.test.cjs` (7/7).
- Los tests de Android funcionales se reservan para la fase final (decisión del proyecto).

## 10. Problemas técnicos conocidos

- Editar un movimiento resetea `fechaPago` a `null`.
- Rules no pueden hacer cross-document query → el duplicado de solicitud PENDIENTE se controla en el repositorio (capa de negocio), no solo en la UI.
- Réplica de sesiones Room→Firestore estuvo rota por `idSesion=0` (fix en working tree; validar).
- Alta Admin `PERMISSION_DENIED` en diagnóstico (posible documento huérfano en el batch; logging `[DIAG alta]`).
- Crear negocio con `PERMISSION_DENIED` (hipótesis token) — histórico sin resolver.
- Login Admin no valida `rol == "ADMIN"` (solo exige doc existente + activo).
- `EstadoCliente.MOROSO` nunca se persiste; appCliente lo rechaza si llegara remoto.
- `Clase`/`SesionClase`/`ServicioItem` y las rutas legadas `ui/clases` siguen TRANSITORIOS (no eliminar sin tarea específica).
- `TipoSolicitud` (Room) sigue con `CLASE`/`BAJA` (deuda: adaptar a `ALTA`/`BAJA` cuando se decida sobre la tabla `solicitud` antigua, inerte).
- Fotos locales (`filesDir/fotos`) hasta decidir migración a Storage.
- Basura versionada: `firestore-tests/firestore-debug.log` (emulador) y posibles `build_*.txt`.
