# conversacionEstilo — Contexto de estilo (GestorPro)

> Archivo de continuidad. La IA debe leerlo para saber exactamente dónde quedamos
> en la sesión de rediseño visual de GestorPro (Admin y Cliente).
> Última actualización: 2026-08-28.

## 1. Resumen rápido — dónde estamos

Se está trabajando **solo en el estilo visual** de GestorPro (dos apps Android en un mismo
proyecto Gradle: `:app` = Admin, `:appCliente` = Cliente, Firebase compartido `gestorpro-50e83`).

Hemos aplicado un lenguaje visual Material 3 moderno y coherente en dos frentes:

- **Home del Admin** (dashboard 2×2, cards con color solo en el icono).
- **Pantallas de tema/preferencias**: `ConfiguracionScreen` (Cliente, "Ajustes") y
  `PreferenciasScreen` (Admin, "Preferencias") rediseñadas con el mismo patrón de
  "fila con icono + título + descripción + indicador de selección".

**Regla fundamental en todas estas tareas:** SOLO presentación. Nunca se toca lógica,
ViewModels, repositorios, navegación, rutas, entidades, Firebase ni Room. Si compila,
no se hacen cambios adicionales.

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

## 3. Patrón reutilizable — filas de opción de tema (Ajustes/Preferencias)

Ambas pantallas de tema usan exactamente el mismo concepto visual (copiado de la pantalla
de Ajustes del Cliente y replicado en Preferencias del Admin). Composable privado `TemaOption`:

- **Una sola Card** agrupa las 3 opciones (Claro / Oscuro / Sistema). No se crean 3 cards.
  Card: `shape = 16.dp`, `containerColor = surface`, `elevation = 0.dp`,
  `border = BorderStroke(1.dp, outlineVariant)`.
- Fila `TemaOption` (`Row`, `verticalAlignment = CenterVertically`,
  `horizontalArrangement = Arrangement.spacedBy(16.dp)`, `clickable(onClick)`):
  - **Icono** a la izquierda (`24.dp`): `LightMode` / `DarkMode` / `SettingsBrightness`
    (disponibles vía `material-icons-extended`, presente en ambas apps).
  - **Columna** con `titleMedium` (título) + `bodyMedium` (descripción secundaria):
    "Claro"→"Tema claro", "Oscuro"→"Tema oscuro", "Seguir configuración del sistema"→"Según el dispositivo".
  - **Indicador** `RadioButton` a la derecha (`selectedColor = azul #1E88E5`).
- **Opción seleccionada:** `background = azul.copy(alpha = 0.08f)`, icono y título en azul
  `#1E88E5` con `FontWeight.Bold`; las no seleccionadas en `onSurfaceVariant` (gris).
- **Divisores:** `HorizontalDivider` con `outlineVariant` entre filas (con `padding horizontal 16.dp`).
- AZUL = selección de tema / acciones principales; ROJO = cerrar sesión / destructivo; GRISES = secundario.
- **No usar `Modifier.weight`** en este patrón: en la versión de Compose del proyecto el import
  `androidx.compose.foundation.layout.weight` resuelve a un símbolo `internal` y falla la compilación.
  Se usa `Arrangement.spacedBy(16.dp)` en su lugar (funciona sin overflow).

### 3a. Pantalla de Preferencias del Admin (`PreferenciasScreen.kt`)
- Cabecera idéntica a la del resto (surfaceContainerLow, padding 20/16, título "Preferencias",
  `IconButton` con `ArrowBack` → `navController.popBackStack()`).
- Título de sección "Apariencia".
- "Cerrar sesión": `TextButton` rojo `#F44336` con icono `Logout`
  (`Icons.AutoMirrored.Filled.Logout`), ancho completo, separado 32.dp de la card.
  Sin tocar `mostrarDialogoCerrarSesion`, `mainViewModel.cerrarSesion()`, navegación ni `popUpTo(0)`.
- Diálogo `AlertDialog` intacto (textos "Cerrar sesión" / "¿Seguro que quieres cerrar sesión?" /
  "Cancelar" / "Cerrar sesión", título azul).

### 3b. Pantalla de Ajustes del Cliente (`ConfiguracionScreen.kt`)
- Cabecera idéntica pero título visible "Ajustes" (el nombre interno `ConfiguracionScreen`/
  `Routes.CONFIGURACION` NO se cambia).
- Misma Card y patrón `TemaOption` que arriba. Sin tocar `themeMode`, `setThemeMode`,
  `THEME_CLARO/OSCURO/SISTEMA`, `popBackStack`.

