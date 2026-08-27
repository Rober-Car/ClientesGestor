# AUDITORÍA PROYECTO MIGRACIÓN KMP — GestorPro

> **⚠️ DOCUMENTO HISTÓRICO / OBSOLETO (2026-08-27):** este informe describe el
> proyecto ANTES de la arquitectura definitiva de dos aplicaciones. A partir de la
> Sesión VIII, GestorPro se divide en **`:app` (Admin)** y **`:appCliente` (Cliente)**,
> la **Vía B (enlace/deep link) está DESCARTADA** y `firestore.rules` se reescribió
> (`indices_clientes`, `perfiles_pendientes`, `clientes_privados`, VÍA 1/VÍA 2 por
> código maestro + DNI). La migración a KMP/CMP no está activa; si se retoma, debe
> auditarse de nuevo sobre la arquitectura vigente. Ver `AGENTS.md`.

> **Fecha:** 2026-08-26
> **Objetivo:** inventario técnico completo y verificado del proyecto Android GestorPro, como base para diseñar una estrategia de migración a **Kotlin Multiplatform (KMP) + Compose Multiplatform (CMP)** conservando la app Android actual.
> **Alcance:** solo análisis y documentación. No se ha modificado ningún archivo del proyecto.
> **Contexto:** coherente con `AGENTS.md` (contrato técnico) e `CONVERSACION_EXPORTADA.md` (historial de sesiones I–V).

---

## 1. RESUMEN GENERAL

| Campo | Valor |
|---|---|
| Nombre del proyecto | **GestorPro** |
| Descripción | App Android para gestionar clientes, clases, sesiones, reservas, cuotas/movimientos, gastos y datos económicos de un negocio deportivo. Doble perfil: ADMINISTRADOR (dueño del negocio) y CLIENTE. |
| Estado actual | **Funcional en desarrollo activo.** Login/registro reales con Firebase Auth probados en dispositivo físico; vinculación cliente↔negocio (2 vías) implementada con Firestore + Security Rules desplegadas en producción (`gestorpro-50e83`), 17/17 pruebas de Rules OK en emulador. Pendiente: pruebas manuales de integración completas, migraciones Room reales (hoy destructivas), adaptar `TipoSolicitud.CLASE` → `ALTA`. |
| Arquitectura | MVVM con repositorios (UI Compose → ViewModel → Repository → Room/DataStore/Firebase). **Sin capa de use cases ni Clean Architecture estricta.** |
| Número de módulos Gradle | **1** (`:app`). Proyecto auxiliar Node.js (`firestore-tests/`, no Gradle) para las Security Rules con emulador. |
| Package / applicationId | `com.roberto.gestorpro` |
| Versión de Kotlin | **2.2.10**. Con AGP 9.x el Kotlin va integrado ("built-in"); no existe plugin `org.jetbrains.kotlin.android` explícito en el catálogo. Plugin Compose: `org.jetbrains.kotlin.plugin.compose` 2.2.10. |
| Android Gradle Plugin | **9.1.1** |
| Versión de Gradle | **9.3.1** (wrapper) |
| compileSdk | **36.1** — `compileSdk { version = release(36) { minorApiLevel = 1 } }` |
| minSdk | **26** |
| targetSdk | **36** |
| Java/JDK | Toolchain del daemon Gradle: **JDK 21** (`gradle/gradle-daemon-jvm.properties`, `toolchainVersion=21`). `compileOptions`: source/target compatibility **Java 11**. |
| Versión de Compose | BOM **2026.02.01** (ui, ui-graphics, material3, material-icons-extended, tooling) |
| Otras versiones | Navigation Compose 2.9.3 · Room 2.8.4 (KSP `2.3.6`) · Hilt 2.60.1 · hilt-navigation-compose 1.2.0 · DataStore Preferences 1.1.7 · Coil 3.3.0 (`io.coil-kt.coil3`) · Gson 2.11.0 · Firebase BOM 34.16.0 (Auth + Firestore) · google-services plugin 4.5.0 |

Observaciones iniciales para KMP:

- Al no existir plugin Kotlin explícito ni `kotlin { jvmToolchain() }`, la conversión del módulo `:app` a un módulo KMP exigirá reescribir la configuración de plugins (punto crítico con AGP 9, muy nuevo).
- **Coil 3** ya es multiplataforma (buena noticia).
- **DataStore Preferences 1.1.7** dispone de artefactos multiplataforma (`datastore-preferences-core`).

---

## 2. ESTRUCTURA DEL PROYECTO

```text
GestorPro/
├── settings.gradle.kts              # include(":app") — único módulo
├── build.gradle.kts                 # Plugins raíz (AGP, compose, ksp, hilt, google-services)
├── gradle/
│   ├── libs.versions.toml           # Catálogo de versiones/dependencias
│   ├── gradle-daemon-jvm.properties # JDK 21
│   └── wrapper/                     # Gradle 9.3.1
├── app/                             # ÚNICO módulo Android
│   ├── build.gradle.kts             # SDKs, buildTypes (release sin minify), dependencias
│   ├── proguard-rules.pro
│   ├── google-services.json         # NO versionado (.gitignore: **/google-services.json)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml  # SIN permisos; deep link gestorpro://vincular/{token}
│       │   └── java/com/roberto/gestorpro/
│       │       ├── GestorProApplication.kt      # @HiltAndroidApp
│       │       ├── MainActivity.kt              # Activity única + deep link + setContent
│       │       ├── navigation/                  # Routes.kt, AppNavigation.kt, EnlacePendiente.kt
│       │       ├── model/                       # Cliente + enums + proyecciones (9 archivos)
│       │       ├── data/
│       │       │   ├── entity/     (7 entidades Room)
│       │       │   ├── dao/        (7 DAOs)
│       │       │   ├── converter/  (5 TypeConverters)
│       │       │   ├── database/   ClientesDatabase.kt (v10)
│       │       │   ├── repository/ (7 repos locales Room + PreferencesRepository DataStore)
│       │       │   ├── firebase/   (Autenticacion, Negocio, Vinculacion, ClienteRemoto)
│       │       │   └── export/     ExportManager.kt (JSON con Gson)
│       │       ├── di/AppModule.kt
│       │       └── ui/
│       │           ├── auth/          SeleccionTipoUsuario, Login, Registro, VincularCliente
│       │           ├── home/          HomeScreen, HomeClienteScreen
│       │           ├── clientes/      Clientes, AñadirCliente, PerfilClienteAdministrador,
│       │           │                  MiPerfil, EnlaceVinculacion (+EnlaceVinculacionViewModel)
│       │           ├── clases/        Clases, CrearClase, DetalleClase, DetalleSesionReservas
│       │           ├── economia/      EconomiaScreen
│       │           ├── configuracion/ Configuracion, Cuenta, Preferencias, Datos, MiNegocio,
│       │           │                  CrearNegocio
│       │           ├── components/    MenuCard, ClienteItem, MovimientoItem, ResumenCard, ServicioItem
│       │           ├── utils/FotoUtils.kt          # Guardado fotos (Android-only)
│       │           ├── theme/         Theme.kt, Color.kt, Type.kt
│       │           └── viewmodel/     Main, Cliente, Clase, Movimiento, Gasto, Economia,
│       │                              Datos, Preferencias (8 ViewModels)
│       ├── res/                     # strings (1 string), colors, themes, mipmaps, drawables
│       ├── test/java/...            # ExampleUnitTest.kt (plantilla)
│       └── androidTest/java/...     # ExampleInstrumentedTest.kt (plantilla)
├── firestore.rules                  # Security Rules versionadas (743 líneas)
├── firestore-tests/                 # Suite Node.js emulador (17 pruebas) + node_modules
├── firebase.json / .firebaserc      # Config deploy Rules (proyecto gestorpro-50e83)
├── AGENTS.md / CONVERSACION_EXPORTADA.md / EXPLICACION_BASE_DE_DATOS.html
```

