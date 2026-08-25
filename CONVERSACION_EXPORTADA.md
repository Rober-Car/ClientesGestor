# Conversación GestorPro - Análisis Firestore Rules Límite 1000 Expresiones
## Fecha: 2026-08-24
## Estado: ⭐ RESUELTO Y AVANZADO — Ver "ACTUALIZACIÓN SESIÓN III" al final: autenticación Firebase real implementada, compilada e instalada en dispositivo físico con registro probado contra producción (Sesión II: rules refactorizadas 9/9 y desplegadas en gestorpro-50e83)

---

## RESUMEN EJECUTIVO

El Modelo A de reglas Firebase firestore.rules ha sido implementado y aprobado conceptualmente.
Sin embargo, la ejecución de pruebas dejó **6/9 aprobadas** con errores de **"maximum of 1000 expressions to evaluate"**.

El problema es redundancia de llamadas `get()` y `getAfter()` sobre `clientes/{clienteId}` en múltiples bloques de reglas.

---

## INFORME TÉCNICO SECCIÓN A: CAUSA EXACTA

### Límites disparados por:
1. **Múltiples `get()` a `/clientes/{clienteId}`** en funciones separadas:
   - `vinculacionPendiente()` y `vinculacionUsadaDespuesDelBatch()`
   - `update: esCliente()` y `update: esAdmin()` blocks
   - `create: vinculacion()` y `create: sesion()`
   - `servicioContratadoPorCliente()` llamado repetidamente

2. **Cada bloque re-lee el documento** sin reutilizar resultados previos.

3. **Emulador debe evaluar todas las expresiones** por operación, acumulando el límite.

### Error observado:
```
evaluation error at L134:24 for 'update' @ L134, evaluation error at L149:24 for 'update' @ L149, false for 'update' @ L647
evaluation error at L232:24 for 'update' @ L232, evaluation error at L251:24 for 'update' @ L251...
Unable to evaluate the expression as the maximum of 1000 expressions to evaluate has been reached.
```

---

## INFORME SECCIÓN B: Rules A SIMPLIFICAR

| Archivo/Área | Función/Block | Redundancias |
|---|---|---|
| `firestore.rules:157-166` | `vinculacionPendiente()` | `get()` a `/clientes/{clienteId}` - idCliente, negocioId, firebaseUid, codigoVinculacion |
| `firestore.rules:187-216` | `update: esCliente` (creación) | `get()` duplicadas idCliente, negocioId, firebaseUid |
| `firestore.rules:249-268` | `update: esCliente` (consumo) | `getAfter()` a `/clientes/{clienteId}` repetida |
| `firestore.rules:275-337` | `update: esAdmin` (asignar código) | Múltiples `getAfter()` y `get()` a `/clientes/{clienteId}` |
| `firestore.rules:275-337` | `update: esAdmin` (revocar) | `get()` + `getAfter()` a `/clientes/{clienteId}` |
| `firestore.rules:533-579` | `create: vinculacion` | `get()` + `getAfter()` a `/clientes/{clienteId}` múltiples |
| `firestore.rules:607-647` | `update: esAdmin` (estado PENDIENTE) | `get()` a `/clientes/{clienteId}` redundante |
| `firestore.rules:647-689` | `delete: esAdmin` | `get()` + `getAfter()` a `/clientes/{clienteId}` |
| `firestore.rules:134-149` | `update: esCliente` (generales) | `get()` a `/clientes/{clienteId}` sin necesidad |
| `firestore.rules:459-481` | `create: sesion` | `get()` a `/clientes/{clienteId}` para validar servicio |
| `firestore.rules:386-412` | `update: cliente` | `get()` a `/clientes/{clienteId}` repetido |

---

## INFORME SECCIÓN C: Código Propuesto (SIN APLICAR)

### 1. Helper `clienteActual()` - UNA sola lectura

```kotlin
function clienteActual() {
  var cid = usuarioActual().clienteId;
  if (!(cid is int)) return null;
  return get(
    /databases/$(database)/documents/clientes/$(cid)
  );
}
```

### 2. `servicioContratadoPorCliente()` en caché

```kotlin
function servicioContratadoPorCliente(servicio) {
  var c = clienteActual();
  if (c == null) return false;
  var servicios = c.data.serviciosContratados;
  return servicios is list && servicio in servicios;
}
```

### 3. `update: esCliente` - Eliminar `getAfter()` a cliente

Usar `resource.data` directamente; `affectedKeys().hasOnly(["firebaseUid"])` impide cambiar idCliente/negocioId.

### 4. `update: esAdmin` (asignar código) - Usar `resource.data`

Eliminar `getAfter()` a `/clientes/{clienteId}`; Batch garantiza valores correctos.

### 5. `delete: esAdmin` (revocación) - Usar `resource.data`