## 4. Estado de archivos

### Admin (`:app`)
- `ui/components/MenuCard.kt` — cuadrada 140.dp, `Card(onClick)`, `elevation = 1.dp`, `shape = 20.dp`.
- `ui/home/HomeScreen.kt` — header `surfaceContainerLow` + logo/nombreNegocio + grid 2 col.
- `ui/configuracion/PreferenciasScreen.kt` — **rediseñada** (patrón TemaOption + Cerrar sesión).
- `ui/viewmodel/MainViewModel.kt` — expone `nombreNegocio` y `logoNegocio` (sin cambios de lógica).

### Cliente (`:appCliente`)
- `ui/home/HomeScreen.kt` — **pendiente de rediseño a grid 2 col** (ver §5). Hoy: columna original.
- `ui/components/MenuCard.kt` — estilo original (elev 6.dp, icono círculo 64.dp azul fijo, flecha).
- `ui/configuracion/ConfiguracionScreen.kt` — **rediseñada** (patrón TemaOption, título "Ajustes").
- `ui/viewmodel/MainViewModel.kt` — expone `logoNegocio` (reusable para header).

## 5. Pendientes / siguiente paso (visual)

- **Home del Cliente aún NO rediseñado** a grid 2 col. El usuario quiso: grid 2 col +
  tarjeta "Vinculación" condicional (solo si `!vinculado`) + aviso si no vinculado.
  Fuera de alcance hasta que lo pida.
- **Indicador de estado del Cliente (Home) — Fase 1 hecha, Fase 2 pendiente (NO es solo visual):**
  `HomeClientEstadoIndicator` ya tiene estilo moderno (bola 16.dp, padding 18.dp, título
  `titleMedium`+Bold, fecha `bodyMedium`, fondo `azul.copy(alpha=0.08f)`, call-site
  `padding(horizontal = 20.dp)`). Usa valores MOCK. La Fase 2 (conectar a datos reales de
  Firestore vía campos de periodo mantenidos por el Admin) es trabajo de lógica, no de este archivo.
- Ajustes finos que el usuario puede pedir en Home Admin: bajar `alpha` a `0.08f`, o icono sobre
  blanco, o elevación `2.dp`.
- Convención del proyecto: responder en español, no usar emojis salvo petición, no romper lógica
  (solo estética salvo que se indique), color de acento cliente `0xFF1E88E5` (azul selección).

## 6. Notas técnicas importantes

- `TakePicture` no requiere permiso de cámara en el manifest (usa app de cámara del sistema).
- Cliente ya tiene `FileProvider` + `res/xml/file_paths.xml` (cache-path `fotos_camara`).
- `material-icons-extended` está en `app/build.gradle.kts` y `appCliente/build.gradle.kts`
  (necesario para `LightMode`/`DarkMode`/`SettingsBrightness`/`Logout`).
- `tonalElevation` NO existe como parámetro de `CardDefaults.cardElevation()` ni
  `surfaceColorAtElevation` en la versión de M3 del proyecto (usar `Surface(tonalElevation=...)` si hace falta).
- Comando de build usado: `.\gradlew.bat :app:assembleDebug` (Admin) /
  `.\gradlew.bat :appCliente:assembleDebug` (Cliente). `BUILD SUCCESSFUL` en ambos rediseños.
- El cliente `MainViewModel` SÍ expone `logoNegocio` (se puede reusar el mismo header que Admin).

## 7. Archivos clave para retomar

- `app/src/main/java/com/roberto/gestorpro/ui/components/MenuCard.kt` (Admin)
- `app/src/main/java/com/roberto/gestorpro/ui/home/HomeScreen.kt` (Admin)
- `app/src/main/java/com/roberto/gestorpro/ui/configuracion/PreferenciasScreen.kt` (Admin, rediseñada)
- `appCliente/src/main/java/com/roberto/gestorpro/cliente/ui/home/HomeScreen.kt` (Cliente, sin tocar)
- `appCliente/src/main/java/com/roberto/gestorpro/cliente/ui/components/MenuCard.kt` (Cliente, sin tocar)
- `appCliente/src/main/java/com/roberto/gestorpro/cliente/ui/configuracion/ConfiguracionScreen.kt` (Cliente, rediseñada)
- `app/src/main/java/com/roberto/gestorpro/ui/viewmodel/MainViewModel.kt` (logoNegocio/nombreNegocio)
- `appCliente/src/main/java/com/roberto/gestorpro/cliente/ui/viewmodel/MainViewModel.kt` (logoNegocio)