**Total código:** 87 archivos `.kt` en `main`; 2 tests de plantilla.

---

## 3. ARQUITECTURA

**Patrón vigente: MVVM con repositorios.**

```text
UI (Compose Screens)
  ↓ observa StateFlow / emite acciones de usuario
ViewModel (@HiltViewModel, viewModelScope, StateFlow/MutableStateFlow)
  ↓ invoca suspend fun / Flow
Repository (clases concretas @Singleton/@Inject, sin interfaces)
  ↓ usa
DataSources:
  - DAOs Room            (Flow reactivo + suspend)
  - PreferencesRepository (DataStore Preferences)
  - Repositorios Firebase (Auth/Firestore, suspend vía puente Task→corrutina)
```

Características verificadas:

- **NO hay use cases** ni capa `domain`.
- **NO hay interfaces de repositorio**: los ViewModels dependen de clases concretas.
- DI centralizada en Hilt `AppModule` (§6).
- Estado UI: `StateFlow` con `stateIn(viewModelScope, WhileSubscribed(5000), initial)`; consultas Room reactivas con `Flow`.
- Flujo híbrido local/remoto: Room = fuente de verdad local; Firestore recibe *write-through* solo de clientes del ADMIN, con reintento manual (`ClienteViewModel.replicar()`). Sin listeners remotos ni cola offline.
- Deuda declarada: `DatosViewModel` inyecta `Context` y DAOs directamente; strings hardcodeados; 9 usos de `collectAsState()` vs 57 de `collectAsStateWithLifecycle()`; sin `!!` detectado en el código actual (búsqueda = 0 resultados en main).

---

## 4. BASE DE DATOS LOCAL (ROOM)

### 4.1 Base de datos e inicialización

- `ClientesDatabase` (`data/database/`): `@Database(version = 10)`, 7 entidades, 5 conversores globales, 7 accessors DAO.
- Construcción en `AppModule.provideDatabase(@ApplicationContext context)`:
  - `Room.databaseBuilder(context, ClientesDatabase::class.java, "gestorpro_database")`
  - **`fallbackToDestructiveMigration()`** — TODO(PRODUCCION) propio del código: subir versión recrea BD (se usó para evitar restauraciones antiguas de backup de Google).
  - `RoomDatabase.Callback.onCreate` que ejecuta `db.execSQL(...)` de **datos de prueba** (20 clientes, movimientos, gastos ficticios) — TODO(PRODUCCION) pendiente de eliminar.
- **Migrations explícitas: NO PRESENTES.**
- Consultas: SQL en anotaciones `@Query`; lecturas reactivas `Flow<List<T>>` y puntuales `suspend`.

### 4.2 Entidades

| Entidad (tabla) | PK | Campos clave | Notas |
|---|---|---|---|
| `ClienteEntity` (`cliente`) | idCliente Int autogenerada | nombre, apellidos, dni (índice único), telefono, email?, foto (ruta), fechaNacimiento Long, fechaRegistro Long (default `System.currentTimeMillis()`), fechaAlta/Baja?, estado: EstadoCliente, tieneLlave Boolean, observaciones?, negocioId?, serviciosContratados List\<String\>, firebaseUid? | Extensión `toCliente(): Cliente` hacia modelo UI (concatena nombre+apellidos) |
| `MovimientoEntity` (`movimiento`) | idMovimiento | idCliente, servicio, fechaInicio/fechaFin Long, precio Double, estado: EstadoMovimiento, fechaPago?, observaciones? | Cuotas/servicios; base del cálculo MOROSO |
| `GastoEntity` (`gasto`) | idGasto | concepto, importe Double, fecha Long, observaciones? | |
| `ClaseEntity` (`clase`) | idClase | negocioId, nombre, servicio, diasSemana String, horaInicio String, duracionMinutos, capacidadMaxima, horaAperturaReservas String, fechaInicio/fin Long, activa Boolean | Horarios como texto |
| `SesionClaseEntity` (`sesion_clase`) | idSesion | negocioId, idClase, servicio, fecha Long, plazasDisponibles Int | Instancia concreta |
| `ReservaEntity` (`reserva`) | idReserva | negocioId, idSesion, idCliente, fechaReserva | Índice único (idSesion,idCliente); default fecha `System.currentTimeMillis()` |
| `SolicitudEntity` (`solicitud`) | idSolicitud | negocioId="", idCliente (índice), tipo: TipoSolicitud (**CLASE/BAJA** — desajuste con contrato remoto ALTA/BAJA), estado: EstadoSolicitud default PENDIENTE, detalle List\<String\>, fechaCreacion | Conversor local de listas |

### 4.3 DAOs (operaciones destacadas)

- **ClienteDao**: insert/update/delete suspend; `obtenerClientesDao(): Flow`; porDni/porId/porEstado; `obtenerIdsMorosos(ahora): Flow<List<Int>>` (JOIN cliente×movimiento con regla ACTIVO+fechaFin vencida o BAJA+PENDIENTE); `obtenerTodosLosClientesSync()`; vaciado total.
- **MovimientoDao**: CRUD; Flows por cliente/estado/todos ordenados por fechaInicio DESC; sync; vaciado.
- **GastoDao**: CRUD; Flow todos por fecha DESC; sync; vaciado.
- **ClaseDao**: CRUD; Flow todas/activas ORDER BY nombre; porId; vaciado.
- **SesionClaseDao**: inserts individual/lote; Flows por clase y por clase desde fecha; porId; **UPDATE atómico de plazas** (`reservarPlaza`: decrementa si >0, devuelve filas afectadas; `liberarPlaza`); eliminar por clase; **JOIN clase** `obtenerSesionesActivasConClase(desde,hasta): Flow<List<SesionConClase>>`; vaciado.
- **ReservaDao**: insert REPLACE; Flow/sync por sesión; obtener reserva puntual; cancelar (DELETE por par); **JOIN cliente** `obtenerReservasConCliente(idSesion): List<ReservaConCliente>`; vaciado.
- **SolicitudDao**: CRUD; Flows todas/porCliente DESC; contador por estado `Flow<Int>`; vaciado.