Mantener `!existsAfter(.../vinculaciones/{codigo})` y `getAfter(.../clientes/{clienteId}).data.codigoVinculacion == null`.

---

## INFORME SECCIÓN D: Garantías Conservadas

| Simplificación | Seguridad Conservada |
|---|---|
| `clienteActual()` único | `affectedKeys().hasOnly(["firebaseUid"])` impide modificar idCliente/negocioId |
| `servicioContratadoPorCliente()` en caché | Sigue leyendo lista real de servicios; un solo `get()` |
| Eliminar `get()` en `update: esCliente` | `resource.data` refleja estado previo; restricciones de keys bloquean cambios |
| Usar `resource.data` en Admin | Batch Android escribe campos correctos; Rules validan consistencia |
| Consolidar revocación | `!existsAfter(.../vinculaciones/{codigo})` garantiza eliminación |

---

## INFORME SECCIÓN E: Problemas Adicionales Modelo A

1. **`clienteActual()` asume `clienteId is int`** - Requiere validación segura si dato es string
2. **`servicioContratadoPorCliente()` depende de `usuarioActual().clienteId`** - Si no tiene perfil cliente, retorna false (correcto)
3. **Falta `getAfter()` consistente en `create: vinculacion`** - Requerido para validación atómica Batch
4. **Validación de Batch Android** - `VinculacionRepository.kt` ya corregió escritura de `negocioId` redundantado

---

## ESTADO ACTUAL DEL PROYECTO

### Android - Hecho:
- ✅ `VinculacionRepository.kt` corregido: ya no escribe `negocioId` en `clientes/{clienteId}` durante consumo de vinculación
- ✅ `assembleDebug` compila exitosamente

### Firestore Rules - Pendiente:
- ⚠️ 6/9 pruebas fallan por límite de 1000 expresiones
- ⚠️ Se necesita simplificación para reducir expresiones por operación
- ⚠️ No aplicar cambios hasta validar propuesta

### Pruebas - Estado:
- ⚠️ 3 de 9 fallan por permisos DENIED (error de expresión)
- ⚠️ No modificar tests 1-9 hasta refactorizar Rules
- ⚠️ Modelo A conceptual aprobado pero Rules necesitan optimización

### Siguientes pasos:
1. Aprobar propuesta de simplificación (secciones C-D)
2. Aplicar refactorización a `firestore.rules`
3. Volver a ejecutar `npm test`
4. Verificar todas las 9 pruebas pasen
5. Si es necesario, adaptar Android adicionalmente

---

## ARCHIVOS MODIFICADOS EN ESTA SESIÓN

- `app/src/main/java/com/roberto/gestorpro/data/firebase/VinculacionRepository.kt`
  - Línea 97-100: Eliminada escritura de `negocioId` en batch de consumición de vinculación
  - Ahora solo escribe `firebaseUid` - compatible con Modelo A Rules

- `firestore.rules`
  - Implementado Modelo A completo (ver diff anterior)
  - Sin simplificar aún para reducir límite de 1000 expresiones

- Sin modificar:
  - `app/src/main/java/com/roberto/gestorpro/data/entity/ClienteEntity.kt`
  - `app/src/main/java/com/roberto/gestorpro/ui/clientes/AñadirClienteScreen.kt`
  - Pruebas 1-9 en `firestore-tests/`
  - Firebase emulated/production

---

## PARA REANUDAR EN OTRO PC

1. Copiar este archivo `CONVERSACION_EXPORTADA.md`
2. Continuar desde el punto "Siguientes pasos" arriba
3. El estado actual es: Rules sin simplificar, Android corregido, pending validación

## COMANDOS ÚTILES PARA VERIFICAR

```powershell
# Verificar estado actual
git diff -- firestore.rules     # Reglas actuales (Modelo A implementado)
git status --short              # Archivos modificados
.\gradlew.bat assembleDebug     # Compilar Android

# Probar Rules (después de simplificar)
npm test                        # Ejecutar firestore tests
```

---

---

# ACTUALIZACIÓN 2026-08-24 (SESIÓN II) — REFACTORIZACIÓN APLICADA, VALIDADA Y DESPLEGADA

> Lo anterior (secciones A-E) quedó SUPERADO: la propuesta de simplificación se descartó
> por ser sintácticamente inválida en Rules (`var`, `if`, early returns no existen) y se
> aplicó otra refactorización por fases. Este bloque es el estado vigente.

## Resultado final

