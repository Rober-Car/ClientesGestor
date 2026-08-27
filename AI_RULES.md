# AI_RULES.md - Reglas de comportamiento del agente

Estas reglas se aplican al trabajo de agentes de IA sobre GestorPro.

## 1. Leer antes de actuar

- Leer `AGENTS.md` completo antes de realizar cambios.
- Antes de crear o modificar un archivo, leer los archivos relacionados existentes.
- Buscar un equivalente existente y seguir su patrón antes de crear una clase, pantalla, ViewModel, repositorio o entidad.
- Revisar el estado de Git y no sobrescribir cambios locales que no haya realizado el agente.

## 2. Razonar y comunicar antes de editar

- Antes de modificar código, explicar brevemente qué se cambiará y por qué.
- Si existen dos soluciones razonables con efectos diferentes, presentarlas y pedir decisión antes de editar.
- No asumir requisitos funcionales que no estén documentados o confirmados.
- Si una instrucción contradice `AGENTS.md`, señalar la contradicción y pedir confirmación.

## 3. Cambios por tarea

- Se pueden modificar varios archivos relacionados cuando una funcionalidad lo requiera.
- Antes de hacerlo, indicar los archivos o grupos de archivos que se tocarán.
- Mantener los cambios lo más pequeños posible.
- No aprovechar una tarea para refactorizar partes no relacionadas.
- Después de completar el grupo de cambios, indicar cómo verificarlo.

## 4. Respeto al código existente

- No borrar código existente sin explicar el motivo.
- No revertir ni modificar cambios realizados por el usuario u otro agente.
- Si se encuentra un bug fuera del alcance, informarlo sin corregirlo automáticamente.
- Solo corregir un bug no solicitado si provoca un riesgo crítico de seguridad, pérdida de datos o impide compilar la modificación actual; en ese caso, avisar antes.
- Mantener el idioma, el estilo y la organización del archivo editado.

## 5. Arquitectura

- Respetar la arquitectura actual `UI -> ViewModel -> Repository -> Data source`.
- No acceder directamente a Room, DataStore o Firebase desde un composable.
- Preferir que los ViewModels accedan a datos mediante repositorios.
- No introducir una capa Domain/UseCase como requisito automático; cualquier migración arquitectónica se debe proponer por separado.
- No poner lógica de negocio nueva compleja dentro de un composable.
- No pasar ViewModels a componentes hijos si se pueden pasar datos y lambdas.

## 6. Dependencias y configuración

- No añadir dependencias sin explicar su nombre, versión, propósito y alternativas disponibles.
- Avisar antes de modificar `build.gradle.kts` o `gradle/libs.versions.toml`.
- No cambiar versiones de Gradle, Kotlin, AGP o librerías sin confirmación.
- Usar el catálogo de versiones cuando se añadan dependencias autorizadas.
- No incluir secretos, tokens, contraseñas o claves privadas en el código.

## 7. Firestore y Security Rules

- Tratar Room y Firestore como capas diferentes; no asumir que sus entidades tienen los mismos campos o tipos.
- No tratar Firestore como una base de datos SQL: las colecciones no tienen un esquema rígido y las Security Rules no son filtros posteriores.
- Toda consulta debe incluir los filtros necesarios para que Firestore pueda demostrar que todos los documentos cumplen las Rules.
- No añadir accesos a una colección sin revisar primero `firestore.rules`.
- No publicar o sustituir Security Rules sin revisar el cambio y ejecutar la matriz de pruebas acordada (`npm --prefix firestore-tests test`).
- Usar Batch o Transaction cuando una regla dependa de `getAfter()`.
- La creación de un negocio debe vincular en la misma operación el negocio, `negocios_publicos/{id}` y `usuarios/{uid}.negocioId`.
- La alta/vinculación de un cliente se realiza por dos vías, **sin `vinculaciones` ni deep links (Vía B descartada)**:
  - **VÍA 1 (ADMIN crea primero):** el ADMIN replica la ficha y en el mismo Batch crea `indices_clientes/{negocioId}_{dni}`. El CLIENTE, al introducir código maestro + DNI, escribe primero una **declaración temporal** en `perfiles_pendientes/{uid}` = `{ dni, negocioId }` (NO un perfil ficticio) y después localiza la ficha por el índice y la vincula con una Transaction que escribe `clientes/{idCliente}.firebaseUid` y actualiza `usuarios/{uid}`. Validar con `vinculacionDniValida()` y con la regla `clientes/get` VÍA 1 (permite al CLIENTE sin vínculo leer solo la ficha declarada). Se borra `perfiles_pendientes/{uid}` al terminar (éxito o rechazo). No se crea segunda ficha.
  - **VÍA 2 (CLIENTE crea primero):** el CLIENTE guarda su perfil completo en `perfiles_pendientes/{uid}` y, al introducir código maestro + DNI, la Transaction crea `clientes/{idCliente}` + `indices_clientes/{negocioId}_{dni}` + `usuarios/{uid}` y borra el perfil pendiente. Validar con `creacionDirectaValida()`. Si el índice ya existe, se vincula a la ficha existente (VÍA 1); si ya tiene UID, se rechaza.