### 4.4 TypeConverters

| Converter | Conversión | Mecanismo |
|---|---|---|
| `StringListConverter` | List\<String\> ⇄ String | join/split por comas |
| `EstadoClienteConverter` | enum ⇄ name | `name`/`valueOf` |
| `EstadoMovimientoConverter` | enum ⇄ name | ídem |
| `TipoSolicitudConverter` | enum ⇄ name | ídem |
| `EstadoSolicitudConverter` | enum ⇄ name | ídem |

### 4.5 Repositorios Room

Los 7 repositorios (`Cliente/Movimiento/Gasto/Clase/SesionClase/Reserva/Solicitud`) son **wrappers finos 1:1 sobre su DAO** (`@Inject constructor(dao)`), sin lógica adicional. `PreferencesRepository` encapsula DataStore (tema, tipoUsuario, nombreNegocio, logoNegocio, idClienteSesion) usando el delegado `preferencesDataStore` sobre `Context`.

### 4.6 Datos locales y sincronización

- Todo el negocio vive **solo en Room**; preferencias en DataStore.
- **Caché explícita: NO PRESENTE.**
- **Sincronización Firestore:** únicamente write-through ADMIN→`clientes/{id}` (mismo idCliente compartido Room/Firestore); fallo ⇒ se conserva local + aviso + botón "Reintentar sincronización". No hay pull remoto→local ni listeners. Clases/sesiones/reservas/solicitudes aún no tocan la nube desde Android.

### 4.7 Clasificación KMP de Room

| Parte | KMP |
|---|---|
| Entidades/enums/proyecciones (data classes puras) | **A** — Room ≥2.7 soporta KMP en commonMain |
| Converters (Kotlin puro) | **A** |
| DAOs con SQL estándar (SELECT/INSERT/UPDATE/DELETE, JOINs simples, Flow) | **B** — verificar soporte exacto de UPDATE-con-retorno y Flow en target iOS |
| Defaults `System.currentTimeMillis()` en entidades | **B** — inyectar reloj o usar kotlinx-datetime |
| Callback onCreate con semillas execSQL + fallback destructivo | **C** — adaptar al builder multiplataforma |
| Builder con `Context` (AppModule) | **D/E** — en KMP no aplica Context Android; factory expect/actual o driver nativo |
---

## 5. FIREBASE Y FIRESTORE

### 5.1 Productos utilizados

| Producto | ¿Usado? | Detalle |
|---|---|---|
| Firebase Authentication | **SÍ** | Email/contraseña: registro (con rollback de cuenta si falla el perfil remoto), login con comprobación `usuarios/{uid}.activo`, logout, sesión persistida por el SDK |
| Cloud Firestore | **SÍ** | usuarios, negocios, negocios_publicos, clientes, vinculaciones |
| Storage / Cloud Messaging / Analytics / Crashlytics / Realtime DB / Functions | **NO PRESENTE** | — |

Dependencias: `firebase-bom 34.16.0` + `firebase-auth` + `firebase-firestore`; plugin `com.google.gms.google-services` 4.5.0.

### 5.2 Colecciones y operaciones en código

| Colección | Operaciones Android actuales | Repositorio |
|---|---|---|
| `usuarios/{uid}` | set alta registro; get en login/vinculación; update batch (clienteId/negocioId) | Autenticacion, Negocio, Vinculacion |
| `negocios/{negocioId=uid del ADMIN}` | get existencia/codigoMaestro; set/update batch | NegocioRepository |
| `negocios_publicos/{id}` | query `whereEqualTo("codigoMaestro", x).limit(1)`; set/update batch | Negocio, Vinculacion |
| `clientes/{idCliente}` | set réplica alta; update réplica edición; get existencia/codigoVinculacion; update batch consumo vínculo | ClienteRemoto, Vinculacion |
| `vinculaciones/{token}` | get; set PENDIENTE; update PENDIENTE→USADA; delete (revocar/regenerar) | VinculacionRepository |
| clases / sesiones / reservas / solicitudes / movimientos | **Definidas en Rules pero sin código Android que las use todavía** | — |

### 5.3 Patrones técnicos

- **Puente Task→corrutina artesanal**: `internal suspend fun <T> Task<T>.esperar()` (`suspendCancellableCoroutine`) en `AutenticacionRepository.kt`. Todas las llamadas remotas son `suspend`.
- **Listeners/snapshots: NO PRESENTES** — no existe ningún `addSnapshotListener` en el proyecto; todo es lectura/escritura puntual.
- **Atomicidad**: `WriteBatch` (crear negocio, código maestro, generar/regenerar/revocar enlace, consumir enlace Vía B) y `runTransaction` (Vía A: comprobar colisión idCliente aleatorio → crear ficha → actualizar usuario; reintento máx. 5 con excepción interna `ColisionIdClienteException`).
- **Modelado**: documentos como `Map<String, Any?>` manuales (sin DTOs tipados); fechas `Timestamp(Date(millis))`; `FieldValue.serverTimestamp()` una vez.
- **Errores traducidos a español** (`mensajeDe(e)` en cada repositorio).
- Generación segura: token individual SecureRandom 24 chars sin ambiguos; idCliente aleatorio `Random.nextInt(1_000_000_000, Int.MAX_VALUE)`.

### 5.4 Security Rules

- `firestore.rules`: deny-all por defecto; helpers de validación atómica (`vinculacionValidaParaConsumo`, `creacionDirectaValida`, `asignacionTokenValida`, `revocacionTokenValida`, `sesionAccesiblePorCliente`, etc.). Desplegadas en producción `gestorpro-50e83`.
- Suite Node.js `firestore-tests/` (17 pruebas, emulador). Las Rules son server-side: **se reutilizan tal cual para iOS**.

### 5.5 Dependencias Android del código Firebase

Los 4 repositorios dependen de: `FirebaseAuth`/`FirebaseFirestore` (singletons vía Hilt), `com.google.android.gms.tasks.Task`, `com.google.firebase.Timestamp`/`FieldValue` y excepciones `FirebaseAuth*Exception`. **Ninguna pantalla toca Firebase directamente**: toda la nube está detrás de repositorios + `MainViewModel`.

### 5.6 Clasificación KMP