- ✅ `firestore.rules` refactorizada completa: límite de 1000 expresiones ELIMINADO
- ✅ **9/9 pruebas pasan** (`npm --prefix firestore-tests test`)
- ✅ Commit `63e88d7` "Refactor firestore.rules: elimina limite de 1000 expresiones, 9/9 pruebas OK" (+188/−202, único archivo)
- ✅ **DESPLEGADAS EN PRODUCCIÓN**: proyecto `gestorpro-50e83` (`firebase deploy --only firestore:rules`)
- ✅ Configuración creada en raíz: `firebase.json` (`{"firestore":{"rules":"firestore.rules"}}`) y `.firebaserc` (`default: gestorpro-50e83`)
- ✅ `app/google-services.json` coincide (`project_id: gestorpro-50e83`, package `com.roberto.gestorpro`)

## Helpers nuevos en firestore.rules (versión desplegada)

| Función | Líneas | Papel |
|---|---|---|
| `vinculacionValidaParaConsumo(clienteId, uid, negocioId)` | L49-79 | Valida el Batch completo de consumo con 2 `get` + 2 `getAfter`: ficha sin UID, código PENDIENTE no caducado, PENDIENTE→USADA, coherencia negocio/cliente/usuario post-Batch |
| `usuarioApuntaACliente(clienteId, negocioId)` | L83-89 | El Batch deja `usuarios/{uid}` apuntando al cliente y negocio correctos |
| `asignacionDeVinculacionValida(codigo, clienteId, negocioId)` | L94-120 | Asignación atómica ficha↔código nuevo (ficha libre, código nace PENDIENTE del propio negocio) |
| `vinculacionPendienteDeCliente(codigo, clienteId, negocioId)` | L124-131 | Revocación: código PENDIENTE del cliente/negocio indicados |
| `sesionAccesiblePorCliente(sesionId)` | L141-148 | Negocio propio + UID en clientesPermitidos + servicio contratado |

## Cambios semánticos respecto al Modelo A original (todos aprobados por el desarrollador)

1. `vinculaciones/create`: valida solo propiedad de negocio + campos (`negocioId`, `estado=PENDIENTE`, `clienteId is int`, `fechaExpiracion` futura); ya no lee la ficha del cliente.
2. `vinculaciones/delete`: solo propiedad + `PENDIENTE`; ya no exige limpiar la ficha en el mismo Batch.
3. Consumo: verificación POST-Batch vía `getAfter` — `negocioId` puede escribirse durante el Batch (las semillas lo crean `null`).
4. `clientes/create`: admite `firebaseUid` arbitrario (campo controlado por ADMIN; solo ADMIN alcanza esa regla).

## Warnings del deploy (auditados: inofensivos)

`L39:11, L58:11, L63:11, L105:11` → patrón ternario `(cond) ? lectura : null`. Aviso estático
de tipado ("Invalid type... null"); todos los usos protegidos por guards (`is list`, `!= null`,
`codigo is string`) y dirección fail-closed. Cero riesgo funcional.

## ESTADO REAL DE ANDROID (auditoría para la siguiente fase)

**CRÍTICO: la app todavía NO usa Firebase.** Las Rules van muy por delante del código Android.

- `LoginScreen.kt` es una maqueta: valida solo campos no vacíos y navega según DataStore. NO existe
  `signInWithEmailAndPassword`, ni `createUserWithEmailAndPassword`, ni `signOut` reales en todo el proyecto.
- `data/firebase/VinculacionRepository.kt` SÍ tiene el batch de consumo (usuarios←{clienteId,negocioId},
  clientes←{firebaseUid}, vinculaciones←{estado:"USADA"}) pero es **código muerto**: no está registrado
  en Hilt y ninguna pantalla lo invoca.
- **No existe** UI de ADMIN para crear códigos (`vinculaciones/{codigo}` PENDIENTE) ni para asignar
  `clientes/{id}.codigoVinculacion` (batch exigido por las Rules). Tampoco UI del CLIENTE para introducir
  el código. Sin QR.
- Desajuste repo vs Rules: el repo lee `vinculaciones/{codigo}` directamente; las Rules obtienen el
  código desde `clientes/{id}.codigoVinculacion` (asignado previamente por el flujo ADMIN).
- Todo el negocio (clientes, movimientos, gastos, clases, sesiones, reservas, solicitudes) vive SOLO en Room local.
- Sin manejo de errores Firebase: ningún try/catch de `FirebaseException`/permission-denied en la app;
  varios `viewModelScope.launch` sin captura.

## Siguiente fase (comprobar GestorPro real contra Firebase)

Antes de poder probar Auth/Vinculación/Admin hay que implementar en Android:
1. Login/registro real con FirebaseAuth + creación de `usuarios/{uid}` (`rol`, `activo:true`, `clienteId:null`, `negocioId:null`).
2. Cierre de sesión real (`FirebaseAuth.signOut()`).
3. Pantalla ADMIN "generar código de vinculación": batch = create `vinculaciones/{codigo}` PENDIENTE + update `clientes/{id}.codigoVinculacion`.
4. Pantalla CLIENTE "introducir código": inyectar `VinculacionRepository` por Hilt, adaptarlo al contrato de las Rules y llamarlo desde la UI.
5. Gestión de `permission-denied` en UI (snackbars/estados) para que nada rompa.

