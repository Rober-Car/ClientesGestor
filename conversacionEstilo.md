# conversacionEstilo — Contexto de estilo (GestorPro)

> Archivo de continuidad. La IA debe leerlo para saber exactamente dónde quedamos
> en la sesión de rediseño visual de GestorPro (Admin y Cliente).
> Última actualización: 2026-08-28.

## 1. Resumen rápido — dónde estamos

Se está trabajando **solo en el estilo visual** de GestorPro (dos apps Android en un mismo
proyecto Gradle: `:app` = Admin, `:appCliente` = Cliente, Firebase compartido `gestorpro-50e83`).

En esta sesión nos centramos en rediseñar la **pantalla Home del Admin** con un look
Material 3 "dashboard". La última versión aplicada y compilada usa **cards casi blancas con
tinte muy suave del color** y **el color protagonista solo en la celda del icono**.

**Estado de alcance pedido por el usuario:** `Solo Admin`. El Cliente NO se ha tocado en este
rediseño de Home (sigue con su layout de columna original/restaurado).

## 2. Decisiones de diseño acordadas (Admin Home)

- Fondo general de la pantalla: `MaterialTheme.colorScheme.surface`.
- Cabecera: fondo gris claro `surfaceContainerLow`, con logo (o placeholder `primaryContainer`)
  + etiqueta "Panel principal" + `nombreNegocio`. Sin barra oscura.
- Grid de 2 columnas (`LazyVerticalGrid`, `GridCells.Fixed(2)`), separación 16.dp,
  padding 20.dp horizontal / 12.dp vertical.
- `MenuCard` (cuadrada, 140.dp de alto): `Card(onClick=...)`, `shape = 20.dp`,
  `elevation = 1.dp` (sombra suave), padding interno 20.dp. Icono en celda redondeada
  12.dp arriba-izquierda, textos abajo (`Arrangement.SpaceBetween`).
- **Combinación de colores final aplicada** (hex fijos, elegidos por el usuario):

  | Sección   | Color hex  | containerColor (fondo card)              | iconContainerColor | iconTint  |
  |-----------|------------|------------------------------------------|--------------------|-----------|
  | Clientes  | #2196F3    | `Color(0xFF2196F3).copy(alpha = 0.12f)` | `Color(0xFF2196F3)`| `Color.White` |
  | Clases    | #43A047    | `Color(0xFF43A047).copy(alpha = 0.12f)` | `Color(0xFF43A047)`| `Color.White` |
  | Economía  | #FB8C00    | `Color(0xFFFB8C00).copy(alpha = 0.12f)` | `Color(0xFFFB8C00)`| `Color.White` |
  | Ajustes   | #78909C    | `Color(0xFF78909C).copy(alpha = 0.12f)` | `Color(0xFF78909C)`| `Color.White` |

- Correcciones que el usuario pidió y se aplicaron:
  - Usar `Card(onClick = onClick)` en vez de `Modifier.clickable`.
  - Contraste de Ajustes: icono neutro (gris), no `surface` sobre `outline`.
  - Cabecera "ligeramente diferenciada" (fondo gris claro), NO oscura.
  - Cards lo más neutras posible: color solo en iconos, fondo muy suave.

## 3. Estado actual de archivos (Admin)

- `app/src/main/java/com/roberto/gestorpro/ui/components/MenuCard.kt`
  - Versión cuadrada 140.dp. Firma:
    `MenuCard(titulo, descripcion, icono, containerColor = surface, iconContainerColor = primaryContainer, iconTint = onPrimaryContainer, onClick)`.
  - `elevation = 1.dp`, `shape = 20.dp`, padding interno 20.dp.
- `app/src/main/java/com/roberto/gestorpro/ui/home/HomeScreen.kt`
  - Header con logo (`AsyncImage(File(logoNegocio))` o placeholder `AccountBox` en `primaryContainer`),
    "Panel principal" + `nombreNegocio`, fondo `surfaceContainerLow`.
  - Título "Accesos rápidos".
  - `LazyVerticalGrid` 2 col con las 4 `MenuCard` de la tabla de colores de arriba.
  - Usa `Routes.CLIENTES`, `Routes.CLASES`, `Routes.ECONOMIA`, `Routes.CONFIGURACION`.
  - `MainViewModel` expone `nombreNegocio` y `logoNegocio`.
  - Imports añadidos: `androidx.compose.ui.graphics.Color`, `coil3.compose.AsyncImage`, `java.io.File`,
    `lazy.grid.*`, etc.