| Parte | KMP |
|---|---|
| Lógica de flujos (vías A/B, validaciones, mensajes, reintento colisión) | **A/B** |
| SDK oficial Firebase (Auth+Firestore Android, GMS Task) | **D** — alternativa: GitLive `firebase-kotlin-sdk` o capa expect/actual |
| Rules + tests emulador | **A** (server-side, independientes de plataforma) |
| Configuración google-services plugin | **E** — iOS requiere GoogleService-Info.plist/config manual |

---

## 6. HILT / INYECCIÓN DE DEPENDENCIAS

- Entrada: `GestorProApplication @HiltAndroidApp`; `MainActivity @AndroidEntryPoint`.
- Un único módulo `di/AppModule.kt` (`@Module @InstallIn(SingletonComponent::class)`), todo con `@Provides`:
  - `provideFirebaseAuth()` / `provideFirebaseFirestore()` (@Singleton)
  - `provideDatabase(@ApplicationContext)` (@Singleton, builder + callback semillas)
  - 7 × provideXxxDao, 7 × provideXxxRepository
  - `providePreferencesRepository(@ApplicationContext)` (@Singleton)
- Constructor injection directo: 4 repositorios Firebase (`@Singleton @Inject constructor`) y `PreferencesRepository`... (esta última también con provider).
- **@Binds: NO PRESENTE. Interfaces de repositorio: NO PRESENTES.**
- ViewModels: `@HiltViewModel` (9) + `hiltViewModel()` en composables.
- Sin subcomponentes custom, entry points ni multibindings. Qualifier usado: solo `@ApplicationContext`.

**Cambios necesarios para KMP:** Hilt no procesa `commonMain`. Opciones: migrar a **Koin** o **kotlin-inject**, o mantener Hilt solo en el source set Android y usar fábricas/inyección manual en commonMain. Recomendable primero extraer interfaces de repositorio. Room KMP no usa `Context` y DataStore multiplataforma no usa el delegado sobre Context: las factories cambian.

---

## 7. JETPACK COMPOSE

- ~24 archivos UI. Material 3 (BOM), `material-icons-extended`, edge-to-edge.
- **Tema**: claro/oscuro/sistema según DataStore (`MainViewModel.themeMode`) + **dynamic color Android 12+** con `LocalContext`/`Build.VERSION.SDK_INT` (Android-only).
- Estado: `collectAsStateWithLifecycle()` (57 usos; 9 residuos `collectAsState()`).
- Navegación: Navigation Compose (§13).
- Componentes reutilizables: `ui/components/` (MenuCard, ClienteItem, MovimientoItem, ResumenCard, ServicioItem).
- Animaciones avanzadas: NO PRESENTE (uso estándar Material3). NO DETERMINADO detalle fino.
- Textos: hardcodeados en español; solo 2 usos de recursos string (§14).
- Imágenes: Coil3 compose cargando rutas de archivo locales.
- Interacción sistema: Photo Picker (galería), SAF documents (export/import), ClipboardManager, Toast, share Intent.

**Portable a CMP casi directamente:** pantallas, layouts M3, components/, rutas Navigation Compose (existe artefacto CMP), lógica VM.
**Android-specific:** ActivityResult launchers, Toast, Clipboard, dynamic color, FotoUtils/Bitmap, deep link en Activity, File paths para fotos.

---

## 8. VIEWMODELS Y LÓGICA DE NEGOCIO

| ViewModel | Responsabilidad | Dependencias | Movible a commonMain | Depende de Android |
|---|---|---|---|---|
| `MainViewModel` | Auth (iniciar/registro/cerrar), destino inicial (DataStore+sión), negocio remoto, vinculación Vía A/B, tema/nombre/logo/idClienteSesion | PreferencesRepository, AutenticacionRepository, NegocioRepository, VinculacionRepository | Casi toda la lógica | Solo indirectamente (repositorios Firebase) |
| `ClienteViewModel` | CRUD clientes Room, validación DNI única, archivar/restaurar, morosos, réplica write-through + reintento | ClienteRepository, ClienteRemotoRepository | Toda la orquestación | Catch de `android.database.sqlite.SQLiteConstraintException` |
| `ClaseViewModel` | Clases, sesiones, reservas, agenda local | Clase/SesionClase/Reserva/Cliente Repository | Todo | No |
| `MovimientoViewModel` | CRUD cuotas/movimientos | MovimientoRepository | Todo | No |
| `GastoViewModel` | CRUD gastos | GastoRepository | Todo | No |
| `EconomiaViewModel` | Combina movimientos+gastos+nombres clientes | Movimiento/Gasto/Cliente Repository | Todo | No |
| `DatosViewModel` | Export/import/restaurar JSON | Context (@ApplicationContext), ClienteDao, MovimientoDao, GastoDao (**deuda: salta repositorio**) | Orquestación | Context + Uri (SAF) |
| `PreferenciasViewModel` | Modo de tema | PreferencesRepository | Todo | No |
| `EnlaceVinculacionViewModel` (en EnlaceVinculacionScreen.kt) | Generar/copiar/compartir/regenerar/revocar enlace; consultar estado | VinculacionRepository (+remotos) | La mayor parte | Portapapeles/compartir en el composable |

Patrón común: corrutinas en `viewModelScope`, `MutableStateFlow` privado + `asStateFlow()`, callbacks `onExito` hacia UI. Ningún otro ViewModel accede a DAOs directamente.

---

## 9. KTOR / RED

- **Ktor: NO PRESENTE.** Retrofit/OkHttp explícitos: NO PRESENTE.
- Red exclusivamente vía SDK Firebase. Gson solo para export/import JSON local (`ExportManager`), no red. Sin `kotlinx.serialization` ni `@Serializable`.
- Compatibilidad KMP: nada que migrar de HTTP propio; si se añade REST en el futuro, usar Ktor client + kotlinx.serialization desde commonMain.

---

## 10. COIL / IMÁGENES

- Dependencia: `io.coil-kt.coil3:coil-compose:3.3.0` (Coil 3 = núcleo multiplataforma).
- Uso: carga de logo del negocio (ruta en DataStore) y foto de cliente (ruta absoluta archivo interno) en Home, Login, HomeCliente, Clientes/ClienteItem, MiPerfil, PerfilClienteAdministrador, AñadirCliente.
- Persistencia: `ui/utils/FotoUtils.kt::guardaFotoEnInterna(context, uri)` — decodifica con `BitmapFactory` (muestreo progresivo), escala máx. 1024 px, comprime JPEG 85 y guarda en `filesDir/fotos/foto_<ts>.jpg`. Invocado tras Photo Picker en AñadirClienteScreen y MiNegocioScreen.
- **Android-specific:** todo FotoUtils (Bitmap/contentResolver/File/FileOutputStream/recycle). La *carga* con Coil3 es portable (existe coil-compose-multiplatform); la *captura/guardado* necesita implementación iOS (picker nativo + guardado sandbox) idealmente tras un contrato expect/actual que devuelva una ruta equivalente.