## Comandos útiles (este PC)

```powershell
git log --oneline -3                                   # historial
npm --prefix firestore-tests test                      # pruebas Rules (emulador)
& ".\firestore-tests\node_modules\.bin\firebase.cmd" deploy --only firestore:rules   # despliegue
```

---

---

# ACTUALIZACIÓN 2026-08-24 (SESIÓN III) — AUTENTICACIÓN FIREBASE REAL IMPLEMENTADA Y PROBADA EN DISPOSITIVO

> La app YA usa Firebase Authentication de verdad. Este bloque es el estado vigente;
> lo anterior queda como histórico (Sesión II = rules; Sesión I = análisis límite 1000).

## Resultado final

- ✅ Login/registro/logout **reales** con FirebaseAuth + documento `usuarios/{uid}`, compilados
  (`assembleDebug` BUILD SUCCESSFUL) y probados en un **móvil físico** contra producción `gestorpro-50e83`:
  registro con email nuevo → entra directo al Home del perfil elegido; email duplicado → rechazado con
  mensaje correcto en español.
- ✅ Sin dependencias nuevas (Firebase BOM ya estaba en `libs.versions.toml`; el puente `Task→suspend`
  se hizo a mano).
- ⚠️ Fase auth **sin commit** todavía. También siguen sin trackear/commitear: `firebase.json`,
  `.firebaserc`, docs actualizadas.

## Decisiones aprobadas por el desarrollador para esta fase

1. SIN verificación de email y SIN cambio/restablecimiento de contraseña.
2. Logout real con `signOut()` pero **sin borrar DataStore** (el perfil elegido persiste).
3. NO tocar: `firestore.rules`, `firestore-tests`, Room, `VinculacionRepository`, ni las pantallas
   de clientes/clases/economía.

## Archivos creados / modificados

| Archivo | Cambio |
|---|---|
| `data/firebase/AutenticacionRepository.kt` | **NUEVO.** `@Singleton @Inject(FirebaseAuth, FirebaseFirestore)`. API: `haySesionActiva()`, `registrar(email, contrasena, rol)`, `iniciarSesion(email, contrasena)`, `cerrarSesion()`; devuelve `ResultadoAutenticacion(exito, mensaje, rol)`. Registro = createUser + set `usuarios/{uid}` `{rol, activo:true, clienteId:null, negocioId:null}` (cumple Rules L194-199); si la escritura Firestore falla, **borra la cuenta recién creada** (rollback) y devuelve error. Login = signIn + lectura del perfil + bloqueo si `activo:false` (con signOut). `mensajeDe(e)` traduce los errores típicos a español. Helper privado `<T> Task<T>.esperar(): T` con `suspendCancellableCoroutine`. |
| `di/AppModule.kt` | Providers Hilt `provideFirebaseAuth()` y `provideFirebaseFirestore()`. |
| `ui/viewmodel/MainViewModel.kt` | Inyecta el repositorio. Nuevos: `autenticando: StateFlow<Boolean>`, `destinoSegunTipo()`, `destinoInicialSegunSesion()` (DataStore + sesión Firebase), `iniciarSesion(...)`, `registrarse(...)` (valida ≥6 chars y coincidencia; mapea `TipoUsuario.ADMINISTRADOR→"ADMIN"`, `CLIENTE→"CLIENTE"`), `cerrarSesion()`. Devuelven `String?` (error o null). |
| `navigation/Routes.kt` | Añadido `const val REGISTRO = "registro"` (KDoc). |
| `navigation/AppNavigation.kt` | Ruta `composable(Routes.REGISTRO)` + arranque inicial ahora con `destinoInicialSegunSesion()`: sin perfil guardado → selección; con perfil y sesión Firebase restaurada → Home directo; con perfil sin sesión → Login. |
| `ui/auth/RegistroScreen.kt` | **NUEVA.** Email/contraseña/repetir, muestra el perfil elegido, botón deshabilitado hasta formulario válido, spinner mientras `autenticando`, errores bajo el formulario, enlace "Ya tengo cuenta". Éxito → destino según perfil con `popUpTo(LOGIN){inclusive=true}`. |
| `ui/auth/LoginScreen.kt` | Botón "Entrar" cableado al login real (antes decorativo), spinner, `mensajeError` mostrado, TextButton "¿No tienes cuenta? Crear una" → REGISTRO. Import `TipoUsuario` eliminado (ya no se usa). |
| `ui/configuracion/CuentaScreen.kt` | Diálogo "Cerrar sesión": ahora llama `mainViewModel.cerrarSesion()` antes de navegar a LOGIN (`popUpTo(0)`). |
| `ui/configuracion/PreferenciasScreen.kt` | Igual que CuentaScreen + nuevo parámetro `mainViewModel: MainViewModel = hiltViewModel()`. |

