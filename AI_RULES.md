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
- No publicar o sustituir Security Rules sin revisar el cambio y ejecutar la matriz de pruebas acordada.
- Usar Batch o Transaction cuando una regla dependa de `getAfter()`.
- La creación de un negocio debe vincular en la misma operación el negocio y `usuarios/{uid}.negocioId`.
- Una vinculación debe actualizar en la misma operación `usuarios/{uid}`, `clientes/{clienteId}` y `vinculaciones/{codigo}`.
- Un código de vinculación debe estar pendiente y no caducado tanto al consultarse como al consumirse.
- No permitir que un cliente se vincule modificando directamente un usuario o cliente sin demostrar el código que se marca como `USADA`.
- Una reserva de cliente debe comprobar la sesión referenciada por `sesionId`, su `negocioId` y la autorización del UID en `clientesPermitidos`.
- Las solicitudes remotas solo usan `ALTA` y `BAJA`; no usar `CLASE` para solicitar una plaza.
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
- Tras cambios Kotlin o Gradle, ejecutar la verificación más adecuada, normalmente `.\gradlew.bat assembleDebug` desde la raíz del proyecto.
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