---

## 11. WORKMANAGER / TAREAS EN SEGUNDO PLANO

- **WorkManager: NO PRESENTE** (sin Workers, sin PeriodicWorkRequest, sin constraints, sin notificaciones, sin sincronización programada — verificado por búsqueda global).
- Concurrencia actual: corrutinas `viewModelScope` + dispatchers implícitos de Room/DataStore/Firebase. Reintento de sincronización manual (botón).
- **Implicación iOS:** hoy no hay funcionalidad que exija background tasks nativas. Futuras necesidades (sincronización automática, recordatorios de sesiones/pagos) exigirían expect/actual (BGTaskScheduler / UNUserNotificationCenter).

---

## 12. PERMISOS Y FUNCIONES DEL DISPOSITIVO

| Función | ¿Se usa? | Dónde / cómo | Permisos manifest |
|---|---|---|---|
| Galería / selección imágenes | SÍ | Photo Picker `PickVisualMedia` en AñadirClienteScreen y MiNegocioScreen | Ninguno necesario |
| Cámara | NO | Solo mención en comentario de FotoUtils | — |
| Archivos import/export | SÍ | SAF `CreateDocument("application/json")` / `OpenDocument` en DatosScreen + ContentResolver en ExportManager | Ninguno |
| Portapapeles | SÍ | ClipboardManager en PerfilClienteAdministradorScreen (2 sitios) y EnlaceVinculacionScreen | Ninguno |
| Compartir enlace | SÍ | Share intent (+ drawable ic_whatsapp.xml) | Ninguno |
| Notificaciones push | NO | — | — |
| Ubicación / Bluetooth / Contactos / Sensores / Biometría | NO | — | — |

El Manifest **no declara ningún `<uses-permission>`** → superficie mínima de adaptación de permisos para iOS.

---

## 13. NAVEGACIÓN

- **Motor:** Navigation Compose 2.9.3 (`NavHost`, `rememberNavController`, `composable(route)`).
- **Rutas:** `object Routes` con constantes string + funciones generadoras (`perfilCliente(id)`, `modificarCliente(id)`, `detalleClase(id)`, `detalleSesionReservas(id)`, `enlaceVinculacion(id)`, `modificarMiPerfil(id)`). ~24 destinos.
- **Destino inicial dinámico:** AppNavigation resuelve `destinoInicialSegunSesion()` (DataStore + sesión Firebase restaurada) antes de montar el NavHost (splash mínimo). 
- **Parámetros:** path `{idCliente}/{idClase}/{idSesion}` (parse `toIntOrNull`); query opcional nullable `vincular_cliente?codigo={codigo}`.
- **Deep links:** custom scheme `gestorpro://vincular/{token}` (Manifest, activity `singleTask`, intent-filter VIEW/BROWSABLE). Captura manual en `MainActivity.onCreate/onNewIntent` → singleton observable `EnlacePendiente.codigo` → `LaunchedEffect` navega si hay sesión CLIENTE; si no, se consume tras login/registro vía `MainViewModel.destinoSegunTipo()`. Preparado para App Links HTTPS.
- **Back stack:** `popUpTo(LOGIN){inclusive=true}` tras autenticación; `popUpTo(0)` al cerrar sesión.

**Adaptación CMP:** Navigation Compose tiene artefacto multiplataforma; las rutas string son portables. Requiere: recepción de deep link por plataforma (expect/actual que alimente `EnlacePendiente`), revisar back handling/popUpTo y el arranque condicional.

---

## 14. RECURSOS

| Recurso | Contenido | Observación KMP |
|---|---|---|
| `values/strings.xml` | Solo `app_name` | Textos UI hardcodeados en español en composables/ViewModels (solo 2 usos de `stringResource`). Externalizar textos será necesario para i18n/CMP resources |
| `values/colors.xml`, `themes.xml` | Mínimos (tema base NoActionBar) | El tema real es Compose; portar a CMP theme Kotlin |
| `mipmap-*/ic_launcher(.round)` | Iconos adaptativos (webp + xml anydpi) | Específicos Android; iOS necesita asset catalog propio |
| `drawable/ic_launcher_foreground/background`, `ic_whatsapp.xml` | Vectores | Portables a recursos CMP o redefinidos |
| `xml/backup_rules.xml`, `data_extraction_rules.xml` | Backup Android | Android-only (allowBackup=false hoy) |
| Fuentes custom / traducciones / values-en | NO PRESENTE | — |

---

## 15. GRADLE Y DEPENDENCIAS

Configuración: un solo módulo; `buildTypes.release` sin minify, proguard estándar; sin productFlavors; sin BuildConfig custom; repos google()+mavenCentral() con FAIL_ON_PROJECT_REPOS.

| Dependencia | Versión | Uso | Módulo | ¿Android-only? | Alternativa KMP | Dificultad migración |
|---|---|---|---|---|---|---|
| androidx.core:core-ktx | 1.18.0 | Utilidades base | :app | Sí | N/A (usar APIs comunes) | BAJA |
| androidx.lifecycle:lifecycle-runtime-ktx / runtime-compose | 2.10.0 | Scope, collectAsStateWithLifecycle | :app | lifecycle tiene KMP parcial (runtime-viewmodel multiplataforma) | androidx.lifecycle multiplataforma | MEDIA |
| androidx.activity:activity-compose | 1.13.0 | ComponentActivity + setContent, ActivityResult | :app | Sí | CMP Application + expect/actual | ALTA (punto de entrada) |
| androidx.compose BOM (ui/graphics/material3/icons/tooling) | 2026.02.01 | UI completa | :app | No (CMP) | Compose Multiplatform | BAJA/MEDIA |
| androidx.navigation:navigation-compose | 2.9.3 | Navegación | :app | No (hay artefacto CMP) | Navigation multiplataforma | BAJA/MEDIA |
| androidx.room:runtime/ktx/compiler | 2.8.4 (KSP) | Persistencia local | :app | No desde 2.7 (KMP oficial) | Room KMP (SQLite nativo) | MEDIA |
| hilt-android + compiler (+hilt-navigation-compose) | 2.60.1 / 1.2.0 | DI | :app | Sí (no commonMain) | Koin / kotlin-inject | ALTA |
| io.coil-kt.coil3:coil-compose | 3.3.0 | Imágenes | :app | No (Coil3 KMP) | coil-compose-multiplatform | BAJA |
| androidx.datastore:datastore-preferences | 1.1.7 | Preferencias | :app | Delegado Context es Android; core es KMP | datastore-preferences-core KMP | MEDIA |
| gson | 2.11.0 | Export/import JSON | :app | JVM-only (reflection) | kotlinx.serialization | BAJA/MEDIA |
| firebase-bom / firebase-auth / firebase-firestore | 34.16.0 | Auth + Firestore | :app | Sí | GitLive firebase-kotlin-sdk o expect/actual | **ALTA** |
| com.google.gms.google-services (plugin) | 4.5.0 | Config Firebase | raíz+:app | Sí | Config iOS manual | MEDIA |
| junit / androidx.test.ext:junit / espresso / compose-ui-test-junit4 | — | Tests plantilla | test/androidTest | Sí | kotlin.test / tests common | BAJA |
| org.gradle.toolchains.foojay-resolver-convention (plugin settings) | 1.0.0 | Toolchain JDK | settings | Build tool | Igual en KMP | — |
| AGP 9.1.1 / Gradle 9.3.1 / Kotlin 2.2.10 / KSP 2.3.6 | — | Build | — | AGP aplica a androidTarget KMP igualmente | Reorganizar plugins a KMP | MEDIA/ALTA (AGP 9 muy nuevo) |