- `perfiles_pendientes/{uid}` admite DOS modos: VÍA 1 (declaración `{ dni, negocioId }`) y VÍA 2 (perfil completo con nombre, apellidos, dni, telefono, email, foto, fechaNacimiento). Solo el propio uid puede gestionarlo; `list` prohibido. Se borra siempre al terminar la vinculación.
- La unicidad de ficha por negocio+DNI está garantizada por el documentId determinista de `indices_clientes/{negocioId}_{dni}`; la Transaction sobre el índice serializa la concurrencia. `update` del índice prohibido; `delete` solo ADMIN al cambiar el DNI (Batch atómico: borra el viejo y crea el nuevo).
- El DNI identifica la ficha dentro del negocio y el CLIENTE **nunca** puede modificarlo; solo el ADMIN puede cambiarlo manteniendo el índice atómico.
- `observaciones` vive en `clientes_privados/{idCliente}` (solo ADMIN); el CLIENTE no puede leerlo ni modificarlo.
- El mismo `idCliente: Int` se comparte entre Room y Firestore; la réplica es write-through sin cola offline: si falla no se revierte lo local, se informa y se ofrece reintento manual. No borrar clientes remotos: baja lógica.
- Los estados remotos de cliente son exactamente `ACTIVO`, `BAJA`, `ARCHIVADO`, `REGISTRADO` (nombres del enum Room); MOROSO nunca se almacena.
- Un CLIENTE solo puede vincularse una vez (`usuarios/{uid}` exige `clienteId == null` y `negocioId == null`).
- El código maestro del negocio es independiente de las vinculaciones individuales; cambiarlo no afecta a clientes ya vinculados.
- `negocios_publicos/{id}` permite `get/list` a cualquier autenticado; `create/update` solo el ADMIN del negocio.
- `indices_clientes` solo permite `get` al ADMIN de su negocio o al CLIENTE cuyo `perfiles_pendientes/{uid}` declara exactamente el `dni` Y el `negocioId` del índice; `list` prohibido (evita enumerar DNI). Para que la Transaction de VÍA 1 funcione, `clientes/get` debe permitir al CLIENTE sin vínculo leer la ficha declarada.
- `perfiles_pendientes/{uid}` solo lo gestiona su propio uid; `list` prohibido.
- Una reserva de cliente debe comprobar la sesión referenciada por `sesionId`, su `negocioId` y la autorización del UID en `clientesPermitidos`.
- Las solicitudes remotas solo usan `ALTA` y `BAJA`; no usar `CLASE` para solicitar una plaza.
- La recuperación de contraseña usa exclusivamente `FirebaseAuth.sendPasswordResetEmail`, con mensaje de éxito genérico (no revelar si el email existe) y validación de email antes de llamar a Firebase.
- No reintroducir la Vía B: sin `vinculaciones`, sin `codigoVinculacion`, sin deep links ni `EnlacePendiente`.
- No usar identificadores reales, UIDs, códigos de vinculación ni datos de prueba concretos en documentación versionada; usar placeholders.

## 8. Advertencias técnicas

El proyecto contiene deuda técnica preexistente. El agente debe detectarla y avisar, pero no debe bloquear ni ampliar una tarea para corregirla sin autorización.

- Uso del operador `!!`.
- Uso de `collectAsState()` en lugar de `collectAsStateWithLifecycle()`.
- Strings de UI escritos directamente en Kotlin.
- ViewModels o pantallas que todavía dependen directamente de tipos de `data`.
- Documentación KDoc incompleta.
- Datos ficticios de desarrollo en la inicialización de Room.
- Uso de `fallbackToDestructiveMigration()`.

Cuando se modifique una zona que contiene una de estas advertencias, preferir una solución segura en el código nuevo y mencionar la deuda sin refactorizar el resto del archivo innecesariamente.

## 9. Corrutinas y ciclo de vida

- Usar `viewModelScope` para operaciones iniciadas por ViewModels.
- No ejecutar operaciones de base de datos, DataStore o Firebase en el hilo principal de forma bloqueante.
- Usar `collectAsStateWithLifecycle()` en código nuevo de Compose.
- Respetar el ciclo de vida de Activities y composables.
- Liberar correctamente recursos y callbacks cuando corresponda.

## 10. Errores y verificación

- Leer el error completo antes de proponer una solución.
- Distinguir entre error de compilación, configuración, datos y arquitectura.
- No proponer más de dos soluciones alternativas consecutivas sin pedir más contexto.
- Tras cambios Kotlin o Gradle, ejecutar la verificación más adecuada: `.\gradlew.bat :app:assembleDebug` (Admin), `.\gradlew.bat :appCliente:assembleDebug` (Cliente) o `.\gradlew.bat assembleDebug` (ambos) desde la raíz del proyecto.
- Tras cambios en `firestore.rules`, ejecutar `npm --prefix firestore-tests test` (16 pruebas en el emulador) antes de desplegar.
- Informar de los errores de compilación o verificaciones que no se hayan podido ejecutar.

## 11. Tests

- No crear tests automáticamente durante una funcionalidad normal.
- Mantener la estrategia de escribir tests en la fase final, salvo petición expresa.
- Si el desarrollador solicita tests, seguir los patrones de los tests existentes.

## 12. Comunicación

- Responder siempre en español.
- Ser directo y técnico, explicando los términos que puedan resultar ambiguos.
- Antes de editar: indicar qué se hará y por qué.
- Después de editar: indicar rutas modificadas, comportamiento resultante y verificación realizada.
- No presentar cambios como completados si no se han aplicado y verificado.
