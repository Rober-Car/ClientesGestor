# AGENTS.md - Contexto del proyecto GestorPro

Lee este archivo completo antes de modificar el proyecto.

## Proyecto

- **Nombre:** GestorPro
- **Descripción:** aplicación Android para gestionar clientes, clases, reservas, cuotas, gastos y datos económicos de un negocio deportivo.
- **Plataforma:** Android.
- **Package:** `com.roberto.gestorpro`.
- **Application:** `GestorProApplication`.
- **Activity principal:** `MainActivity`.
- **Módulo:** `app`.
- **Min SDK:** 26.
- **Target SDK:** 36.
- **Compile SDK:** 36.1.

## Stack tecnológico

Las versiones oficiales del proyecto están en `gradle/libs.versions.toml`.

| Área | Tecnología | Versión configurada |
|---|---|---|
| Lenguaje | Kotlin | 2.2.10 |
| Android Gradle Plugin | AGP | 9.1.1 |
| UI | Jetpack Compose + Material 3 | BOM 2026.02.01 |
| Navegación | Navigation Compose | 2.9.3 |
| Inyección de dependencias | Hilt | 2.60.1 |
| Base de datos local | Room | 2.8.4 |
| Preferencias | DataStore Preferences | 1.1.7 |
| Imágenes | Coil Compose | 3.3.0 |
| Serialización auxiliar | Gson | 2.11.0 |
| Backend | Firebase Authentication + Firestore | Firebase BOM 34.16.0 |

Reglas relacionadas con dependencias:

- No añadir una dependencia sin avisar y explicar su necesidad.
- Comprobar primero si la funcionalidad ya está cubierta por una dependencia existente.
- Usar el catálogo `libs.versions.toml` para las nuevas dependencias cuando sea posible.
- Avisar antes de modificar `build.gradle.kts` o `gradle/libs.versions.toml`.
- No cambiar versiones por iniciativa propia.

## Arquitectura actual

GestorPro utiliza actualmente **MVVM con repositorios**, no una Clean Architecture estricta.
La estructura vigente es:

```text
UI Compose -> ViewModel -> Repository -> Room / DataStore / Firebase
```

No se debe exigir ni introducir una capa `domain/usecase` como requisito automático. Si una tarea necesita mejorar la arquitectura, debe proponerse primero y hacerse de forma incremental.

Reglas de arquitectura vigentes:

- La UI renderiza el estado y emite acciones del usuario.
- Los ViewModels coordinan el estado de pantalla y lanzan operaciones con `viewModelScope`.
- Los ViewModels acceden a datos mediante repositorios, no directamente mediante DAOs.
- Los DAOs y entidades Room pertenecen a `data`.
- Las clases de `model` contienen modelos compartidos de la aplicación.
- `AppModule` centraliza la configuración de Hilt, Room y repositorios.
- `ClientesDatabase` es la base de datos Room principal.
- `PreferencesRepository` encapsula DataStore.
- `VinculacionRepository` encapsula la vinculación con Firebase Authentication y Firestore.
- `MainActivity` es la única Activity de la aplicación.
- `AppNavigation` y `Routes` centralizan la navegación.

El código existente contiene deuda arquitectónica y no se debe refactorizar de forma masiva como parte de otra tarea. Si se detecta una inconsistencia, se informa y se propone por separado.

## Estructura principal

```text
app/src/main/java/com/roberto/gestorpro/
├── GestorProApplication.kt       -> Application de Hilt
├── MainActivity.kt               -> única Activity y punto de entrada Compose
├── navigation/
│   ├── AppNavigation.kt          -> NavHost y destinos
│   └── Routes.kt                 -> rutas de navegación
├── model/                        -> modelos y enums de la aplicación
├── data/
│   ├── converter/                -> conversores de Room
│   ├── dao/                      -> DAOs Room
│   ├── database/                 -> ClientesDatabase
│   ├── entity/                   -> entidades Room
│   ├── export/                   -> exportación de datos
│   ├── firebase/                 -> Firebase Authentication/Firestore
│   └── repository/               -> repositorios de datos
├── di/
│   └── AppModule.kt              -> dependencias de Hilt
└── ui/
    ├── auth/                     -> login y selección de tipo de usuario
    ├── clases/                   -> clases, sesiones y reservas
    ├── clientes/                 -> clientes y perfiles
    ├── components/               -> componentes Compose reutilizables
    ├── configuracion/            -> cuenta, negocio, preferencias y datos
    ├── economia/                 -> movimientos y gastos
    ├── home/                     -> inicio de administrador y cliente
    ├── theme/                    -> tema, colores y tipografía
    └── viewmodel/                -> ViewModels de las pantallas
```