**NO PRESENTES** de la lista inicial del usuario: Ktor, WorkManager.

---

## 16. CÓDIGO ANDROID-SPECIFIC (lista completa verificada)

| API/patrón | Archivos |
|---|---|
| `android.app.Application` (@HiltAndroidApp) | GestorProApplication.kt |
| `ComponentActivity`, `Intent`, `Uri`, `Bundle`, `enableEdgeToEdge`, deep link onNewIntent | MainActivity.kt |
| `Context` inyectado | AppModule.kt, PreferencesRepository.kt, DatosViewModel.kt (@ApplicationContext), ExportManager.kt |
| DataStore delegado sobre Context | PreferencesRepository.kt |
| ContentResolver + Uri (SAF export/import JSON) | ExportManager.kt, DatosScreen.kt, DatosViewModel.kt |
| Bitmap / BitmapFactory / File(filesDir) / FileOutputStream | FotoUtils.kt; consumo java.io.File en AñadirClienteScreen, MiNegocioScreen, MiPerfilScreen, HomeScreen, HomeClienteScreen, LoginScreen, ClienteItem, PerfilClienteAdministradorScreen (carga de fotos desde ruta) |
| Photo Picker (ActivityResultContracts.PickVisualMedia + rememberLauncherForActivityResult) | AñadirClienteScreen.kt, MiNegocioScreen.kt |
| SAF CreateDocument/OpenDocument launchers | DatosScreen.kt |
| ClipboardManager + Toast + share Intent | PerfilClienteAdministradorScreen.kt, EnlaceVinculacionScreen.kt |
| `SQLiteConstraintException` | ClienteViewModel.kt |
| Dynamic color + LocalContext + Build.VERSION | Theme.kt |
| Hilt Android (HiltAndroidApp/AndroidEntryPoint/HiltViewModel/@ApplicationContext) | toda la app |
| Firebase SDK Android + GMS Task | data/firebase/* |
| Room builder con Context | AppModule.kt |
| `java.time.*` (LocalDate, DayOfWeek, Instant, ZoneId, DateTimeFormatter, TemporalAdjusters), `java.text.NumberFormat`, `java.util.Locale`, `java.util.concurrent.TimeUnit`, `java.security.SecureRandom`, `java.util.Date` | pantallas clases/clientes/economía/configuración y VinculacionRepository — **no disponibles en Kotlin/Native**: requieren kotlinx-datetime/formatos propios/expect-actual |

Fragment, BroadcastReceiver, Service, SharedPreferences, AndroidView, Play Services (salvo Task): **NO PRESENTES**.

---

## 17. MODELOS DE DATOS

**Capas modelo y relaciones:**

1. **Entidades Room** (`data/entity/*Entity`) — persistencia local. Anotadas @Entity.
2. **Modelo UI/compartido** (`model/`):
   - `Cliente` (UI: nombre completo concatenado, sin negocioId/servicios/firebaseUid)
   - Enums: `EstadoCliente` (ACTIVO, MOROSO*, BAJA, ARCHIVADO, REGISTRADO — *MOROSO calculado, nunca almacenado), `EstadoMovimiento` (PENDIENTE/PAGADO), `EstadoSolicitud` (PENDIENTE/ACEPTADA/RECHAZADA), `TipoSolicitud` (**CLASE/BAJA — desajuste pendiente con contrato remoto ALTA/BAJA**), `TipoUsuario` (ADMINISTRADOR/CLIENTE), `FiltroClientes` (TODOS/ACTIVO/MOROSO/...)
   - Proyecciones JOIN: `SesionConClase`, `ReservaConCliente`
3. **Documentos Firestore** — construidos como `Map<String,Any?>` manuales dentro de los repositorios remotos (`mapaDeAlta/mapaDeEdicion/timestampDe`); no hay DTOs tipados.
4. **Mapeadores**: extensión `ClienteEntity.toCliente()`; conversión millis⇄Timestamp; enums ⇄ String vía converters.
5. **Resultados de operaciones**: `ResultadoAutenticacion(exito,mensaje,rol)`, `EnlaceVinculacion`, `ConsultaEnlace` (data classes en data/firebase).

Los nombres de estado remotos replican exactamente los del enum Room (ACTIVO/BAJA/ARCHIVADO/REGISTRADO).

---

## 18. TESTS

| Tipo | Estado |
|---|---|
| Unit tests | Solo `ExampleUnitTest.kt` (plantilla JUnit4) |
| Instrumentation/UI tests | Solo `ExampleInstrumentedTest.kt` (plantilla) |
| Tests Room / ViewModel / Repositorios | NO PRESENTES |
| Tests Security Rules | Suite externa Node.js `firestore-tests/` (17 pruebas contra emulador; `firestore-debug.log` versionado — deuda de repo) |

Para KMP: la infraestructura de tests prácticamente empieza de cero, lo cual facilita crear tests en commonMain (kotlin.test). Los DAOs podrían probarse en target JVM/iOS con Room testing; la lógica de ViewModels con corrutinas-test.

---

## 19. CONFIGURACIÓN Y SECRETOS

- **`app/google-services.json`: EXISTE** en disco pero **NO está versionado** (ignorado por `.gitignore`: `**/google-services.json`). Contiene identificadores del proyecto Firebase `gestorpro-50e83`. *No se muestra contenido.*
- `firebase.json` + `.firebaserc` (raíz, versionados): configuración de despliegue de Rules al proyecto Firebase (identificador de proyecto visible; no es secreto).
- Sin claves/API keys/tokens en código fuente (verificado en los archivos clave; lo prohíbe además AGENTS.md).
- Sin variables de entorno ni keystore config custom. `local.properties` gitignored.
- BuildConfig fields: NO PRESENTE. productFlavors: NO PRESENTE. buildTypes: debug/release (release sin minify, proguard estándar).
- Deuda de repo: `firestore-tests/firestore-debug.log` versionado (recomendado ignorarlo).

---

## 20. ESTADO DE GIT

| Campo | Valor |
|---|---|
| Rama actual | `master` |
| Último commit | `37381dc` "firebase" |
| Commits recientes | 086be55 "Autenticacion Firebase real: AutenticacionRepository, RegistroScreen y config deploy" · e3f49b9 "pantalla cleites" · 63e88d7 "Refactor firestore.rules: elimina limite de 1000 expresiones, 9/9 pruebas OK" |
| Working tree | **Limpio** (sin modificaciones ni archivos sin seguimiento) |
| Remoto | NO DETERMINADO en esta auditoría (no se consultó `git remote`; verificar antes de planificar ramas de migración) |

Nota: el historial de sesiones mencionaba trabajo sin commitear; al día de la auditoría todo está comprometido en `master`.

---

## 21. MAPA DE DEPENDENCIAS

```text
┌─────────────────────────────────────────────────────────────────────┐
│ UI (Compose M3) — 24 pantallas/composables                          │
│  Android-only: ActivityResult launchers, Toast, Clipboard,          │
│  deep link (Activity), dynamic color (LocalContext), Coil File()    │
└──────────────┬──────────────────────────────────────────────────────┘
               ↓