## Errores de compilación corregidos (3 reales, resto cascada)

1. `esperar()` devolvía `Task<T>` (resume con `this`) → `.user/.exists()/...` no resolvían. Corregido a devolver `T`.
2. Falta `import kotlinx.coroutines.flow.StateFlow` en `MainViewModel`.
3. Falta `import androidx.compose.material3.Icon` en `RegistroScreen`.

## Prueba en dispositivo físico (Xiaomi 25080RABDG, MIUI)

- Emulador descartado: primero sin espacio en disco (0,8 GB libres → FATAL del emulador), luego
  inestable; el desarrollador decidió probar en móvil real.
- Instalación vía adb falló dos veces:
  1. `INSTALL_FAILED_UPDATE_INCOMPATIBLE` → existía una instalación previa con otra firma; se desinstaló (datos Room locales perdidos, asumido por el desarrollador).
  2. `INSTALL_FAILED_USER_RESTRICTED` → MIUI bloquea instalar por USB; el desarrollador instaló manualmente (Run ▶ o copiando el APK `app/build/outputs/apk/debug/app-debug.apk`).
- Logcat tras abrir: `FirebaseInitProvider: FirebaseApp initialization successful`, sin FATAL.
- Registro email nuevo → OK, Home correcto. Primer intento con email ya existente → rechazado con
  mensaje adecuado (flujo de errores verificado).

## Verificación de `usuarios/{uid}` — PENDIENTE ABIERTO

El logcat NO muestra el contenido del documento escrito (el SDK no lo registra). Garantía lógica:
las Rules solo permiten el create con `rol ∈ {ADMIN,CLIENTE} && activo==true && clienteId==null &&
negocioId==null`, y el éxito observado implica que la validación pasó. Comprobación definitiva pendiente:

- [ ] Mirar en consola Firebase → Firestore → colección `usuarios` que el doc tiene exactamente
      `rol: ADMIN` (o CLIENTE según perfil), `activo: true`, `clienteId: null`, `negocioId: null`.
- [ ] Matar la app desde recientes y reabrir → debe ir directo al Home sin pedir login (sesión Firebase restaurada).
- [ ] Cerrar sesión desde Cuenta/Preferencias → vuelve al Login conservando el perfil en DataStore.
- [ ] Login con contraseña errónea → mensaje en español bajo el formulario.

## Siguiente fase sugerida (vinculación real)

1. Pantalla ADMIN "generar código de vinculación": batch = create `vinculaciones/{codigo}` PENDIENTE +
   update `clientes/{id}.codigoVinculacion` (contrato de `asignacionDeVinculacionValida`, rules L94-120).
2. Pantalla CLIENTE "introducir código": inyectar `VinculacionRepository` por Hilt y adaptarlo al
   contrato de `vinculacionValidaParaConsumo` (rules L49-79), llamándolo desde la UI.
3. Gestión de `permission-denied` en UI para todo lo remoto.

## Comandos útiles añadidos (este PC)

```powershell
.\gradlew.bat assembleDebug            # compilar APK debug
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices -l        # móvil conectado
# Instalar en el móvil (MIUI puede exigir hacerlo manualmente desde Android Studio):
adb -s <serial> install -r app\build\outputs\apk\debug\app-debug.apk
```

---

---

# ACTUALIZACIÓN 2026-08-25 (SESIÓN IV) — DISEÑO DEFINITIVO DE VINCULACIÓN

> Este bloque es el estado vigente del diseño de vinculación.
> La Sesión III (auth real) y la Sesión II (Rules refactorizadas) siguen vigentes.
> Lo anterior queda como histórico.

## Resumen del diseño

Se define un sistema de vinculación con **dos vías** que reutiliza la infraestructura
existente de `vinculaciones/{codigo}`:

### Código maestro del negocio

- Un único código por negocio.
- Se configura durante el alta inicial del ADMIN.
- El ADMIN puede modificarlo desde Configuración → Mi negocio.
- Sirve para que un CLIENTE que llega por su cuenta pueda registrarse y vincularse al negocio (Vía A).
- El código maestro **no identifica a un cliente concreto**.
- Cambiar el código maestro **no afecta** a clientes ya vinculados.

### Enlace individual de vinculación

- Cuando un ADMIN crea manualmente un cliente, el sistema genera un enlace/token
  de vinculación asociado exclusivamente a esa ficha.
- El ADMIN comparte ese enlace con el cliente.
- El enlace identifica exactamente `clientes/{clienteId}` sin que el CLIENTE tenga
  que buscar fichas por DNI.
- El token es aleatorio, no es el ID del cliente, y tiene expiración.
- Es de uso único.
- Si caduca o se revoca, el ADMIN puede generar otro.