## Funcionalidades principales

- Selección del perfil de uso: administrador o cliente.
- Inicio de administrador y de cliente.
- Alta, edición, consulta, archivado y restauración de clientes.
- Gestión de clases, sesiones y reservas.
- Gestión de movimientos, cuotas y gastos.
- Configuración del negocio, logo, tema, cuenta y datos.
- Selección de foto de perfil desde galería o cámara (perfil propio, perfil de cliente y formulario de cliente).
- Inicio y cierre de sesión con Firebase Authentication.
- Recuperación de contraseña con el correo de Firebase (solo `FirebaseAuth.sendPasswordResetEmail`).
- Persistencia local con Room y DataStore.
- Vinculación de clientes con Firebase Authentication y Firestore.

## Contrato remoto de Firestore

Firestore es la capa remota de GestorPro. Room sigue siendo la base de datos local de Android; no se debe asumir sincronización automática ni que ambas representen exactamente el mismo modelo.

Colecciones remotas actuales:

```text
usuarios/{uid}
negocios/{negocioId}
negocios_publicos/{negocioId}
clientes/{idCliente}
clases/{claseId}
sesiones/{sesionId}
reservas/{reservaId}
solicitudes/{solicitudId}
movimientos/{movimientoId}
vinculaciones/{codigo}
```

Reglas de identidad y pertenencia:

- El UID de Firebase es el ID del documento `usuarios/{uid}`.
- Los roles remotos son exactamente `ADMIN` y `CLIENTE`.
- `TipoUsuario.ADMINISTRADOR` se mapea al rol remoto `ADMIN`.
- `negocioId` es un `String` en Firestore.
- `clienteId`, `idCliente` y `sesionId` se manejan como enteros (`int64`) cuando forman parte de los datos.
- Los documentos de `clientes` pueden usar el identificador numérico convertido a texto en la ruta, por ejemplo `clientes/2`.
- `firebaseUid` es un `String`.
- `serviciosContratados` y `clientesPermitidos` son arrays de strings.
- Un administrador solo puede acceder a su negocio, identificado por `adminUid` y `negocioId`.
- Un cliente solo puede acceder a sus datos, sus reservas, sus solicitudes y las sesiones para las que esté autorizado.
- Los clientes nunca pueden acceder a `movimientos`.

Flujos funcionales remotos:

- Un ADMIN nuevo puede registrarse con `negocioId = null` y debe crear su propio negocio con código maestro.
- La creación del negocio, `negocios_publicos/{id}` y la asignación de `usuarios/{uid}.negocioId` deben ejecutarse en el mismo Batch.
- Las solicitudes solo representan altas y bajas. Sus valores remotos son `ALTA` y `BAJA`.
- Las clases definen servicios y horarios; las sesiones son instancias concretas; las reservas relacionan un cliente con una sesión mediante `sesionId`.
- Un cliente no solicita una clase mediante `solicitudes`; primero debe tener contratado el servicio y después puede reservar una sesión autorizada.
- La vinculación se realiza por dos vías:
  - **Vía A (código maestro):** CLIENTE introduce el código maestro del negocio → app busca en `negocios_publicos` → Transaction genera `idCliente` como entero aleatorio positivo dentro del rango válido de `Int` (`Random.nextInt(1_000_000_000, Int.MAX_VALUE)`), comprueba que `clientes/{id}` no existe y reintenta ante colisión (máx. 5), crea la ficha con su UID y actualiza `usuarios/{uid}`. **No se crea ninguna `vinculaciones/{codigo}`** y el CLIENTE jamás tiene permisos de escritura sobre `negocios`. No existen contadores de clientes.
  - **Vía B (enlace individual):** ADMIN replica la ficha a Firestore (`firebaseUid: null`) y genera un token individual (SecureRandom, ≥20 caracteres alfanuméricos, sin idCliente) mediante Batch atómico sobre `clientes/{id}.codigoVinculacion` + `vinculaciones/{token}` (PENDIENTE, expira en 7 días) → CLIENTE abre el deep link `gestorpro://vincular/{token}`, se autentica y reclama la ficha → batch actualiza `clientes/{idCliente}` (firebaseUid), `vinculaciones/{token}` (PENDIENTE→USADA) y `usuarios/{uid}`. El token es de uso único, revocable y regenerable.