## 4. Estado del Cliente (sin cambios en este rediseño)

- `appCliente/.../ui/home/HomeScreen.kt`: **restaurado a layout de columna** (no grid).
  Header "GestorPro Cliente" + subtítulo, `Card` de aviso `errorContainer` si `!vinculado`,
  y `MenuCard` a ancho completo en orden: Mi perfil, Clases y sesiones, Vinculación (solo si
  `!vinculado`), Mi cuenta, Configuración.
- `appCliente/.../ui/components/MenuCard.kt`: estilo original (Card elev 6.dp, icono círculo 64.dp
  con `shadowElevation = 4.dp`, color azul fijo `0xFF1E88E5`, flecha `KeyboardArrowRight`).
- Trabajo previo ya existente en Cliente (no es parte de este rediseño, se mantiene):
  ruta `CONFIGURACION` + `ConfiguracionScreen` (tema claro/oscuro/sistema), `BotonSelectorFoto`
  en ambas apps, helpers de cámara en `FotoUtils`, foto en `CompletarPerfilScreen`/`EditarPerfilScreen`.

## 5. Pendientes / siguiente paso

- **El usuario quería VER cómo queda** la última versión (hex + tinte 0.12f) en dispositivo.
  Posibles ajustes que él mismo sugirió:
  - Si el tinte se ve mucho: bajar `alpha` a `0.08f`.
  - Si prefiere icono sobre blanco en vez de color fuerte: cambiar `iconContainerColor` a blanco
    y `iconTint` al color fuerte.
  - Subir elevación a `2.dp` si quiere más contraste de sombra.
- **Rediseño del Home del Cliente (NO hecho aún):** el usuario contestó en una pregunta que,
  cuando se aplique al cliente, quiere: grid 2 col + tarjeta "Vinculación" condicional (solo si
  `!vinculado`) + aviso si no vinculado. Queda pendiente y fuera de alcance hasta que lo pida.
- Convención del proyecto: responder en español, no usar emojis salvo petición, no romper lógica
  (solo estética salvo que se indique), color de acento cliente `0xFF1E88E5`.

## 6. Notas técnicas importantes

- `TakePicture` no requiere permiso de cámara en el manifest (usa app de cámara del sistema).
- Cliente ya tiene `FileProvider` + `res/xml/file_paths.xml` (cache-path `fotos_camara`).
- `material-icons-extended` está en `appCliente/build.gradle.kts`.
- `tonalElevation` NO existe como parámetro de `CardDefaults.cardElevation()` ni
  `surfaceColorAtElevation` en la versión de M3 del proyecto (usar `Surface(tonalElevation=...)` si hace falta).
- Comando de build usado: `.\gradlew.bat :app:assembleDebug --offline -q`
  (o `:appCliente:assembleDebug` para el cliente).
- El cliente `MainViewModel` SÍ expone `logoNegocio` (se puede reusar el mismo header que Admin).

## 7. Archivos clave para retomar

- `app/src/main/java/com/roberto/gestorpro/ui/components/MenuCard.kt` (Admin)
- `app/src/main/java/com/roberto/gestorpro/ui/home/HomeScreen.kt` (Admin)
- `appCliente/src/main/java/com/roberto/gestorpro/cliente/ui/home/HomeScreen.kt` (Cliente, sin tocar)
- `appCliente/src/main/java/com/roberto/gestorpro/cliente/ui/components/MenuCard.kt` (Cliente, sin tocar)
- `app/src/main/java/com/roberto/gestorpro/ui/viewmodel/MainViewModel.kt` (tiene logoNegocio/nombreNegocio)
- `appCliente/src/main/java/com/roberto/gestorpro/cliente/ui/viewmodel/MainViewModel.kt` (tiene logoNegocio)