### Vía A: CLIENTE se registra con código maestro

```
1. CLIENTE introduce código maestro
2. App busca en negocios_publicos/{id} → obtiene negocioId
3. App genera idCliente = abs(codigo.hashCode())
4. App genera código de vinculación aleatorio
5. Batch:
   a. set(vinculaciones/{codigo}, { negocioId, clienteId, estado:"PENDIENTE", fechaExpiracion })
   b. set(clientes/{idCliente}, { idCliente, negocioId, firebaseUid: uid, vinculacionCode: codigo, ... })
   c. update(usuarios/{uid}, { clienteId: idCliente, negocioId: negocioId })
6. CLIENTE queda vinculado
```

### Vía B: CLIENTE reclama ficha creada por ADMIN

```
1. ADMIN crea cliente (firebaseUid: null)
2. ADMIN genera código de vinculación aleatorio
3. Batch ADMIN:
   a. set(clientes/{idCliente}, { ..., firebaseUid: null, vinculacionCode: codigo })
   b. set(vinculaciones/{codigo}, { negocioId, clienteId, estado:"PENDIENTE", fechaExpiracion })
4. ADMIN comparte enlace con el cliente
5. CLIENTE pulsa enlace → se registra/inicia sesión
6. CLIENTE pulsa "Vincular"
7. Batch CLIENTE:
   a. update(clientes/{idCliente}, { firebaseUid: uid, negocioId: negocioId })
   b. update(vinculaciones/{codigo}, { estado: "USADA" })
   c. update(usuarios/{uid}, { clienteId: idCliente, negocioId: negocioId })
8. CLIENTE queda vinculado
```

### Restricciones de seguridad

| Regla | Detalle |
|---|---|
| Un CLIENTE solo puede vincularse una vez | `usuarios/{uid}` exige `clienteId == null` y `negocioId == null` |
| Código maestro no da acceso a fichas admin | `clientes/create` de CLIENTE crea documento nuevo; `clientes/update` de CLIENTE exige `firebaseUid == null` |
| Enlace expirado no funciona | `vinculacionValidaParaConsumo()` exige `fechaExpiracion > request.time` |
| Enlace ya usado no funciona | `clientes/update` exige `resource.data.firebaseUid == null` |
| Cambiar código maestro no rompe vínculos | Solo se modifica `negocios/{id}.codigoMaestro` y `negocios_publicos/{id}` |

### Generación de idCliente

- **Vía A:** `idCliente = abs(codigo.hashCode())` — determinista, único por negocio, sin lectura extra.
- **Vía B:** El ADMIN asigna `idCliente` al crear la ficha (secuencial con contador en `negocios/{id}.contadorClientes`).

### Validación atómica en Rules

**Vía B** se valida con la función existente `vinculacionValidaParaConsumo()` sin cambios.

**Vía A** se valida con una nueva función `userUpdateValida()` que comprueba:
- El batch toca el documento del cliente (`existsAfter`)
- El batch toca el documento de la vinculación (`existsAfter`)
- La vinculación apunta al mismo `clienteId` y `negocioId` del usuario
- El usuario tiene `clienteId` y `negocioId` correctamente asignados

### Cambios en firestore.rules

| Colección | Cambio |
|---|---|
| `negocios_publicos/{id}` | **NUEVA.** `get/list: autenticado(); create/update: esAdmin() && getAfter().negocioId == id; delete: false` |
| `vinculaciones/{codigo}` | Añadir `create` para CLIENTE (Vía A): valida estado, fechaExpiracion, negocioId, clienteId |
| `clientes/{idCliente}` | Añadir `create` para CLIENTE (Vía A): valida firebaseUid, idCliente, vinculacionCode, serviciosContratados |
| `usuarios/{uid}` | Añadir `update` para CLIENTE (Vía A): affectedKeys solo clienteId/negocioId, sin vinculacionValidaParaConsumo |
| `negocios/{id}` | Añadir `codigoMaestro is string` en `create` |

### Funciones eliminadas

| Función | Motivo |
|---|---|
| `asignacionDeVinculacionValida()` | El ADMIN ya no asigna códigos individuales |
| `vinculacionPendienteDeCliente()` | No hay códigos pendientes por revocar |

### Funciones nuevas

| Función | Motivo |
|---|---|
| `userUpdateValida(clienteId, negocioId)` | Valida batch atómico de Vía A (cliente + vinculación + usuario) |

### Funciones que se mantienen

| Función | Motivo |
|---|---|
| `vinculacionValidaParaConsumo()` | Sigue siendo válida para Vía B |
| `usuarioApuntaACliente()` | Sigue siendo válida |
| `servicioContratadoPorCliente()` | Sin cambios |
| `sesionAccesiblePorCliente()` | Sin cambios |

### Pruebas