- El consumo de una vinculación debe actualizar atómicamente `usuarios/{uid}`, `clientes/{cliente}` y `vinculaciones/{codigo}`.
- El mismo `idCliente: Int` se comparte entre Room y Firestore. Los clientes creados por el ADMIN se replican a Firestore con write-through inmediato (sin cola offline); si la réplica falla, el dato local se conserva, se informa al ADMIN y queda preparada una operación de reintento manual. Las Rules prohíben `delete` en `clientes`: el borrado local se refleja como baja lógica remota.
- Los valores remotos de `clientes.estado` son exactamente los nombres del enum Room: `ACTIVO`, `BAJA`, `ARCHIVADO`, `REGISTRADO`. `MOROSO` se calcula desde movimientos y nunca se almacena.
- Un código de vinculación es de uso único, tiene `fechaExpiracion`, y puede ser revocado por el ADMIN.
- El código maestro es independiente de las vinculaciones individuales; cambiarlo no afecta a clientes ya vinculados.
- Las consultas de sesiones y reservas deben diseñarse para ser compatibles con las Security Rules; las Rules no funcionan como filtros posteriores.

Security Rules:

- El archivo versionado del proyecto es `firestore.rules`.
- Todo está bloqueado por defecto salvo las rutas declaradas expresamente.
- Las funciones de Rules usan el documento `usuarios/{uid}` para resolver rol, estado, `clienteId` y `negocioId`; por ahora no se usan Custom Claims.
- `getAfter()` solo debe utilizarse en operaciones atómicas que actualicen todos los documentos relacionados.
- **Vía B:** la vinculación se valida con `vinculacionValidaParaConsumo()` — ficha sin UID, código PENDIENTE no caducado, PENDIENTE→USADA, coherencia negocio/cliente/usuario post-Batch.
- **Vía A:** la creación directa se valida con `creacionDirectaValida()` — el Batch/Transaction incluye la ficha nueva ligada al UID autenticado y deja `usuarios/{uid}` coherente post-operación.
- **Gestión de tokens (ADMIN):** `asignacionTokenValida()` (asignar/regenerar) y `revocacionTokenValida()` + `!existsAfter` exigen atomicidad estricta entre `clientes/{id}.codigoVinculacion` y `vinculaciones/{token}`; solo sobre fichas con `firebaseUid == null` y tocando únicamente esa clave.
- La generación de enlaces está bloqueada hasta que la ficha exista en Firestore (`existeClienteRemoto`).
- Un CLIENTE solo puede vincularse una vez (`usuarios/{uid}` exige `clienteId == null` y `negocioId == null`).
- `negocios_publicos/{id}` permite `get/list` a cualquier autenticado; `create/update` solo el ADMIN del negocio.
- Una reserva de cliente debe apuntar a una sesión existente del mismo negocio y a una sesión cuyo `clientesPermitidos` contenga el UID autenticado.
- Las Rules deben probarse con los casos ADMIN, CLIENTE y vinculación antes de publicarse.

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
- No incluir claves secretas en el código cliente ni en archivos versionados.
- No eliminar código existente sin explicar el motivo.
- No corregir bugs no solicitados si no son necesarios para la tarea actual; informar de ellos como deuda o riesgo.
- No introducir Activities adicionales sin justificarlo explícitamente.
- Respetar `allowBackup`, las migraciones Room y los datos de prueba; cualquier cambio de producción debe avisarse.

El código actual tiene algunos usos de `!!`, `collectAsState()` y strings hardcodeados. Se consideran advertencias y deuda técnica: deben señalarse cuando afecten a una tarea, pero no se debe iniciar una migración general sin pedirlo.

## Comandos del proyecto

Ejecutar desde `C:\Users\Roberto\AndroidStudioProjects\GestorPro`:

```powershell
# Compilar debug
.\gradlew.bat assembleDebug

# Compilar release
.\gradlew.bat assembleRelease

# Tests unitarios
.\gradlew.bat test

# Tests instrumentados, requiere emulador o dispositivo
.\gradlew.bat connectedAndroidTest

# Lint
.\gradlew.bat lint

# Limpiar y recompilar debug
.\gradlew.bat clean assembleDebug
```

## Tests

Los tests se mantienen para la fase final del proyecto salvo que el desarrollador los solicite expresamente antes. No crear archivos de test automáticamente durante una funcionalidad normal.

## Convenciones específicas de Firebase y navegación

- **Recuperación de contraseña:** usar exclusivamente `FirebaseAuth.sendPasswordResetEmail`. El mensaje de éxito debe ser **genérico** ("Si el email existe, recibirás un enlace…") para no revelar qué cuentas existen; ante errores de autenticación (usuario inexistente, email inválido…) se responde con el mismo mensaje genérico. Solo se comunican fallos reales (p. ej. sin conexión). Validar email no vacío y formato antes de llamar a Firebase (`android.util.Patterns.EMAIL_ADDRESS`).
- **Rutas con parámetros de query:** construir siempre sustituyendo el placeholder, nunca concatenando. Ejemplo correcto: `Routes.VINCULAR_CLIENTE.replace("{codigo}", token)`. No usar `"${Routes.VINCULAR_CLIENTE}?codigo=$token"` porque `Routes.VINCULAR_CLIENTE` ya contiene `?codigo={codigo}` y produce una ruta malformada (doble query) que corrompe el argumento.
- **Fotos:** la lógica de guardado vive en `ui/utils/FotoUtils.kt` (`guardaFotoEnInterna`, `crearFotoTemporal`, `uriDeFotoTemporal`, `guardarFotoDeCamara`). No duplicar esa función en pantallas. La cámara usa `TakePicture()` con `FileProvider` (`${applicationId}.fileprovider`, `res/xml/file_paths.xml`); el guardado se hace solo en el callback del resultado, nunca justo después de `launch()`. El selector común es el componente `ui/components/BotonSelectorFoto.kt`.

## Estado actual y pendientes (2026-08-26)

Implementado y compilado (BUILD SUCCESSFUL):

- Selección de foto galería/cámara en `MiPerfilScreen`, `PerfilClienteAdministradorScreen` y `AñadirClienteScreen` (componente `BotonSelectorFoto`, `FotoUtils.kt` ampliado, FileProvider configurado).
- Recuperación de contraseña (`RecuperarPasswordScreen`, `AutenticacionRepository.enviarCorreoRecuperacion`, `MainViewModel.enviarCorreoRecuperacion`, ruta `RECUPERAR_PASSWORD`, enlace en Login).
- Fix del bug de ruta de Vía B: `MainViewModel.destinoSegunTipo()` y `AppNavigation` usan `Routes.VINCULAR_CLIENTE.replace("{codigo}", token)`. (2 archivos sin commitear.)

Pendiente para continuar:

1. **Creación de negocio con PERMISSION_DENIED sin resolver.** Con Rules actuales y datos confirmados (usuarios/{uid} correcto con `negocioId == null`, `negocios/{uid}` inexistente) el Batch de `NegocioRepository.crearNegocio()` es lógicamente permitido. Hipótesis principal: `esAdmin()` false porque la petición llega sin `request.auth` válido (token de sesión caducado/inválido). Verificar: cerrar sesión y volver a iniciar, diff de reglas desplegadas vs `firestore.rules`, `project_id` de la APK, y que no exista `negocios_publicos/{uid}` huérfano. No modificar `firestore.rules` ni el diseño `negocioId = uid`.
2. **Réplica Room → Firestore de clientes** falla mientras `usuarios/{uid}.negocioId` sea null; se desbloquea al crear el negocio.
3. **Pruebas en dispositivo pendientes:** Vía B tras el fix de ruta, recuperación de contraseña, cámara de fotos.
4. **Commit pendiente** de los 2 archivos del fix de ruta (y de esta sesión si procede).
5. Limpieza de basura versionada: `build_*.txt` en raíz y `firestore-tests/firestore-debug.log`.
