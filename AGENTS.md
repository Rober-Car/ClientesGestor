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

- **Rules de Firestore + Storage:** `npm --prefix firestore-tests test` (**90 pruebas** en los emuladores `--only firestore,storage`: 20 de clientes/vinculación/negocio/logo + 13 de servicios + 20 de sesiones + 23 de reservas + 14 de regresión de cascadas/queries/activación, incluidas PRUEBA 9B, 33A–33H, 77–81). Deben pasar **antes** de desplegar las Rules.
- Los tests de Android se mantienen para la fase final del proyecto salvo que el desarrollador los solicite expresamente antes. No crear archivos de test automáticamente durante una funcionalidad normal.

## Convenciones específicas de Firebase y navegación

- **Recuperación de contraseña:** usar exclusivamente `FirebaseAuth.sendPasswordResetEmail`. El mensaje de éxito debe ser **genérico** ("Si el email existe, recibirás un enlace…") para no revelar qué cuentas existen; ante errores de autenticación (usuario inexistente, email inválido…) se responde con el mismo mensaje genérico. Solo se comunican fallos reales (p. ej. sin conexión). Validar email no vacío y formato antes de llamar a Firebase (`android.util.Patterns.EMAIL_ADDRESS`).
- **Rutas con parámetros de query:** construir siempre sustituyendo el placeholder, nunca concatenando. En GestorPro Cliente no existen rutas con query (la Vía B está descartada); si se añade una ruta con placeholder, usar `Ruta.replace("{param}", valor)`.
- **Fotos:** la lógica de guardado vive en `ui/utils/FotoUtils.kt` (`guardaFotoEnInterna`; además `crearFotoTemporal`, `uriDeFotoTemporal`, `guardarFotoDeCamara` en Admin y también en appCliente). No duplicar esa función en pantallas. La cámara usa `TakePicture()` con `FileProvider` (`${applicationId}.fileprovider`, `res/xml/file_paths.xml`); el guardado se hace solo en el callback del resultado, nunca justo después de `launch()`. El selector común es `ui/components/BotonSelectorFoto.kt`.
- **Vinculación del CLIENTE:** el flujo de código maestro + DNI vive en `appCliente` (repositorio `VinculacionRepository`, pantalla de vinculación accesible desde el Home). Las operaciones críticas (VÍA 1 y VÍA 2) deben ejecutarse en Transaction. Nunca reintroducir Vía B/deep links.
- **Logo del negocio:** la subida vive en `NegocioRepository.guardarLogoRemoto()` (`:app`): `putFile` a `negocios/{uid}/logo.jpg` → `downloadUrl` → WriteBatch con `logo` en `negocios` + `negocios_publicos`. El Cliente lee `negocios_publicos/{id}.logo` y lo muestra con Coil. Requiere el bucket habilitado en Firebase Console.

## Estado actual y pendientes (2026-08-29)

> ACTUALIZACIÓN: lo implementado el 28-29/08 (DeepSeek) y el 29/08 (corrección PERMISSION_DENIED + auditoría appCliente) está en el working tree **sin commit**. Tests de Rules en **90/90** y las Rules desplegadas en `gestorpro-50e83` son **byte-idénticas** al local `firestore.rules` (42.687 bytes, con `cascadaEliminaSesion` y `match /servicios/{servicioId}`). Detalle en `CONVERSACION_EXPORTADA.md` (Sesiones XII–XIV).

Implementado y compilado (`:app` y `:appCliente` BUILD SUCCESSFUL; `:app:compileDebugKotlin` EXITCODE 0; Rules **90/90 OK**):

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
- **`ClaseEntity`/`SesionClaseEntity` y su UI/DAOs/repositorios/ViewModel siguen TRANSITORIOS** (Fase 5B desconectados); NO eliminar sin tarea específica.

Pendiente para continuar:

1. **Diagnóstico PERMISSION_DENIED en baja/eliminación de servicios — RESUELTO a nivel de Rules (Sesión XIII):** la causa era que las queries administrativas de cascada (`reservas` por `sesionId`, `sesiones` por `idServicio`) no incluían `negocioId`, así las reglas `sesiones/list` y `reservas/list` las negaban (rules-are-not-filters). Se corrigió en `ReservaRemotoRepository`/`SesionRemotoRepository` (filtro `negocioId` + fail-closed si la sesión no existe pero tiene reservas) y se añadieron 8 pruebas de regresión (PRUEBA 33A–33H). Tests **90/90**, `:app:assembleDebug` BUILD SUCCESSFUL. **Riesgo abierto:** el crash de la app en alta/reactivación no se aisló (no se aportó stacktrace/logcat); conviene validar en dispositivo con el build corregido y, si persiste, capturar el logcat.
2. **Reservar/ver/cancelar reservas del CLIENTE + indicador de estado real en Home (Sesión XIV, plan listo):** reusar la Transaction `reservas/{clienteId}_{sesionId}`; añadir `Reserva` model/`ReservaRepository`/`ReservasClienteViewModel`/`MisReservasScreen`; `SesionRepository` debe filtrar por `negocioId`; `ClienteRepository` debe parsear `Timestamp` (hoy solo `Number`) y `MainViewModel`/`HomeScreen` mostrar el estado real derivado (ACTIVO/BAJA/ARCHIVADO/REGISTRADO + MOROSO/PAGO_VENCIDO derivado de `fechaFinActual`). Sin implementar aún (pendiente autorización; ver Partes 1–5 de la Sesión XIV). En Admin: poblar `fechaInicioActual`/`fechaFinActual` desde el `Movimiento` vigente y set `fechaBaja` al dar de baja.
3. **Habilitar el bucket de Storage** y desplegar `storage.rules` (hasta entonces el logo falla).
4. **Backfill de `indices_clientes`** (DRY-RUN: 2 índices). NO ejecutar sin aprobación.
5. **Limpieza definitiva de `Clase`/`SesionClase`** y de `ServicioItem` (sin uso).
6. **Commits pendientes** (todo el working tree: dos apps, Rules, tests, docs) y limpieza de basura versionada (`build_*.txt`, `firestore-tests/firestore-debug.log`).
7. Pendientes heredados: crear negocio con `PERMISSION_DENIED` (hipótesis token) y validar `rol == "ADMIN"` en el login de Admin (hoy solo exige doc existente + activo).