17 pruebas totales (9 existentes + 8 nuevas):

| # | Estado | Qué valida |
|---|---|---|
| 1 | Sin cambios | CLIENTE no lee otro cliente |
| 2 | Sin cambios | CLIENTE no modifica permisos |
| 3 | Sin cambios | ADMIN solo lee datos de su negocio |
| 4 | Sin cambios | CLIENTE no lee movimientos |
| 5 | **Se reescribe** | Vía A: vinculación por código maestro |
| 6 | Sin cambios | CLIENTE vinculado solo accede a sus datos |
| 7 | Sin cambios | CLIENTE solo usa sesiones de servicios contratados |
| 8 | **Se adapta** | ADMIN solo escribe en su negocio |
| 9 | **Se reescribe** | Vía B: reclamación de ficha con enlace |
| 10 | **Nueva** | CLIENTE no vinculado puede leer negocios_publicos |
| 11 | **Nueva** | CLIENTE no puede modificar negocios_publicos |
| 12 | **Nueva** | Enlace expirado no funciona |
| 13 | **Nueva** | Enlace ya usado no funciona |
| 14 | **Nueva** | CLIENTE ya vinculado no puede reclamar otra ficha |
| 15 | **Nueva** | ADMIN puede revocar enlace |
| 16 | **Nueva** | ADMIN puede regenerar enlace |
| 17 | **Nueva** | Cambio de código maestro no rompe vínculos |

### Archivos a modificar

| Archivo | Cambio |
|---|---|
| `firestore.rules` | Añadir negocios_publicos, vinculaciones/create CLIENTE, clientes/create CLIENTE, userUpdateValida(), eliminar funciones obsoletas |
| `VinculacionRepository.kt` | Reescritura completa |
| `NegocioRepository.kt` | **NUEVO** |
| `MiNegocioScreen.kt` | Añadir campo codigoMaestro, modo dual |
| `CrearNegocioScreen.kt` | **NUEVA** |
| `VincularClienteScreen.kt` | **NUEVA** |
| `AppNavigation.kt` | Añadir rutas |
| `Routes.kt` | Añadir rutas |
| `MainViewModel.kt` | Añadir lógica de creación de negocio y vinculación |
| `AppModule.kt` | Registrar repositorios en Hilt |
| `firestore-tests/firestore.rules.test.cjs` | Reescribir 5,8,9; añadir 10-17 |

### Archivos que NO se modifican

| Archivo | Motivo |
|---|---|
| `AutenticacionRepository.kt` | Registro sin cambios |
| `ClienteEntity.kt` | Room sin cambios |
| `ClientesDatabase.kt` | Sin cambios |
| DAOs | Sin cambios |
| Modelos | Sin cambios |

## Comandos útiles (este PC)

```powershell
.\gradlew.bat assembleDebug            # compilar APK debug
npm --prefix firestore-tests test      # pruebas Rules (emulador)
& ".\firestore-tests\node_modules\.bin\firebase.cmd" deploy --only firestore:rules   # despliegue
```
---

---

# ACTUALIZACION 2026-08-25 (SESION V) — VINCULACION DEFINITIVA IMPLEMENTADA, DESPLEGADA Y APK LISTO

> Este bloque es el estado vigente. Sesiones anteriores quedan como historico.
> La app compila (BUILD SUCCESSFUL), las Rules estan desplegadas en produccion
> `gestorpro-50e83` y el APK debug esta instalado en el Xiaomi (serial
> `batchiqwxkbylnzl`, instalacion con `install -r` conservando datos Room).
> SIGUIENTE PASO: pruebas manuales de integracion paso a paso (flujo abajo).

## Diseno definitivo aprobado e implementado

1. **idCliente compartido Room/Firestore**: Int aleatorio generado con
   `Random.nextInt(1_000_000_000, Int.MAX_VALUE)`, comprobacion de existencia en
   Transaction y reintento ante colision (max. 5). Sin contador en negocios y sin
   hashCode(). El CLIENTE no tiene NINGUN permiso de escritura sobre `negocios`.
2. **Estados**: se replican los nombres exactos del enum Room: ACTIVO, BAJA,
   ARCHIVADO, REGISTRADO. MOROSO se calcula y nunca se almacena.
3. **Replica write-through**: alta/edicion de cliente del ADMIN se replica a
   Firestore con el mismo id; si falla NO se revierte lo local, se informa y hay
   boton "Reintentar sincronizacion" (`ClienteViewModel.replicar()`).
4. **Via A** (codigo maestro): buscar en `negocios_publicos` -> Transaction:
   comprobar inexistencia de clientes/{id}, set ficha con UID propio, update
   usuarios/{uid}. Sin vinculaciones.