┌─────────────────────────────────────────────────────────────────────┐
│ ViewModels (@HiltViewModel) — 9                                     │
│  Android-only: DatosViewModel(Context/Uri), ClienteViewModel        │
│  (SQLiteConstraintException)                                        │
└──────────────┬──────────────────────────────────────────────────────┘
               ↓            (NO existe capa UseCase)
┌─────────────────────────────────────────────────────────────────────┐
│ Repositories                                                        │
│  Locales (7): wrappers DAO — Kotlin puro          [compartible]     │
│  PreferencesRepository: DataStore+Context         [adaptar]         │
│  Remotos (4): Firebase SDK + GMS Task             [Android-only]    │
│  ExportManager: Context/Uri/Gson                  [Android-only]    │
└──────┬───────────────────┬──────────────────────┬───────────────────┘
       ↓                   ↓                      ↓
┌─────────────┐   ┌──────────────────┐   ┌───────────────────────────┐
│ Room        │   │ DataStore        │   │ Firestore / Auth          │
│ v10, Flow   │   │ preferencias     │   │ Rules server-side OK      │
│ [KMP-ready] │   │ [core KMP-ready] │   │ [SDK → GitLive/expect]    │
└─────────────┘   └──────────────────┘   └───────────────────────────┘

DI: Hilt (AppModule) ──► sustituir por Koin/kotlin-inject para commonMain
```

Puntos de fuga Android identificados en el grafo: punto de entrada (Activity), DI, SDK Firebase, ExportManager/FotoUtils, launchers del sistema, Theme dynamic color y APIs java.* no nativas.

---

## 22. ANÁLISIS DE MIGRACIÓN A KMP (clasificación sin migrar)

**A. Compartible directamente**
- `model/*` (data classes y enums), proyecciones SesionConClase/ReservaConCliente.
- Converters Room (Kotlin puro).
- Repositorios locales como *orquestación* (su firma Flow/suspend es común; el interior depende de Room KMP).
- Lógica pura de ViewModels (validaciones, destinos, estado StateFlow).
- Security Rules y su suite de emulador.
- Resultados/tipos de operación (ResultadoAutenticacion, EnlaceVinculacion, ConsultaEnlace).

**B. Compartible con pequeños cambios**
- Entidades Room (defaults `System.currentTimeMillis()`).
- DAOs (verificar SQL soportado en target iOS; UPDATE con retorno).
- Pantallas Compose en general (imports CMP, recursos).
- Navigation Compose rutas/NavHost (artefacto CMP).
- Coil3 (artefacto multiplataforma).
- DataStore (paso a datastore-preferences-core con factory propia).

**C. Necesita adaptación**
- ViewModels: base `ViewModel` multiplataforma (lifecycle-viewmodel KMP) o equivalente; quitar SQLiteConstraintException → mapeo genérico.
- Theme (dynamic color → expect/actual o esquema fijo).
- Deep link: pipeline expect/actual hacia EnlacePendiente.
- Gson → kotlinx.serialization en export/import.
- APIs java.time/java.text/java.util → kotlinx-datetime + format propios.
- SecureRandom → expect/actual.

**D. Debe ser específico de Android (se conserva)**
- MainActivity/GestorProApplication, Manifest, launchers Photo Picker y SAF, Toast/Clipboard/share actuales (hasta abstraerlos), google-services plugin, Hilt si se mantiene solo en Android.

**E. Requiere implementación específica iOS**
- Punto de entrada app (MainViewController/KMP app lifecycle).
- Recepción de deep link custom scheme (Info.plist + UIApplicationDelegate adapter) y futuro App Links (Universal Links + AASA).
- Captura/guardado de fotos (picker nativo + sandbox) detrás de expect/actual.
- Configuración Firebase iOS (GoogleService-Info.plist o init manual con GitLive SDK).
- Almacenamiento clave-valor nativo si no se usa DataStore core.
- (Futuro) notificaciones/background tasks si se añaden.

---

## 23. RIESGOS

| Área | Riesgo | Severidad |
|---|---|---|
| **Firebase Auth/Firestore** | El SDK oficial no es KMP. Opción GitLive firebase-kotlin-sdk: cobertura Auth+Firestore buena pero API distinta (no Task/Batch idénticos); las escrituras atómicas Batch/Transaction deben revalidarse contra las Rules (17 tests). Riesgo de divergencia semántica (timestamps, errores). | **ALTA** |
| **Hilt** | No soporta commonMain → migración a Koin/kotlin-inject toca todos los constructores; sin interfaces de repositorio hoy, el cambio es más invasivo. | **ALTA** |
| **Room** | Room KMP oficial pero: builder sin Context, callback semillas execSQL, fallback destructivo, y verificar comportamiento de Flow/UPDATE-retorno en iOS target; esquema v10 compartido exige plan de versionado conjunto Android/iOS. | MEDIA/ALTA |
| **AGP 9 + Gradle 9.3 + Kotlin built-in** | Stack muy reciente; la conversión a KMP puede chocar con incompatibilidades de plugins (ksp "2.3.6", compose plugin) y toolchains. | MEDIA/ALTA |
| **DataStore** | Delegado sobre Context no portable; usar datastore-preferences-core cambia la inicialización (riesgo bajo-medio). | MEDIA |
| **Compose Multiplatform** | BOM Android ≠ versión CMP; iconos-extended y material3 disponibles en CMP, pero diferencias menores (ventanas, fuentes, gestuales) y rendimiento iOS aún en maduración. | MEDIA |
| **Navigation Compose** | Artefacto CMP joven; deep links y type-safe args pueden requerir ajustes. | MEDIA |
| **WorkManager** | NO PRESENTE hoy ⇒ sin riesgo actual; riesgo solo si se añade sincronización automática (iOS BGTask). | NULO hoy |
| **Permisos** | Solo Photo Picker/SAF/portapapeles ⇒ superficie mínima; iOS requiere adaptación de picker/documentos. | BAJA |
| **Almacenamiento imágenes** | Rutas absolutas de filesDir guardadas en BD/DataStore no trasladables a iOS tal cual (sandbox distinto) → mejor guardar nombres relativos + resolver por plataforma. | MEDIA |
| **APIs java.\*** | java.time/NumberFormat/Locale/SecureRandom/File no existen en Native → refactor transversal en pantallas de clases/economía y repositorios. | MEDIA |
| **Datos existentes** | fallbackToDestructiveMigration + datos de prueba en onCreate: peligroso replicar en producción iOS; conviene sanear antes de migrar. | MEDIA |
| **Reglas/contrato remoto** | Cualquier nueva implementación cliente debe respetar exactamente las operaciones atómicas validadas por Rules (batch estrictos); un desajuste rompe permisos en runtime. | MEDIA |

---

## 24. RECOMENDACIÓN FINAL (solo conclusión técnica)

- **Porcentaje potencialmente compartible:** ≈ **60–70 %** del código (modelos, repositorios-orquestación, lógica de ViewModels, pantallas Compose, navegación, Rules). El 30–40 % restante son puntos de entrada/DI/Firebase SDK/utilidades de sistema/recursos que requieren adaptación o duplicación por plataforma.
- **Partes más difíciles:** 1) Firebase Auth/Firestore (elección GitLive vs expect/actual + revalidación atómica contra Rules), 2) sustitución de Hilt en commonMain, 3) bootstrap Gradle KMP sobre AGP 9 tan nuevo, 4) fotos/archivos (FotoUtils, SAF) y APIs java.*.
- **Orden sugerido de migración (para quien diseñe la estrategia):**
  1. Preparación previa barata en Android puro: extraer interfaces de repositorio, externalizar strings, aislar SQLiteConstraintException/reloj/SecureRandom detrás de abstracciones, saneiar migraciones/datos de prueba.
  2. Crear módulo shared KMP con `model` + converters + lógica pura de ViewModels (tests comunes).
  3. Room KMP (entidades/DAOs/database factory) y DataStore core.
  4. Capa Firebase (decidir SDK KMP) reutilizando las Rules y su suite como red de seguridad.
  5. Compose Multiplatform para UI + navegación, empezando por flujos auth/vinculación (menos dependencia de sistema) y terminando por clientes/clases (launchers, fotos).
  6. Al final: deep links por plataforma, export/import SAF↔document picker iOS, tema dinámico, iconos/recursos.
- **Información adicional necesaria antes de empezar:** disponibilidad de macOS/Xcode para compilar target iOS; decisión de versión CMP compatible con Kotlin 2.2.10 y AGP 9; evaluación puntual del estado actual del SDK GitLive (Auth email/password + Batch/Transaction); estrategia de distribución de esquema Room entre plataformas; requisitos futuros (notificaciones, sincronización automática, App Links HTTPS) que condicionen expect/actual.

---

## 25. INVENTARIO DE ARCHIVOS IMPRESCINDIBLES PARA LA FUTURA MIGRACIÓN

| Archivo | Por qué importa |
|---|---|
| `gradle/libs.versions.toml`, `app/build.gradle.kts`, `settings.gradle.kts`, `build.gradle.kts` | Reescritura completa del sistema de build hacia KMP (plugins, catálogo, AGP 9) |
| `di/AppModule.kt` | Centro de DI a sustituir (Hilt→Koin/kotlin-inject); contiene builder Room y providers Firebase |
| `GestorProApplication.kt`, `MainActivity.kt`, `AndroidManifest.xml` | Puntos de entrada Android; referencia para diseño expect/actual (deep link, edge-to-edge) |
| `navigation/Routes.kt`, `AppNavigation.kt`, `EnlacePendiente.kt` | Contrato de navegación y deep link a portar a Navigation CMP |
| `data/entity/*.kt` (7), `data/converter/*.kt` (5), `data/database/ClientesDatabase.kt` | Esquema Room a commonMain (v10, converters, semillas a extraer) |
| `data/dao/*.kt` (7) | SQL a validar en targets iOS |
| `data/repository/*.kt` (7 + PreferencesRepository) | Orquestación local; PreferencesRepository define el contrato de preferencias a portar a datastore-core |
| `data/firebase/AutenticacionRepository.kt` (+`esperar()`), `NegocioRepository.kt`, `VinculacionRepository.kt`, `ClienteRemotoRepository.kt` | Toda la semántica remota (batches/transacciones/mensajes) que debe reimplementarse fielmente sobre SDK KMP |
| `firestore.rules` + `firestore-tests/firestore.rules.test.cjs` | Contrato de seguridad y suite de regresión obligatoria tras cambiar cliente Firebase |
| `ui/viewmodel/MainViewModel.kt`, `ClienteViewModel.kt` | Lógica de sesión/vinculación y réplica write-through con reintento (núcleo de negocio híbrido) |
| Resto de ViewModels (`Clase/Movimiento/Gasto/Economia/Datos/Preferencias`, EnlaceVinculacionViewModel) | Candidatos directos a commonMain tras limpiar Context/SQLite |
| `ui/theme/Theme.kt`, `Color.kt`, `Type.kt` | Tema a portar (dynamic color → expect/actual) |
| `ui/components/*.kt` (5) | Componentes compartidos base de todas las pantallas |
| `ui/auth/*` (4 pantallas), `ui/clientes/VincularClienteScreen.kt`, `ui/configuracion/CrearNegocioScreen.kt`, `MiNegocioScreen.kt`, `ui/clientes/EnlaceVinculacionScreen.kt` | Flujos auth/vinculación/negocio (primera ola CMP) y usos de Clipboard/Share |
| `ui/clientes/AñadirClienteScreen.kt`, `ui/utils/FotoUtils.kt` | Foto Picker + guardado interno: patrón a abstraer expect/actual |
| `ui/configuracion/DatosScreen.kt` + `data/export/ExportManager.kt` | Export/import SAF+Gson a reimplementar (documents picker + kotlinx.serialization) |
| `ui/clases/*.kt`, `ui/economia/EconomiaScreen.kt` | Uso intensivo de java.time/NumberFormat → refactor datetime |
| `app/google-services.json` (local, no versionado) | Config Firebase Android; referencia para crear el equivalente iOS |
| `AGENTS.md`, `CONVERSACION_EXPORTADA.md` | Contrato funcional/remoto vigente e historial de decisiones |

---

*Fin del informe. Generado por auditoría estática del código (sin ejecutar builds ni modificar archivos).*