5. **Via B** (enlace individual): token SecureRandom de 24 caracteres
   alfanumericos (sin ambiguos), 7 dias de expiracion, uso unico, revocable y
   regenerable. Batch atomico ficha<->vinculaciones validado por las nuevas
   funciones `asignacionTokenValida()` y `revocacionTokenValida()` + `!existsAfter`.
6. **Deep link**: `gestorpro://vincular/{token}` (custom scheme, singleTask,
   holder `EnlacePendiente`). Reclamacion automatica tras login/registro via
   `MainViewModel.destinoSegunTipo()`. Estructura lista para App Links HTTPS.

## Cambios en firestore.rules (DESPLEGADAS en gestorpro-50e83)

| Seccion | Cambio |
|---|---|
| Funciones | +`creacionDirectaValida()`, +`asignacionTokenValida()`, +`revocacionTokenValida()`. Eliminadas `asignacionDeVinculacionValida()` y `vinculacionPendienteDeCliente()` |
| negocios | create exige `codigoMaestro is string`. Sin contadorClientes |
| negocios_publicos | get/list autenticado; create/update solo ADMIN propietario (keys: codigoMaestro, nombre) |
| clientes | +create CLIENTE (Via A); 2 bloques update ADMIN estrechos para codigoVinculacion (solo fichas sin UID, solo esa clave, atomicos con vinculaciones) |
| usuarios | +update CLIENTE Via A con `creacionDirectaValida()` |

Warnings del deploy (auditados, inofensivos, patron ternario null): L39:11, L58:11, L63:11.

## Pruebas: 17/17 OK (emulador) tras reescritura

Prueba 5 reescrita (Via A + colision/sobrescritura DENY); 15 reescrita (revocacion
atomica, limpiar sin borrar -> DENY); 16 reescrita (token huerfano -> DENY, ficha
con UID -> DENY, asignacion y regeneracion atomicas -> ALLOW); +10,11,12,13,14,17.

## Archivos clave de esta sesion

Modificados: firestore.rules, firestore-tests/firestore.rules.test.cjs,
VinculacionRepository.kt, AutenticacionRepository.kt (solo visibilidad de
`esperar()` a internal), ClienteViewModel.kt, MainViewModel.kt, Routes.kt,
AppNavigation.kt, MainActivity.kt, AndroidManifest.xml (intent-filter +
singleTask), VincularClienteScreen.kt, AnadirClienteScreen.kt (aviso sincro),
PerfilClienteAdministradorScreen.kt (boton), MiNegocioScreen.kt, HomeClienteScreen.kt.
Nuevos: ClienteRemotoRepository.kt, NegocioRepository.kt, EnlacePendiente.kt,
VincularClienteScreen.kt (ui/auth), EnlaceVinculacionScreen.kt (+VM),
CrearNegocioScreen.kt.

## PENDIENTE INMEDIATO (retomar aqui)

### Pruebas manuales en el movil (instalado, datos Room conservados)
ADMIN:
1. Registrar ADMIN nuevo
2. Crear negocio (Mi negocio -> Crear negocio en la nube)
3. Verificar usuarios/{uid}.negocioId en consola Firebase
4. Verificar negocios/{negocioId} creado (negocioId = uid del ADMIN)
5. Crear un cliente desde ADMIN
6. Verificar Room local + replica en clientes/{idCliente} (mismo id)
7. Generar enlace (Perfil cliente -> "Vinculacion en la nube")
8. Verificar vinculaciones/{token} y clientes/{id}.codigoVinculacion iguales
CLIENTE:
9. Abrir el enlace gestorpro://vincular/{token}
10. Registrar CLIENTE nuevo
11. Verificar token precargado en pantalla
12. Reclamar la ficha
13. Verificar usuarios/{uid}.clienteId y negocioId
14. Verificar clientes/{id}.firebaseUid = UID del cliente
15. Comprobar entrada al Home del cliente
Despues: edicion de perfil, regeneracion y revocacion del enlace.

### Otros pendientes
- COMMIT de todo (nada commiteado de Sesion IV+V): docs, rules, tests, Android.
- Decidir que hacer con `firestore-tests/firestore-debug.log` (log del emulador;
  recomendado anadirlo a .gitignore antes del commit).
- Fase futura: ediciones del CLIENTE hacia la lista Room del ADMIN (lectura),
  borrados como baja logica remota, App Links HTTPS, gestion de
  fechaInicioActual/fechaFinActual del contrato de clientes.

## Comandos utiles (este PC)

```powershell
.\gradlew.bat assembleDebug            # compilar APK debug
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices          # movil
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s batchiqwxkbylnzl install -r "C:\Users\Roberto\AndroidStudioProjects\GestorPro\app\build\outputs\apk\debug\app-debug.apk"
npm --prefix firestore-tests test      # pruebas Rules (emulador)
& ".\firestore-tests\node_modules\.bin\firebase.cmd" deploy --only firestore:rules   # despliegue
```
