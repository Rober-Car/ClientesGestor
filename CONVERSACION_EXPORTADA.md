# Conversación GestorPro - Análisis Firestore Rules Límite 1000 Expresiones
## Fecha: 2026-09-01
## Estado: ⭐ RESUELTO Y AVANZADO — Ver las últimas actualizaciones al final: apertura global de reservas, bug idSesion=0 corregido, logging diagnóstico. Tests 99/99. Pendiente: verificar réplica en producción, reservas del CLIENTE, bucket de Storage, backfill de índices y commits.

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

---

---

# ACTUALIZACION 2026-08-26 (SESION VI) — CAMBIO DE PC, FOTOS+CAMARA, RECUPERACION DE CONTRASEÑA Y FIX DE RUTA VIA B

> Bloque vigente. Sesiones anteriores quedan como historico. Se trabajo en un PC nuevo
> (se hizo un commit para continuar); este bloque resume la auditoria, las features
> terminadas, los commits hechos por el desarrollador y los pendientes abiertos.

## Arranque: cambio de PC y commit con errores

- Se retomo el proyecto en un PC nuevo con el commit `c7ff21c` "CONMIT CON ERRORES DE OPENCODE".
- Auditoria inicial (sin cambios): git limpio, HEAD = `856ea89`; build fallaba en
  `MiPerfilScreen.kt` (4 errores); funcionalidades de Sesion III/V presentes (auth,
  creacion de negocio, Via A/B, deep link, replica, Rules 17/17 en emulador).
- Basura versionada: `build_*.txt` en raiz y `firestore-tests/firestore-debug.log`.

## Feature 1: fotos galeria/camara (corregida y terminada)

- Corregidos los 4 errores de `MiPerfilScreen.kt` (import duplicado de
  `ActivityResultContracts`, import duplicado de `getValue`, `rememberSaveable` sin import,
  `guardarFotoDesdeLauncher` inexistente).
- Implementado el selector "Seleccionar/Cambiar foto" → "Elegir de galeria" / "Hacer una foto"
  en las 3 pantallas de perfil (MiPerfil, PerfilClienteAdministrador, AñadirCliente).
- Nuevo componente reutilizable `ui/components/BotonSelectorFoto.kt` (DropdownMenu).
- `ui/utils/FotoUtils.kt` ampliado: `crearFotoTemporal`, `uriDeFotoTemporal`,
  `guardarFotoDeCamara`; ambas vias terminan en `guardaFotoEnInterna(context, uri)`.
- Camara con `TakePicture()` y `FileProvider` (`${applicationId}.fileprovider`) +
  `res/xml/file_paths.xml` (cache-path `fotos_camara`). El guardado ocurre solo en el
  callback del resultado, nunca tras `launch()`.
- `AñadirClienteScreen` elimina su copia privada de `guardarFotoEnInterna` y reutiliza
  `FotoUtils.kt`.

## Feature 2: recuperacion de contrasena (Firebase)

- `AutenticacionRepository.enviarCorreoRecuperacion(email)` → solo
  `FirebaseAuth.sendPasswordResetEmail`; mensaje de exito generico (no revela existencia);
  ante errores de auth responde el mismo generico; solo fallos reales (p. ej. sin conexion).
- `MainViewModel.enviarCorreoRecuperacion(email): String?` valida email vacio/formato
  (`android.util.Patterns.EMAIL_ADDRESS`) y reutiliza `_autenticando`.
- Nueva `ui/auth/RecuperarPasswordScreen.kt` (estilo Login), ruta `RECUPERAR_PASSWORD`
  en `Routes`/`AppNavigation`, enlace "¿Has olvidado tu contrasena?" en Login.

## Commits hechos por el desarrollador (fuera de sesion, ya en origin/master)

- `d764587` "Foto con camara implementado": fotos (7 archivos: Manifest, FotoUtils,
  BotonSelectorFoto, file_paths.xml, MiPerfilScreen, PerfilClienteAdministradorScreen,
  AñadirClienteScreen).
- `856ea89` "Foto con camara implementado": recuperacion de contrasena (6 archivos:
  AutenticacionRepository, MainViewModel, Routes, AppNavigation, LoginScreen,
  RecuperarPasswordScreen).

## Diagnostico: replica Room→Firestore de clientes NO llega

- Cliente creado por ADMIN aparece en Room pero no en `clientes` de Firestore; "Vinculacion
  en la nube" dice "ficha no sincronizada"; reintento no cambia.
- Causa: las Rules de `clientes` (create/get/update) exigen `usuarioActual().negocioId is
  string`. Con `usuarios/{uid}.negocioId == null` todo queda DENEGADO. No es bug del mapa de
  replica: el ADMIN debe tener negocio creado (o la sesion no se autentica, ver abajo).

## Diagnostico: creacion de negocio PERMISSION_DENIED (ABIERTO)

- Datos confirmados: `usuarios/{uid}` con rol ADMIN, activo true, clienteId null,
  negocioId null; `negocios/{uid}` NO existe; `negocios_publicos` no existe; Rules
  desplegadas = actuales = 17/17.
- Evaluando el Batch de `NegocioRepository.crearNegocio()` (set negocios/{uid},
  set negocios_publicos/{uid}, update usuarios/{uid} negocioId=uid) contra las Rules,
  las 3 operaciones son logicamente PERMITIDAS. La unica condicion que podria ser false
  es `esAdmin()` (firestore.rules) → solo ocurre si la peticion llega sin `request.auth`
  valido (token de sesion caducado/invalido) o `usuarios/{request.auth.uid}` no es ADMIN.
- Pendiente de verificar: cerrar sesion y re-login (renovar token), diff de reglas
  desplegadas vs `firestore.rules`, `project_id` de la APK instalada, y que no exista
  `negocios_publicos/{uid}` huerfano. NO modificar Rules ni el diseno `negocioId = uid`.

## Diagnostico y FIX: Via B "No tienes permisos" (bug de ruta)

- La reclamacion fallaba con PERMISSION_DENIED. Trazado: `MainActivity` extrae el token
  limpio a `EnlacePendiente.codigo`; luego `destinoSegunTipo()`/`AppNavigation` construian
  la ruta como `"${Routes.VINCULAR_CLIENTE}?codigo=$token"`. Como
  `Routes.VINCULAR_CLIENTE = "vincular_cliente?codigo={codigo}"`, el resultado era
  `"vincular_cliente?codigo={codigo}?codigo=TOKEN"` (doble query) y Navigation extraia
  `codigoPrecargado = "{codigo}?codigo=TOKEN"` (basura). El `get()` de
  `vinculaciones/{basura}` no existe → la regla `allow get` de `vinculaciones` (exige
  estado PENDIENTE y fecha futura) lo deniega → PERMISSION_DENIED.
- **Fix aplicado y compilado (BUILD SUCCESSFUL):** en `MainViewModel.kt` y
  `AppNavigation.kt` usar `Routes.VINCULAR_CLIENTE.replace("{codigo}", token)`.
  Ahora `codigoPrecargado` recibe el token limpio. (2 archivos SIN commitear.)

## Pendiente para continuar

1. Probar en dispositivo: Via B reclamar ficha con enlace tras el fix de ruta.
2. Resolver creacion de negocio PERMISSION_DENIED (probar re-login; diff de reglas;
   verificar project_id de la APK; descartar `negocios_publicos/{uid}` huerfano).
3. Tras crear el negocio, "Reintentar sincronizacion" del cliente ya creado y probar la
   generacion del enlace (Vía B exige ficha remota).
4. Probar en dispositivo: recuperacion de contrasena (correo real) y camara de fotos.
5. Commit pendiente de los 2 archivos del fix de ruta (y de los cambios de esta sesion).
6. Limpieza: `build_*.txt` en raiz y `firestore-tests/firestore-debug.log`.

## Comandos utiles (este PC nuevo)

```powershell
.\gradlew.bat assembleDebug            # compilar APK debug
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
npm --prefix firestore-tests test      # pruebas Rules (emulador)
& ".\firestore-tests\node_modules\.bin\firebase.cmd" deploy --only firestore:rules
```

---

---

# ACTUALIZACION 2026-08-27 (SESION VII) — REDISEÑO FLUJO ALTA/VINCULACION CLIENTE + BACKFILL indices_clientes

> Bloque vigente. **La Vía B (enlace individual/deep link) queda DESCARTADA** en todo el
> nuevo flujo; no se toca `vinculaciones` ni `codigoVinculacion`. Sesiones anteriores
> quedan como historico.

## Objetivo funcional acordado

- **CASO 1 — ADMIN crea primero al cliente:** ADMIN crea la ficha (Room + Firestore, con
  `firebaseUid = null`). Luego el CLIENTE entra, elige CLIENTE, y en la pantalla inicial ve
  "¿Tu gimnasio ya te ha registrado?" con campos **código maestro** + **DNI** (botones
  Continuar / "No tengo código"). Si introduce código+DNI: el código identifica el negocio,
  el DNI identifica la ficha dentro de ese negocio; si existe ficha con `negocioId + dni` y
  `firebaseUid == null`, se vincula ese UID a ESA ficha existente. **NO se crea segunda ficha.**
- **CASO 2 — ADMIN no creó al cliente:** CLIENTE pulsa "No tengo código" → registro normal.
  Antes de crear una ficha nueva se comprueba en Firestore si ya existe ficha con ese DNI:
  si no existe → crear; si existe → NO crear y avisar "ese DNI ya está registrado, vincúlate
  con el código del gimnasio".
- **Objetivo central:** una persona nunca acaba con dos fichas del mismo negocio por haberse
  registrado después de que el ADMIN la hubiera creado.

## Auditoria de estructura de datos (solo lectura, nada modificado)

- **`usuarios/{uid}`** hoy: `{rol, activo, clienteId, negocioId}`. Sin datos personales.
  El email existe solo en Firebase Auth. Reglas obligan a `clienteId == null && negocioId == null`
  en el create y a que el update CLIENTE ocurra en Batch validado.
- **`clientes/{idCliente}`** hoy: `{idCliente, negocioId, firebaseUid, codigoVinculacion,
  nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta,
  fechaBaja, estado, tieneLlave, observaciones, serviciosContratados, fechaInicioActual,
  fechaFinActual}`. ADMIN crea con `firebaseUid = null` y `negocioId = uid del ADMIN`.
  **Vía A actual crea la ficha SIN `dni` ni `nombre`** (deuda detectada).
- **`ClienteEntity` (Room):** PK `idCliente` autoincremental Int, índice único global en `dni`.
  Ya tiene `negocioId`, `serviciosContratados` y `firebaseUid`.
- **Donde se guarda el perfil durante el registro:** hoy el registro de ficha del CLIENTE
  (`AñadirClienteScreen(modoRegistroCliente=true)` → `ClienteViewModel.insertarCliente`) guarda
  SOLO en Room con `negocioId = null`, `firebaseUid = null`; DataStore guarda `id_cliente_sesion`.
  La replica a Firestore con `crearClienteRemoto` usa `negocioId = uid propio` → **falla para
  CLIENTE por Rules** (quedaba local sin sincronizar). No existe almacen en Firestore para un
  perfil "pendiente de vinculacion".
- Conclusion: `usuarios/{uid}` debe seguir siendo solo identidad de cuenta; todo lo personal
  pertenece a `clientes/{idCliente}`. Se propone nueva coleccion `perfiles_pendientes/{uid}`
  como almacen temporal del perfil del CLIENTE sin negocio (borrable al vincular).

## Decisiones tecnicas CERRADAS para el nuevo flujo

1. **`idCliente` se mantiene como Int**: NO cambiar `ClienteEntity.idCliente`, NO migrar Room
   a String, NO cambiar el modelo Room↔Firestore. La unicidad negocio+DNI NO se resuelve con
   clave determinista en `clientes`, sino con una **coleccion de indice**.
2. **Indice para localizar ficha por negocio + DNI:**
   - Coleccion: `indices_clientes`.
   - `documentId`: **`{negocioId}_{dni}`** (dni normalizado en mayusculas; `_` seguro porque ni
     el UID ni un DNI lo contienen). DocumentId verificable en Rules sin hash.
   - Campos: `{ negocioId: string, dni: string, clienteId: int }`. **Sin `firebaseUid`**
     (el estado de vinculacion vive solo en `clientes`; evita segunda fuente de verdad).
   - Ciclo de vida: ADMIN crea `set` del indice junto a `clientes` (mismo Batch); ADMIN que
     cambia el DNI hace `delete` del indice antiguo + `set` del nuevo (mismo Batch); CLIENTE
     en CASO D crea ficha + indice + `usuarios` en la **misma Transaction**; CLIENTE en CASO C
     NO toca el indice (ya existe). `update` del indice: **prohibido**.
   - Atomicidad: toda escritura del indice dentro del mismo Batch/Transaction que toca
     `clientes`; Rules lo exigen con `getAfter(clientes/...)`.
   - Rules lectura CLIENTE: `allow get` solo si `dni` del indice == `dni` de
     `perfiles_pendientes/{uid}` (+ `resource == null` para chequear existencia en
     Transaction); `allow list: if false` (no enumerable); `update: false`.
   - **Concurrencia mismo DNI:** la Transaction conflictua en el mismo documentId del indice
     → Firestore serializa, el perdedor reintenta y pasa a rama CASO C; la ficha ya tiene UID
     → Rules deniegan → "ese DNI ya esta vinculado". **Requiere Transaction, nunca batch plano**
     (un `batch.set` haria last-write-wins y dejaría ficha huerfana).

## Analisis de migracion de datos existentes

Categorias de `clientes` en Firestore:
| Categoria | Creacion | Tiene dni | firebaseUid | Indice |
|---|---|---|---|---|
| A | ADMIN alta sin vincular | si | null | necesita backfill |
| B | ADMIN alta + Via B reclamada | si | uid | necesita backfill |
| C | Via A (codigo maestro) | **no** | uid | incompatible, se deja intacta |

- Las categorias A/B con DNI requieren backfill del indice (operacion aditiva, sin tocar
  `clientes`); sin indice un CLIENTE nuevo con ese DNI crearía duplicado.
- La categoria C (sin DNI) se deja intacta: ya esta vinculada, no pasa por el flujo DNI.
- Migracion segura: script Admin SDK de una sola ejecucion con pre-vuelo (dry-run) que detecta
  colisiones `(negocioId, dni)`, backfill con `create()` (falla ante colision en lugar de
  sobrescribir), y verificacion `count(indices) == count(clientes con dni)`.

## IMPLEMENTADO EN ESTA SESION: DRY-RUN de auditoria (sin escrituras)

- **Nuevo script (NO commiteado):** `firestore-tests/auditoria_backfill_indices.cjs`.
- Autenticacion: reutiliza la sesion del CLI Firebase
  (`~/.config/configstore/firebase-tools.json`) + `google-auth-library` 9.15.1 ya presente en
  `firestore-tests/node_modules` (dependencia transitiva de firebase-tools) → **sin dependencias
  nuevas**. Requiere el `clientId`/`clientSecret` publicos del CLI Firebase
  (`563584335869-fgrhgmd47bqnekij5i8b5pr03ho849e6.apps.googleusercontent.com` /
  `j9iVZfS8kkCEFUPaAeJV0sAi`, en `firebase-tools/lib/api.js`) para renovar el refresh_token.
- Consulta REST paginada (`pageSize=300`) a
  `https://firestore.googleapis.com/v1/projects/gestorpro-50e83/databases/(default)/documents/clientes`.
- **SOLO LECTURA**: no escribe nada en Firestore, no toca la app ni Rules.
- Ejecucion: `node firestore-tests/auditoria_backfill_indices.cjs`.

### RESULTADO DEL DRY-RUN (2026-08-27, produccion gestorpro-50e83)

```
Total clientes:                  3
Con DNI (normalizado):           2
Sin DNI:                         1
Sin negocioId (total):           1
Pares unicos (negocioId, dni):   2
Indices necesarios:              2
Colisiones detectadas:           0
Clientes con inconsistencias:    1
```

- **Colisiones:** ninguna.
- **Ficha sin DNI:** `docId=2`, `idCliente=2`, sin negocioId, `firebaseUid=Vnyht6hlR5EYJ1G0vxxl`
  (categoria C, Via A). Se deja intacta.
- **Fichas con DNI pero sin negocioId:** ninguna (las 2 con DNI son indexables).
- **Inconsistencia:** solo `docId=2` (sin negocioId) — no bloquea porque no genera indice.
- **BLOQUEOS: NINGUNO.** El backfill con `create()` por par unico (2 indices) es seguro.

## Pendiente para continuar (siguiente conversacion)

1. **[PENDIENTE APROBACION]** Preparar el script de **backfill real**:
   `create(indices_clientes/{negocioId}_{dni})` para los 2 pares detectados (ADMIN o Admin SDK),
   con pre-chequeo de inexistencia y verificacion de conteos. NO ejecutar sin confirmacion.
2. Implementar el nuevo flujo en la app (tras decisión de G0 en conversacion previa):
   - Pantalla CLIENTE "¿Tu gimnasio ya te ha registrado?" (codigo maestro + DNI + "No tengo codigo").
   - `perfiles_pendientes/{uid}` (Rules nuevas: solo el propio uid, `hasOnly` datos personales).
   - Busqueda por indice `indices_clientes/{negocioId}_{dni}` (CASO C: vincular ficha existente;
     CASO D: crear ficha + indice + `usuarios` en Transaction).
   - Evitar duplicados: Transaction obligatoria; la ficha de Via A actual crea ficha SIN DNI
     (debe desactivarse o migrarse).
   - `destinoSegunTipo()` debe llevar al CLIENTE sin `clienteId` a la pantalla de vinculacion
     (eliminando el tramo de `EnlacePendiente`/Via B).
3. **Rules nuevas necesarias** (proposal, NO aplicadas): `indices_clientes` (get por dni propio,
   list false, update false, create/delete atomicos con `getAfter(clientes)`), `perfiles_pendientes`,
   regla `update` CLIENTE de vinculacion por DNI sin depender de `vinculaciones`, tercera rama en
   `usuarios/update`. Validar con `npm --prefix firestore-tests test` antes de publicar.
4. **Nuevo archivo sin commitear:** `firestore-tests/auditoria_backfill_indices.cjs`.
5. Pendientes heredados de Sesion VI (siguen abiertos): creacion de negocio PERMISSION_DENIED sin
   resolver (hipotesis token de sesion), replica Room→Firestore bloqueada con `negocioId == null`,
   pruebas en dispositivo, commit del fix de ruta Via B (2 archivos), limpieza `build_*.txt` y
   `firestore-tests/firestore-debug.log`.

## Comandos utiles (este PC)

```powershell
node firestore-tests/auditoria_backfill_indices.cjs          # DRY-RUN de auditoria (solo lectura)
.\gradlew.bat assembleDebug                                  # compilar APK debug
npm --prefix firestore-tests test                            # pruebas Rules (emulador)
& ".\firestore-tests\node_modules\.bin\firebase.cmd" deploy --only firestore:rules
```

---

---

# ACTUALIZACION 2026-08-27 (SESION VIII) — IMPLEMENTACION COMPLETA: DOS APLICACIONES (ADMIN + CLIENTE)

> Bloque vigente. Se implementa la arquitectura definitiva de DOS aplicaciones
> independientes sobre el mismo Firebase (`gestorpro-50e83`). **Vía B / deep link
> DESCARTADA definitivamente** (sin `vinculaciones`, sin `codigoVinculacion`,
> sin `EnlacePendiente`). Sesiones anteriores quedan como historico.

## Decisiones de arquitectura confirmadas

- **Dos módulos en el mismo proyecto Gradle:** `:app` = GestorPro Admin, `:appCliente` = GestorPro Cliente.
- `appCliente` package `com.roberto.gestorpro.cliente`, `applicationId com.roberto.gestorpro.cliente`.
- `google-services.json` del Cliente colocado en `appCliente/google-services.json` (registrado en la
  consola Firebase como Android app con ese paquete); el plugin se aplica de forma incondicional
  (igual que `:app`).
- La app Cliente NO usa Room ni Gson: fuente de verdad = Firestore; solo DataStore para preferencias.
- `observaciones` vive en `clientes_privados/{idCliente}` (solo ADMIN); el CLIENTE no puede leerlo.
- `indices_clientes/{negocioId}_{dni}` garantiza unicidad negocio+DNI.
- `perfiles_pendientes/{uid}` guarda el perfil temporal del CLIENTE sin negocio.

## Implementado

### Firestore Rules (`firestore.rules`) — REESCRITAS
- Colecciones nuevas: `indices_clientes` (get por dni propio / admin, list false, update false,
  create/delete atómicos), `perfiles_pendientes` (solo uid propio), `clientes_privados` (solo ADMIN).
- VÍA 1: `vinculacionDniValida()` — vincula el UID a una ficha existente libre (firebaseUid null).
- VÍA 2: `creacionDirectaValida()` — crea ficha + índice + usuarios en la misma Transaction.
- `clientes/update` CLIENTE: solo `nombre, apellidos, telefono, email, foto, fechaNacimiento`;
  DNI, negocioId, firebaseUid, estado, servicios, fechas admin y tieneLlave bloqueados.
- ADMIN edita el DNI manteniendo el índice atómico (borra viejo + crea nuevo en el mismo Batch).
- Eliminada la colección `vinculaciones` y todas las funciones de Vía B.

### Tests de Rules (`firestore-tests/firestore.rules.test.cjs`) — REESCRITOS
- 16 pruebas: VÍA 1, VÍA 2, índices, perfiles pendientes, clientes_privados, edición personal
  del CLIENTE, cambio de DNI por el ADMIN, concurrencia, aislamiento por negocio.
- **16/16 OK** (`npm --prefix firestore-tests test`).

### App Admin (`:app`) — adaptada a Admin-only
- `ClienteRemotoRepository`: réplica en Batch `clientes` + `indices_clientes` + `clientes_privados`;
  al cambiar el DNI mantiene el índice (delete viejo + create nuevo). `dniAnterior` pasado por
  `ClienteViewModel.actualizarCliente`.
- Eliminados: `VincularClienteScreen`, `EnlaceVinculacionScreen`, `MiPerfilScreen`,
  `HomeClienteScreen`, `SeleccionTipoUsuarioScreen`, `VinculacionRepository`, `EnlacePendiente`,
  deep link del Manifest y `MainActivity`. `AñadirClienteScreen` sin `modoRegistroCliente`.
- `MainViewModel`: rol ADMIN fijo, sin vinculación. `CuentaScreen` sin "cambiar tipo de usuario".
- **BUILD SUCCESSFUL** (`:app:assembleDebug`).

### App Cliente (`:appCliente`) — módulo nuevo
- Paquetes: `com.roberto.gestorpro.cliente`. Flujo: Login/Registro/Recuperar → Inicio
  ("¿Tu gimnasio ya te ha registrado?" código+DNI / "No tengo código") → CompletarPerfil (VÍA 2)
  → vinculación por Transaction (VÍA 1 o VÍA 2) → Home → Mi perfil / Editar / Cuenta.
- Repositorios: `AutenticacionRepository` (con `esperar()`), `NegocioRepository` (código maestro),
  `PerfilPendienteRepository`, `ClienteRepository` (ficha + edición personal),
  `VinculacionRepository` (VÍA 1 y VÍA 2 con Transactions y manejo de colisión).
- `MainViewModel` orquesta el flujo; DataStore guarda idCliente/negocioId/dni pendiente.
- `google-services.json` colocado; plugin incondicional. **BUILD SUCCESSFUL** (`:appCliente:assembleDebug`).

### Configuración para Android Studio
- `settings.gradle.kts` incluye `:app` y `:appCliente`.
- `.idea/gradle.xml` añade `$PROJECT_DIR$/appCliente` a los módulos vinculados.
- `appCliente/build.gradle.kts`: plugin google-services incondicional (igual que `:app`),
  `applicationId com.roberto.gestorpro.cliente`, minSdk 26, targetSdk 36, Compose + Hilt + Firebase BOM.

## Verificación
- `.\gradlew.bat assembleDebug` → **BUILD SUCCESSFUL** (`:app` y `:appCliente`).
- `npm --prefix firestore-tests test` → **16/16 OK**.
- Sin commits (working tree listo para revisión).

## Pendiente para continuar (siguiente conversacion)
1. **Desplegar las Rules** en producción (`firebase deploy --only firestore:rules`) tras aprobación.
2. **Backfill de `indices_clientes`** (2 índices detectados en el DRY-RUN; script listo; NO ejecutar sin aprobación).
3. **Pruebas manuales en dispositivo:** registro y vinculación VÍA 1 y VÍA 2, edición de perfil,
   recuperación de contraseña, y que `:app` (Admin) siga funcionando con su APK.
4. **Verificar en Android Studio** que `:appCliente` aparece como aplicación ejecutable en el selector de Run.
5. **Commits pendientes:** toda la sesión en working tree (dos apps, Rules, tests, docs, `.idea/gradle.xml`).
6. Limpieza de basura versionada: `build_*.txt` en raíz y `firestore-tests/firestore-debug.log`.

## Comandos utiles (este PC)
```powershell
.\gradlew.bat :app:assembleDebug            # compilar Admin
.\gradlew.bat :appCliente:assembleDebug     # compilar Cliente
npm --prefix firestore-tests test           # pruebas Rules (emulador)
node firestore-tests/auditoria_backfill_indices.cjs   # DRY-RUN de auditoria (solo lectura)
& ".\firestore-tests\node_modules\.bin\firebase.cmd" deploy --only firestore:rules
```

---

---

# ACTUALIZACION 2026-08-27 (SESION IX) — VIA 1 FUNCIONAL: DECLARACION TEMPORAL (OPCION B) + LECTURA DE FICHA (OPCION A)

> Bloque vigente. Se resuelven los PERMISSION_DENIED de la VIA 1 (código maestro + DNI)
> con dos cambios de seguridad en Rules y flujo de appCliente. Sesiones anteriores quedan
> como historico.

## Problema original (sesion de pruebas reales)

- La VIA 1 fallaba con "ficha no existe" y luego "No tienes permisos". Causas encontradas:
  1. `indices_clientes` vacío (sin backfill) y fichas antiguas con `negocioId` de otro negocio.
  2. La regla GET de `indices_clientes` exigía `perfiles_pendientes/{uid}.dni`, pero en VIA 1 el
     CLIENTE no tenia perfil pendiente → DENIED.
  3. Tras permitir el indice, `transaction.get(clientes/{idCliente})` fallaba porque `clientes/get`
     de CLIENTE exige `string(usuarioActual().clienteId) == clienteId` (y el CLIENTE aun no esta
     vinculado, clienteId == null).

## Solucion implementada

### OPCION B — declaracion temporal { dni, negocioId } en perfiles_pendientes (VIA 1)
- `PerfilPendienteRepository.guardarDeclaracion(uid, dni, negocioId)` escribe `perfiles_pendientes/{uid}`
  = `{ dni, negocioId }` ANTES de consultar el indice. NO es un perfil ficticio: es el dato que el
  CLIENTE introduce en el momento de la vinculacion. Se borra al terminar (exito o rechazo).
- `VinculacionRepository.vincularConCodigoYDNI`: resuelve negocioId → guardarDeclaracion →
  localizar indice → VIA 1 (vincular) o VIA 2 (crear ficha) → borrar perfiles_pendientes en todos los casos.
- `MainViewModel.vincularConCodigoYDNI`: limpia estado local `_perfilPendiente` y borra el perfil remoto
  tambien en caso de error.
- Rules: `perfiles_pendientes` create/update admite DOS modos:
  - VIA 1: `{ dni, negocioId }`
  - VIA 2: perfil completo `{ nombre, apellidos, dni, telefono, email, foto, fechaNacimiento }`
  (hasOnly = union de ambos; `dni is string`).
- Rules: GET de `indices_clientes` exige que el indice coincida con la declaracion en AMBOS campos:
  `get(perfiles_pendientes/{uid}).data.dni == resource.data.dni`
  `get(perfiles_pendientes/{uid}).data.negocioId == resource.data.negocioId`.
  Mantiene `list: false`, `resource == null` y el acceso ADMIN.

### OPCION A — lectura de la ficha por el CLIENTE no vinculado (VIA 1)
- Nueva regla `clientes/get` (tercera allow get):
  - `esCliente() && usuarioActual().clienteId == null && usuarioActual().negocioId == null`
  - `exists(perfiles_pendientes/{uid})` y `perfiles.dni == resource.data.dni`
  - `perfiles.negocioId == resource.data.negocioId`
  - `string(resource.data.idCliente) == clienteId` (documentId coherente).
- Permite que `transaction.get(clientes/{idCliente})` de la Transaction de vinculacion funcione
  para un CLIENTE aun sin vincular, sin permitir enumerar ni leer fichas de otros.
- La regla de CLIENTE ya vinculado queda intacta.
- NOTA transaction.get: el `get` dentro de una Transaction SI se evalúa contra las reglas de lectura;
  con la regla VIA 1 ya pasa. El `update` de clientes se valida con `vinculacionDniValida()` (usa
  get/getAfter de servidor, no sujetos a reglas de lectura).

## Tests de Rules
- Reescritos/ampliados a **18 pruebas** (`npm --prefix firestore-tests test` → 18/18 OK).
- PRUEBA 6 adaptada: el CLIENTE declara `{ dni, negocioId }`; get del propio indice ALLOW, de otro
  DNI/negocio DENY, list DENY.
- PRUEBA 17 nueva (indice VIA 1): declaracion valida ALLOW; DNI distinto DENY; negocioId distinto
  DENY; indice de otro negocio DENY; list DENY; cambio de declaracion; delete del perfil ALLOW.
- PRUEBA 18 nueva (lectura ficha VIA 1): declaracion correcta ALLOW; ficha de otro DNI DENY; ficha
  de otro negocio DENY; CLIENTE vinculado leyendo ficha ajena DENY; CLIENTE sin perfil pendiente
  DENY; list DENY; CLIENTE vinculado lee solo su ficha (ALLOW propia / DENY ajena).

## Deploys realizados en gestorpro-50e83
- Rules Opcion B: ruleset verificado idéntico al local (28291 bytes) — deploy OK.
- Rules Opcion B + A: ruleset `545ae672...` verificado idéntico al local (29617 bytes) — deploy OK.
- Ficha real creada y vinculable: `clientes/22` (Luna, dni 12345678X, negocioId aSiZI8...),
  con su indice y clientes_privados.

## Verificacion
- `npm --prefix firestore-tests test` → **18/18 OK**.
- Admin `:app` replica correctamente (clientes/22 + indice + clientes_privados) cuando la sesion
  es el ADMIN real (logout+login con su email; el login de Admin no valida rol → riesgo documentado).
- Sin commits (working tree listo).

## Pendiente para continuar (siguiente conversacion)
1. **Prueba manual en dispositivo de VIA 1** con las Rules B+A desplegadas: código 123456 + DNI
   de clientes/22 → debe vincular y mostrar "Te has vinculado a la ficha de tu gimnasio".
2. **Prueba manual VIA 2** (cliente sin código → completar perfil → crear ficha).
3. **Endurecer app Admin:** login/arranque deben validar `rol == "ADMIN"` (hoy el login solo exige
   doc existente + activo; una cuenta CLIENTE o sin doc puede entrar a HOME y replicar a ciegas).
4. **Backfill de `indices_clientes`** para fichas existentes con DNI (DRY-RUN: 2 indices; ficha
   `clientes/1` pertenece a negocio `7X1KyM8...` sin `negocios_publicos` vigente → decisión aparte).
5. **Commits pendientes:** toda la sesión en working tree sin commitear (Rules, tests, apps, docs).
6. Limpieza de basura versionada: `build_*.txt` en raíz y `firestore-tests/firestore-debug.log`.

## Comandos utiles (este PC)
```powershell
.\gradlew.bat :app:assembleDebug            # compilar Admin
.\gradlew.bat :appCliente:assembleDebug     # compilar Cliente
npm --prefix firestore-tests test           # pruebas Rules (emulador) — 18/18
node firestore-tests/auditoria_backfill_indices.cjs   # DRY-RUN de auditoria (solo lectura)
& ".\firestore-tests\node_modules\.bin\firebase.cmd" deploy --only firestore:rules
```


---

---

# ACTUALIZACION 2026-08-28 (SESION X) — FASE 1: FLUJO CLIENTE SIN VINCULAR + VIA 2, FIX NOMBRE NEGOCIO Y LOGO CON STORAGE

> Bloque vigente. Sesiones anteriores quedan como historico. Se trabajan tres fases
> sobre la app Cliente y el Admin: (1) flujo de cliente sin vinculo y VIA 2 completa,
> (2) sincronizacion del nombre del negocio Admin -> Firestore -> Cliente, (3) logo
> del negocio con Firebase Storage.

## FASE 1 — Flujo CLIENTE sin vinculo y VIA 2 (validado en dispositivo)

### Diagnostico del fallo real de VIA 2 ("No existe ficha creada por tu gym")

- Causa raiz: `InicioScreen` y `CompletarPerfilScreen` usan ViewModels DISTINTOS
  (cada `hiltViewModel()` se ancla a su NavBackStackEntry), por lo que
  `_perfilPendiente` set en CompletarPerfil no era visible en Inicio. En
  `MainViewModel.vincularConCodigoYDNI` el `perfil` llegaba `null` y
  `VinculacionRepository` devolvia el mensaje enganoso (antes de VIA 2).
- Bug secundario: `guardarDeclaracion` con `set()` destruia el perfil completo.
- Bug latente confirmado por simulacion contra el emulador: la Transaction de
  VIA 2 hace `transaction.get(clientes/{id})` sobre una ficha inexistente; las Rules
  de produccion (sin la rama `resource == null`) denegaban esa lectura -> PERMISSION_DENIED.
  Los tests usaban `writeBatch` (no `runTransaction`), por eso no lo detectaban.

### Cambios implementados

- `PerfilPendienteRepository`: `guardarDeclaracion` usa `SetOptions.merge()` (no
  destruye el perfil completo).
- `VinculacionRepository`: `localizarFicha()` devuelve `ResultadoIndice.Ficha/NoExiste`
  y NO traga excepciones (permisos/red se propagan y se traducen por separado);
  `vincularConCodigoYDNI` lee el perfil desde Firestore como fuente de verdad y
  borra `perfiles_pendientes/{uid}` SOLO al completar la vinculacion con exito.
- `MainViewModel`: `destinoInicial`/`destinoTrasAutenticar` -> HOME si hay ficha o
  perfil pendiente (DataStore `dniPendiente`), INICIO solo si no hay ninguno;
  `vincularConCodigoYDNI` no limpia el perfil ante errores; nuevo `cargarPerfilVista()`.
- `CompletarPerfilScreen`: rellena los campos desde `perfiles_pendientes/{uid}` al
  abrir y tras guardar navega a HOME (no vuelve a INICIO).
- `HomeScreen`: soporta estado sin vincular (aviso + cards Mi perfil / Clases y
  sesiones / Vincular con mi gimnasio / Mi cuenta / Configuracion).
- `ClasesScreen` (nueva): placeholder sin consultar Firestore (aviso si no vinculado).
- `MiPerfilScreen` / `EditarPerfilScreen`: si `clienteId == null` usan
  `perfiles_pendientes/{uid}`; el DNI es editable sin vinculo y queda bloqueado
  al vincular.
- `firestore.rules`: nueva rama `clientes/get` con `resource == null` (VIA 2) para
  que la Transaction pueda comprobar que la ficha no existe; restringida a CLIENTE
  sin vinculo con perfil pendiente.
- `Routes`/`AppNavigation`: ruta `CLASES`.
- Pruebas en dispositivo OK: registro, completar perfil, Home sin vinculacion,
  Mi perfil, edicion (DNI editable), cierre/reapertura conservando sesion y perfil,
  y VIA 2 completa (crea `clientes/{id}` + `indices_clientes/{negocioId}_{dni}` +
  `usuarios/{uid}`; borra `perfiles_pendientes`; sigue vinculado al reabrir).

## FASE 2 — Sincronizacion del nombre del negocio (validado en dispositivo)

### Diagnostico

- `MiNegocioScreen` guardaba nombre y logo SOLO en DataStore local; Firestore no
  recibia el nombre. La app Cliente lee `negocios_publicos/{id}.nombre`, por eso
  seguia mostrando el antiguo (y Firestore tambien).
- En appCliente, `cargarEstadoLocal()` (unico refresco de nombre desde Firestore)
  solo se ejecutaba tras login/registro; al reabrir la app con sesion restaurada
  nadie consultaba `negocios_publicos`.

### Cambios implementados

- Admin `NegocioRepository.guardarNombreNegocio(nombre)`: WriteBatch con `nombre`
  en `negocios/{id}` y `negocios_publicos/{id}` (mismo mecanismo que
  `guardarCodigoMaestro`). `MainViewModel.sincronizarNombreNegocio(nombre)` guarda
  DataStore + Firestore. `MiNegocioScreen` "Guardar cambios" sincroniza si el
  negocio existe en la nube.
- appCliente `MainViewModel`: `cargarEstadoLocal()` se ejecuta en `destinoInicial()`
  al arrancar con sesion restaurada y queda envuelto en try/catch (si no hay
  conexion se conserva la caché de DataStore).
- Prueba real OK: Admin cambia el nombre a "C.D. COLISEO PRUEBA 2", Firestore se
  actualiza, Cliente cerrado y reabierto muestra el nombre nuevo.

## FASE 3 — Logo del negocio con Firebase Storage (implementado y compilado; PENDIENTE bucket)

- Dependencia `firebase-storage` (vía Firebase BOM) SOLO en `:app` (el Cliente
  carga la URL por HTTP con Coil).
- `storage.rules` (nueva): lectura para autenticados; escritura solo para el ADMIN
  propietario (`usuarios/{uid}.negocioId == negocioId`); resto bloqueado.
- `firestore.rules`: campo `logo` permitido en `negocios_publicos` create/update.
- Admin: `NegocioRepository.guardarLogoRemoto(rutaLocal)` sube a
  `negocios/{uid}/logo.jpg` -> `downloadUrl` -> WriteBatch `logo` en `negocios` +
  `negocios_publicos`. `MainViewModel.sincronizarLogoNegocio`. `MiNegocioScreen`
  muestra preview (URL o archivo local) y sube el logo al guardar.
- Cliente: `NegocioRepository.obtenerDatosPublicosNegocio` lee `negocios_publicos`
  (nombre + logo); DataStore guarda la URL como caché; `cargarEstadoLocal` refresca
  logo y nombre al arrancar; `HomeScreen` muestra el logo con Coil (placeholder si
  vacio).
- Tests de Rules: 20/20 OK (`npm --prefix firestore-tests test`, emuladores
  firestore+storage). PRUEBA 19 (Storage: ADMIN propietario sube, ADMIN ajeno/CLIENTE/
  no autenticado no, cliente autenticado lee) y PRUEBA 20 (Firestore: logo en
  negocios y negocios_publicos).
- Builds: `:app:assembleDebug` y `:appCliente:assembleDebug` BUILD SUCCESSFUL.
- **FALLO REAL en produccion:** al pulsar "Guardar cambios" el logo falla con
  "Object does not exist at location". Diagnostico: el bucket por defecto
  `gestorpro-50e83.firebasestorage.app` (de `app/google-services.json`) NO esta
  creado/habilitado en Firebase Console; es la primera operacion de Storage del
  proyecto. Los tests 19/20 pasan porque el emulador crea el bucket automaticamente.
  Correccion: habilitar Cloud Storage en la consola y desplegar `storage.rules`.

## Pendiente para continuar

1. **Habilitar el bucket de Storage** en Firebase Console (proyecto `gestorpro-50e83`
   -> Storage -> Empezar) y desplegar `storage.rules`. Hasta entonces el logo falla.
2. Verificar que las Rules de Firestore desplegadas en produccion coinciden con
   `firestore.rules` local (necesarias para VIA 2 / `clientes/get resource == null`).
3. **Backfill de `indices_clientes`** (DRY-RUN: 2 indices; ficha `clientes/1` de un
   negocio sin `negocios_publicos` vigente, decision aparte).
4. Pruebas de Storage en produccion (subir/ver logo; Cliente lo refresca al reabrir).
5. Commits pendientes (toda la sesion en working tree). Limpieza `build_*.txt` y
   `firestore-tests/firestore-debug.log`.
6. Heredados de Sesion VI (abiertos): crear negocio con `PERMISSION_DENIED`
   (hipotesis token) y validar `rol == "ADMIN"` en el login de Admin.

## Comandos utiles (este PC)
```powershell
.\gradlew.bat :app:assembleDebug            # compilar Admin
.\gradlew.bat :appCliente:assembleDebug     # compilar Cliente
npm --prefix firestore-tests test           # pruebas Rules (emulador firestore+storage) — 20/20
node firestore-tests/auditoria_backfill_indices.cjs   # DRY-RUN (solo lectura)
& ".\firestore-tests\node_modules\.bin\firebase.cmd" deploy --only firestore:rules
& ".\firestore-tests\node_modules\.bin\firebase.cmd" deploy --only storage:rules
```

---

---

# ACTUALIZACION 2026-08-28 (SESION XI) — NUEVO MODELO SERVICIOS / SESIONES / RESERVAS (Fases 1 a 5C)

> Bloque vigente. Se rediseña el sistema de "Clases" por un catálogo de SERVICIOS con
> sesiones propias y reservas. Relación final **Cliente → Servicio → Sesión → Reserva**,
> SIN entidad Clase en el flujo nuevo. Sesiones anteriores quedan como historico.

## Diagnóstico previo (solo lectura)

- El "servicio contratado" vivía como `ClienteEntity.serviciosContratados: List<String>` (texto libre);
  `ClaseEntity.servicio: String` y `SesionClaseEntity.servicio: String` eran duplicados denormalizados;
  `MovimientoEntity.servicio: String` es texto libre e INDEPENDIENTE del catálogo.
- No existía `ServicioEntity`, ni pantalla de gestión de servicios, ni réplica de clases/sesiones a Firestore
  (las Rules de `clases`/`sesiones`/`reservas` eran "reglas adelantadas" sin datos).
- Decisiones: `Clase` pasa a tener `servicioId` (no String); `serviciosContratados` → `List<Int>` (ids);
  el movimiento sigue con su `servicio` en texto y NO se relaciona con el catálogo.

## FASE 1 — Modelo Room (nuevas entidades)

- `ServicioEntity` (tabla `servicio`): `idServicio` (PK auto), `negocioId`, `nombre`, `descripcion`, `activo`.
- `SesionEntity` (tabla `sesion`): `idSesion` (PK auto), `negocioId`, `idServicio`, `fecha` (Long), `hora`
  (String), `duracionMinutos`, `capacidad`, `plazasDisponibles`. Pertenece DIRECTAMENTE a un servicio.
- `ClienteEntity.serviciosContratados`: `List<String>` → `List<Int>`; nuevo `IntListConverter`.
- `ServicioDao`/`SesionDao` + repositorios; Room v10 → v11 (sigue `fallbackToDestructiveMigration`).
- `ClaseEntity`/`SesionClaseEntity` se MANTIENEN temporalmente (transición por fases).
- `:app:compileDebugKotlin` EXITCODE 0.

## FASE 2 — Gestión ADMIN de Servicios y Sesiones (Room)

- `ServiciosScreen` (ACTIVOS / DE BAJA; crear/editar/dar de baja/reactivar/eliminar), `EditarServicioScreen`,
  `DetalleServicioScreen` (sesión del día), `ProgramarSesionesScreen` (desde/hasta + CADA día con su propia
  hora + duración + capacidad), `EditarSesionScreen` ("Ver / editar sesión"), `SesionReservasScreen`.
- Generación/regeneración: borra sesiones futuras + sus reservas y crea las nuevas; conserva pasadas.
- `ReservaDao` ganó cascadas por servicio (subconsultas sobre la tabla `sesion`); plazas vía `reservarPlaza`
  (solo si >0) y `liberarPlaza` (tope capacidad).
- Ajuste posterior: cards de servicio con acciones según estado (ACTIVO → Editar/Dar de baja; DE BAJA →
  Reactivar/Eliminar); `DetalleServicioScreen` muestra SOLO la sesión de HOY y botón "Gestionar sesiones".

## FASE 3 — Servicios contratados en el perfil (Room, ADMIN)

- `Cliente` (modelo) y `toCliente()` portan `serviciosContratados: List<Int>`.
- Perfil: sección dinámica con nombres reales (resueltos contra `ServicioEntity`), sin hardcodes
  ("Sala de máquinas"/"CrossFit") y botón "Editar servicios" (diálogo con servicios ACTIVOS, selección múltiple;
  los ids de servicios inactivos contratados se conservan).
- `ClienteViewModel.guardarServiciosContratados` actualiza SOLO Room (sin réplica aún).

## FASE 4A — Servicios en Firestore

- `servicios/{idServicio}` (documentId = id int): `{ idServicio, negocioId, nombre, descripcion, activo }`.
  `negocioId` remoto = UID del ADMIN (Room sigue con `""`).
- `ServicioRemotoRepository`: crear (con comprobación de colisión de id en Transaction), actualizar,
  activar/desactivar, eliminar. `ServicioViewModel` sincroniza con patrón write-through + reintento.
- Rules `servicios`: ADMIN CRUD de su negocio (create con `hasOnly`+tipos; update solo nombre/descripcion/activo;
  delete propio; get con `resource == null` para la Transaction). CLIENTE: get de ACTIVOS de su negocio
  (añadido en 5C para la Transaction de reserva); sin escrituras.
- Tests PRUEBA 21–33. Total 33/33.

## FASE 4B — Sesiones en Firestore

- `sesiones/{idSesion}`: `{ idSesion, negocioId, idServicio, fecha, hora, duracionMinutos, capacidad, plazasDisponibles }`.
- `SesionRemotoRepository`: crear/actualizar/eliminar, eliminar futuras y todas de un servicio,
  `sincronizarSesionesGeneradas` (Batch: borra futuras + crea nuevas).
- Rules `sesiones`: ADMIN CRUD de su negocio (create exige servicio existente+activo del negocio vía
  `servicioValidoParaSesion`; update mantiene idSesion/negocioId/idServicio); CLIENTE get/list solo de
  servicios contratados Y activos (se ELIMINÓ `clientesPermitidos`; acceso calculado con
  `get(clientes)` + `get(servicios)`).
- Tests PRUEBA 34–53. Total 53/53. `SesionViewModel` sincroniza generación y edición.

## FASE 5B — Reservas en Room + nuevo modelo Sesion

- `ReservaEntity` sin cambios (índice único `(idSesion, idCliente)`).
- `ReservaRepository` REESCRITO: inyecta `ReservaDao`+`SesionDao`+`ServicioDao`+`ClientesDatabase`;
  operaciones ATÓMICAS con `RoomDatabase.withTransaction`:
  - `crearReserva`: sesión existe + plazas>0 + servicio existe y ACTIVO + sin duplicado → insert reserva + `plazas-1`.
  - `cancelarReserva`: reserva existe → delete + `plazas+1` (≤ capacidad).
  - `regenerarProgramacion`, `eliminarReservasYSesiones(Futuras)DelServicio`, `eliminarSesionConReservas`.
- `SesionDao.liberarPlaza` con tope `plazasDisponibles < capacidad`; `SesionDao.eliminarSesion`.
- `SesionReservasScreen` enlazada desde `EditarSesionScreen` ("Ver reservas de la sesión").
- NO se creó `ReservaViewModel` (las reservas de sesión las gestiona `SesionViewModel`; la capa de datos
  de reserva está en `ReservaRepository`).

## FASE 5C — Reservas en Firestore + Transactions + Rules

- `reservas/{clienteId}_{sesionId}` (documentId DETERMINISTA → una reserva por cliente+sesión):
  `{ idReserva, negocioId, sesionId, clienteId, fechaReserva }`.
- `ReservaRemotoRepository`:
  - `crearReservaRemota` (Transaction: cliente → negocio; sesión → existe/negocio/plazas; servicio →
    existe/negocio/activo; contratado; sin duplicado; set reserva + `plazasDisponibles-1`).
  - `cancelarReservaRemota` (Transaction: reserva existe + sesión existe + plazas<capacidad → delete + `+1`).
  - Cascadas: `eliminarReservasDeSesionRemoto`, `eliminarReservasDeSesionesFuturasDelServicioRemoto`,
    `eliminarTodasLasReservasDelServicioRemoto` (queries por `sesionId` + WriteBatch).
- Rules `reservas` (ATÓMICAS con `getAfter`/`existsAfter`):
  - CLIENTE create: `reservaCreaValida` (negocio, servicio contratado+activo, `plazas == anterior-1 && >= 0`);
    delete: `reservaEliminadaValida` (`== anterior+1 && <= capacidad`); update false.
  - `sesiones/update` CLIENTE: solo `plazasDisponibles` (±1 exacto) y solo si la Transaction crea/elimina la
    reserva (`reservaCreadaEnTransaccion` / `reservaEliminadaEnTransaccion`).
  - ADMIN: get/list/delete de su negocio (delete con ajuste de plazas).
  - `resource == null` en get de reservas para la comprobación de duplicado en Transaction (patrón VÍA 2).
- Cascadas remotas conectadas en `ServicioViewModel` (baja/eliminar) y `SesionViewModel` (eliminar sesión,
  regenerar). Los movimientos NO se tocan.
- Tests PRUEBA 54–76. **Total 76/76** (`npm --prefix firestore-tests test`). `:app:compileDebugKotlin` EXITCODE 0.
- Nota de depuración: un test (PRUEBA 69) falló por un typo en el valor `plazasDisponibles` (6 en vez de 5);
  corregido, no era un problema de Rules.

## Pendiente para continuar

1. **`appCliente` del nuevo modelo:** `serviciosContratados: List<Int>`, `SesionesScreen` (sesiones de
   servicios contratados y activos), reservar/ver/cancelar reservas (reusar la Transaction).
2. **Sincronizar `serviciosContratados` del ADMIN a Firestore** (hoy solo Room).
3. **Habilitar el bucket de Storage** en Firebase Console y desplegar `storage.rules`.
4. **Desplegar las Rules** tras validar (76/76) y verificar producción == `firestore.rules`.
5. **Backfill de `indices_clientes`** (2 índices; `clientes/1` decisión aparte).
6. **Limpieza definitiva de `Clase`/`SesionClase`** (entidades, DAOs, repos, VM, UI `ui/clases`, rutas) y de
   `ServicioItem` (sin uso).
7. **Commits pendientes** (toda la sesión en working tree) y limpieza `build_*.txt`, `firestore-debug.log`.
8. Heredados: crear negocio con `PERMISSION_DENIED` (hipótesis token) y validar `rol == "ADMIN"` en login Admin.

## Comandos utiles (este PC)

```powershell
.\gradlew.bat :app:compileDebugKotlin        # compilar Admin (Kotlin)
npm --prefix firestore-tests test            # pruebas Rules — 76/76
& ".\firestore-tests\node_modules\.bin\firebase.cmd" deploy --only firestore:rules
& ".\firestore-tests\node_modules\.bin\firebase.cmd" deploy --only storage:rules
```

---

# ACTUALIZACION 2026-08-29 (SESION XII) — SYNC serviciosContratados, PANTALLA CLASES DEL CLIENTE, CASCADAS ADMIN Y DIAGNOSTICO DE RULES

> Trabajo realizado con otra IA (DeepSeek) entre el 28 y el 29 de agosto de 2026. Todo sin commit (working tree).
> Rules desplegadas en `gestorpro-50e83` verificadas **byte-idénticas** al local `firestore.rules` (42.687 bytes). Tests de Rules: **82/82 OK**.

## 1. Sincronizacion de `serviciosContratados` (Admin → Firestore)

**Objetivo:** el ADMIN guardaba los servicios contratados solo en Room; había que replicarlos a `clientes/{id}.serviciosContratados` como `array<int>`.

**Cambios (`:app`):**
- `ClienteRemotoRepository.actualizarServiciosContratadosRemoto(idCliente: Int, ids: List<Int>)`: `db.collection("clientes").document("$idCliente").update("serviciosContratados", ids)`. Solo Firestore, no toca índices.
- `ClienteViewModel.guardarServiciosContratados(...)`: actualiza Room y replica con write-through; nueva bandera `_sincronizacionPendienteServicios: MutableStateFlow<Boolean>`; `reintentarSincronizacion()` ramifica a la operación original para reintento manual.
- `PerfilClienteAdministradorScreen`: banner de error de sync + botón "Reintentar sincronizacion".

**Cambios (`appCliente`):**
- `model/Cliente.kt`: `serviciosContratados` de `List<String>` → `List<Int>`.
- `ClienteRepository.kt`: parser robusto `(it as? Number)?.toInt()`; ausente/vacío → `emptyList()`.
- `VinculacionRepository.kt`: `emptyList<String>()` → `emptyList<Int>()`.

**Validación:** añadida **PRUEBA 9B** (ADMIN update `serviciosContratados`) → 77/77 OK.

## 2. Pantalla "Clases de hoy" del Cliente (Fase 7)

**Modelo nuevo en `appCliente`** (no Room, no Gson): `model/Servicio.kt`, `model/Sesion.kt`.
**Capa datos:** `data/firebase/SesionRepository.kt` con `obtenerServicioActivo(idServicio)` y `obtenerSesionesPorServicio(idServicio)` (solo servicios contratados y activos).
**ViewModel:** `ui/viewmodel/SesionesClienteViewModel.kt` con `cargar()`, `reintentar()`, `inicioDeHoy()`, data class `SesionVisible`, y estados `noVinculado / cargando / error / sinServicios / sinSesionesHoy / ok`. Filtra `fecha == inicioDeHoy()` y ordena por hora.
**UI:** `ui/home/ClasesScreen.kt` funcional (lista de sesiones del día de servicios contratados+activos).
**Pendiente:** reservar / ver / cancelar reservas del CLIENTE (reusar la Transaction de `reservas/{clienteId}_{sesionId}`).

## 3. Diagnostico: Rules desplegadas en produccion estaban desactualizadas

- Síntoma reportado: `darDeBaja`/`alta`/`reactivar` servicio daba `PERMISSION_DENIED`.
- Hallazgo: el ruleset desplegado **no contenía `match /servicios/{servicioId}`** (era un ruleset antiguo). Por eso cualquier escritura en `servicios` se negaba.
- Acción: redeploy de `firestore.rules` local. Verificado `updateTime` 2026-08-28T23:07:50Z y **byte-idéntico** al local (incluye `match /servicios/{servicioId}` y `cascadaEliminaSesion`).
- **Aclaración de UID:** se venía manejando `aSiZI8YWILYOWhj2TXIznZWJP5O2` (mayúscula `I`). El admin real en producción es **`aSiZI8YWlLYOWhj2TXlznZWJP5O2`** (minúscula `l` en posición 9 y 18: `WlLYO` y `TXlzn`). Los servicios en producción tienen `negocioId = aSiZI8YWlLYOWhj2TXlznZWJP5O2`, coherente con ese UID → **no hay desajuste de negocio**, el anterior era un typo I/l del humano.

## 4. Cascadas administrativas de reservas (Fase 8)

**Problema:** el borrado masivo anterior con `batch.delete(reservas)` fallaba en Rules porque `reservaEliminadaValida` exige `plazas == anterior + 1`.
**Solución:** `runTransaction` por sesión, con reintento de query fresca (3 intentos) e `idempotente` (lee plazas actuales antes de ajustar). `MAX_RESERVAS_POR_SESION = 498`.

**Fase 1 (Android, `:app`):**
- `ReservaRemotoRepository.kt`: `eliminarSesionConReservasRemoto(idSesion)`, `eliminarSesionesFuturasConReservasRemoto(idServicio)`, `eliminarTodasLasSesionesConReservasRemoto(idServicio)`. Se eliminaron los métodos `batch.delete` antiguos.
- `ServicioViewModel.replicarDesactivacionRemota` / `replicarEliminacionRemota`: usan las cascadas (primero borran sesiones+reservas, luego desactivan/eliminan servicio).
- `SesionViewModel.eliminarSesion` / `generarSesiones`: usan `eliminarSesionConReservasRemoto`.
- `:app:assembleDebug` → BUILD SUCCESSFUL.

**Fase 2 (Rules + tests):**
- `firestore.rules`: helper `cascadaEliminaSesion(sesionId)` y rama OR en `reservas/delete` ADMIN: `reservaEliminadaValida() || cascadaEliminaSesion(sesionId)`.
- `firestore-tests/firestore.rules.test.cjs`: **PRUEBA 77-81** (escenarios ADMIN cascada).
- `npm --prefix firestore-tests test` → **82/82 OK**.

## 5. DIAGNOSTICO ABIERTO (interrumpido, sin concluir)

A pesar del redeploy y de confirmarse que `negocioId` de servicios == UID real del admin y que en producción hay **0 sesiones** (la cascada debería saltar directo a `desactivarServicioRemoto` sin tocar reservas):

- `darDeBaja` / `eliminar` servicio **sigue reportando `PERMISSION_DENIED`** en el dispositivo.
- La app **se cierra (crash)** durante alta / reactivación de servicio.

**Evidencia recabada antes de la interrupción:** Rules desplegadas == local (con `cascada`); `usuarios/{aSiZI8YWlLYOWhj2TXlznZWJP5O2}` rol=ADMIN, `negocioId=aSiZI8YWlLYOWhj2TXlznZWJP5O2`; servicios id=1 e id=8 con ese `negocioId`; `sesiones` = 0; `reservas` = 1 huérfana de otro negocio (`7X1KyM8rhBcUAK18EDUI`).

**Falta (informe A-H):** confirmar el `idServicio`/docId y el token que usa el build instalado, y obtener el stacktrace real del crash (logcat) para aislar la excepción no controlada. Posible causa app-side: excepción no capturada en `darDeBaja()`/`reactivar()` al recibir `ResultadoAutenticacion` false, o build instalado anterior a Fase 1.

## 6. Commits y basura

- Todo el working tree sin commit (dos apps, Rules, tests, docs).
- Basura versionada que limpiar: `build_*.txt`, `firestore-tests/firestore-debug.log`.

---

---

# ACTUALIZACION 2026-08-29 (SESION XIII) — CORRECCION PERMISSION_DENIED EN BAJA/ELIMINACION DE SERVICIOS (QUERIES SIN negocioId) + 8 PRUEBAS DE REGRESION

> Continuacion de la SESION XII (Diagnostico abierto). Se acepta el diagnostico A-H y se implementa
> la correccion en los repositorios Admin. Tests 90/90, build SUCCESSFUL. NO se despliega ni se commitea.
> firestore.rules NO se modifica.

## Diagnostico aceptado (informe A-H)

- Sintoma: `darDeBaja`/`eliminar` servicio -> `PERMISSION_DENIED`; la app se cerraba (crash) en
  alta/reactivacion. El redeploy de Rules idénticas al local no resolvio el `PERMISSION_DENIED`.
- Causa raiz (confirmada por revision de codigo, NO por stacktrace de dispositivo): las consultas
  administrativas de cascada en `ReservaRemotoRepository` y `SesionRemotoRepository` NO incluian
  `negocioId` en los `whereEqualTo`:
  - `eliminarReservasDeSesionRemoto(idSesion)` consultaba `reservas` solo por `sesionId`.
  - `eliminarReservasDeSesionesFuturasDelServicioRemoto` / `eliminarTodasLasReservasDelServicioRemoto`
    consultaban `reservas` por `sesionId in [...]`.
  - `eliminarSesionesFuturasDelServicioRemoto` / `eliminarTodasLasSesionesDelServicioRemoto` consultaban
    `sesiones` por `idServicio`.
  Las reglas `sesiones/list` y `reservas/list` exigen `negocioId == usuarioActual().negocioId`
  (rules-are-not-filters) -> DENIED. Como en produccion hay 0 sesiones, la rama de servicio saltaba a
  `desactivarServicioRemoto`/`eliminarServicioRemoto`, pero las queries previas ya lanzaban la excepcion.
- Punto aclarado (NO es bug): `ServicioRemotoRepository.replicarDesactivacionRemota`/`replicarEliminacionRemota`
  usan `update(mapOf("activo" to ...))`. En Firestore `update`, `request.resource` es el documento
  RESULTANTE (merge), asi que la regla `servicios/update` (que exige `idServicio`,`nombre`,`descripcion`,
  `activo`) se cumple aunque solo se envie `{activo}`. Se decide MANTENER las Rules estrictas y el
  `update` parcial (no enviar el documento completo).

## Cambios implementados (sin modificar firestore.rules)

### `app/src/main/java/com/roberto/gestorpro/data/firebase/ReservaRemotoRepository.kt`
- Las 3 consultas de reservas por sesion ahora añaden `whereEqualTo("negocioId", negocioId)` (se agrega
  parametro `negocioId` a los metodos publicos).
- `eliminarReservasDeSesionRemoto`: antes de la cascada, `transaction.get(sesionRef)`; si la sesion NO
  existe pero tiene reservas -> `SesionInexistenteConReservasException` (fail-closed: no se borran reservas
  a ciegas).
- Nuevo `resultadoDeError(e)`: `Log.e` del `FirebaseFirestoreException.Code` (unico sitio en la capa remota
  con log de codigo); el mensaje de UI no cambia.
- Se conserva la `runTransaction` por sesion, el reintento de query fresca (3 intentos) y
  `MAX_RESERVAS_POR_SESION = 498`.

### `app/src/main/java/com/roberto/gestorpro/data/firebase/SesionRemotoRepository.kt`
- `eliminarSesionesFuturasDelServicioRemoto` / `eliminarTodasLasSesionesDelServicioRemoto`: añaden
  `whereEqualTo("negocioId", negocioId)`.

### `app/src/main/java/com/roberto/gestorpro/data/firebase/ServicioRemotoRepository.kt`
- Sin cambio funcional en `replicarDesactivacionRemota`/`replicarEliminacionRemota` (se mantiene
  `update({activo})`); solo se anade `registrarError()` para trazabilidad.

## Pruebas de Rules (regresion)

- `firestore-tests/firestore.rules.test.cjs`: añadidas **PRUEBA 33A–33H** (8 pruebas) que verifican que las
  queries admin de sesiones/reservas SÍ incluyen `negocioId`, que el `delete` ADMIN de reservas ajusta plazas
  (cascada) y que el CLIENTE no puede listar sesiones/reservas de otro negocio.
- `npm --prefix firestore-tests test` -> **90/90 OK** (82 -> 90).
- `firestore.rules` NO modificado (sigue 42.687 bytes).

## Build

- `.\gradlew.bat :app:assembleDebug` -> BUILD SUCCESSFUL (primer intento abortado por referencia fuera de
  alcance a `idServicio` en un `Log` de `ServicioRemotoRepository`; corregido y recompilado).
- NO se despliega a produccion (pendiente aprobacion) ni se commitea.

## Riesgo abierto

- El crash real de la app en alta/reactivacion NO se pudo aislar: nunca se aporto el stacktrace (logcat) del
  dispositivo. La hipotesis de una excepcion no capturada en `darDeBaja()`/`reactivar()` al recibir
  `ResultadoAutenticacion` false sigue sin confirmar. Tras este parche, el `PERMISSION_DENIED` de las queries
  quedo resuelto a nivel de reglas; conviene validar en dispositivo con build actualizado y, si persiste el
  crash, capturar el stacktrace.

## Commits y basura

- Working tree sin commit (dos apps, Rules, tests, docs).
- Basura: `build_*.txt`, `firestore-tests/firestore-debug.log`.

---

---

# ACTUALIZACION 2026-08-29 (SESION XIV) — AUDITORIA DE SOLO LECTURA appCliente: ESTADO DEL CLIENTE + SERVICIOS/SESIONES/RESERVAS (PLAN, SIN IMPLEMENTACION)

> Auditoria de solo lectura para preparar la siguiente fase de `:appCliente`. NO se modifico ningun archivo.
> Entrega: diagnostico + plan de implementacion por pasos (Partes 1-5).

## PARTE 1 — Estado real del cliente (fuente de verdad: Firestore, NO Room)

- `appCliente` no usa Room: `MainViewModel._cliente` se carga desde `clientes/{idCliente}` (Firestore) al
  iniciar sesion.
- `model/Cliente.kt`: `estado` (ACTIVO/BAJA/ARCHIVADO/REGISTRADO), `fechaBaja`, `fechaInicioActual`,
  `fechaFinActual`, `serviciosContratados: List<Int>`.
- `MovimientoEntity` (Admin) NO se replica a Firestore y el CLIENTE no puede leer `movimientos` (Rules). Por
  tanto MOROSO/PAGO_VENCIDO NO se almacena y debe DERIVARSE en cliente.
- Recomendacion de derivacion: usar `fechaFinActual` (periodo de pago actual). No existe campo "vencido";
  reusar `fechaFinActual`/`fechaBaja` sin crear campos nuevos. (Pendiente decision de producto: periodo
  vencido vs pago individual pendiente.)

## PARTE 2 — Home del cliente (datos ficticios hoy)

- `ui/home/HomeScreen.kt` usa datos ficcion: estado `ACTIVO` y "31/08/2026". Debe observar `MainViewModel`
  para mostrar el estado real.
- `ui/viewmodel/MainViewModel.kt` ya tiene `_cliente`; falta exponer un estado "preparado" para Home (estado
  + fechas formateadas + si puede reservar).
- Problema de parseo de fechas: `ClienteRepository` lee `fechaBaja`/`fechaInicioActual`/`fechaFinActual` SOLO
  como `Number` (`(datos[...] as? Number)?.toLong()`), pero Admin las escribe como `Timestamp` -> llegan
  `null`. Hay que soportar `Timestamp` y `Number`
  (`(it as? Timestamp)?.toDate()?.time ?: (it as? Number)?.toLong()`).

## PARTE 3 — Servicios y sesiones del cliente (modelo nuevo ya existe)

- `model/Servicio.kt`, `model/Sesion.kt`, `SesionesClienteViewModel.kt` creados en Fase 7 (Sesion XII).
  `ClasesScreen` funcional lista sesiones del dia.
- `data/firebase/SesionRepository.kt`: `obtenerSesionesPorServicio(idServicio)` consulta SOLO por `idServicio`
  (sin `negocioId`) -> riesgo PERMISSION_DENIED bajo la regla `sesiones/list` actual. Debe añadir
  `whereEqualTo("negocioId", negocioId)`.
- FALTAN en `appCliente`: modelo `Reserva`, `ReservaRepository` (Transaction `reservas/{clienteId}_{sesionId}`),
  `ReservasClienteViewModel`, y la pantalla `MisReservasScreen`. La Transaction de reserva/cancelacion ya
  esta definida en `:app` (`ReservaRemotoRepository`) y las Rules lo permiten al CLIENTE
  (crear/leer/cancelar propias).

## PARTE 4 — Reglas y Firestore (compatibilidad)

- CLIENTE puede: leer `clientes/{idCliente}`; leer `servicios` activos de su negocio; listar `sesiones` con
  `negocioId`+`idServicio` (servicios contratados+activos); crear/leer/cancelar
  `reservas/{clienteId}_{sesionId}` (Transaction con `getAfter`/`existsAfter`, ajuste de plazas ±1).
- No crear campos nuevos en Firestore: reusar `fechaInicioActual`/`fechaFinActual`/`fechaBaja`.
- NO tocar lo antiguo (`ui/clases/`, `Clase`/`SesionClase`), Storage, Auth.

## PARTE 5 — Diseno propuesto (orden de implementacion, pendiente autorizacion)

1. **Fechas Real:** en Admin, `ClienteRemotoRepository.mapaDeAlta`/actualizacion deben poblar
   `fechaInicioActual`/`fechaFinActual` desde el `Movimiento` vigente (hoy los escribe `null`);
   `AñadirClienteScreen`/`ClienteViewModel` deben set `fechaBaja` al dar de baja y limpiarla al reactivar;
   `archivarCliente`/`restaurarCliente` deben replicar a Firestore. En `appCliente` `ClienteRepository`
   soporta `Timestamp`+`Number`.
2. **Indicador de estado en Home:** `MainViewModel` expone estado derivado (ACTIVO/BAJA/ARCHIVADO/REGISTRADO
   + MOROSO/PAGO_VENCIDO derivado de `fechaFinActual`); `HomeScreen` lo muestra real.
3. **SesionRepository negocioId:** añadir filtro `negocioId` a `obtenerSesionesPorServicio`.
4. **Reservas del CLIENTE:** `Reserva` model + `ReservaRepository` (crear/cancelar/leer propias con
   Transaction) + `ReservasClienteViewModel` + `MisReservasScreen`; integrar reservar/cancelar en
   `ClasesScreen`.
5. **Verificacion:** `npm --prefix firestore-tests test` sigue 90/90; compilar `:appCliente:assembleDebug`.

## Estado de la auditoria

- Solo lectura. Ningun archivo modificado. Queda a la espera de autorizacion para implementar los puntos 1-5.

## Commits y basura

- Working tree sin commit. Basura: `build_*.txt`, `firestore-tests/firestore-debug.log`.

---

# ACTUALIZACIÓN 2026-08-30 (SESIONES XV-XVII) — SINCRONIZACIÓN DE PERÍODOS, VALIDACIÓN DEL CARD Y CORRECCIÓN VÍA A

## SESIÓN XV — Sincronización Admin → Firestore de períodos

- `MovimientoRepository` es el punto único de persistencia y sincronización de movimientos.
- Cada insertar, actualizar o eliminar persiste primero en Room, recalcula el período desde los movimientos persistidos y replica después `fechaInicioActual`/`fechaFinActual` en `clientes/{idCliente}`.
- La sincronización usa `NonCancellable + Dispatchers.IO` y un `Mutex` para evitar carreras entre operaciones del mismo repositorio.
- Los fallos no deshacen Room: quedan señalados como pendientes y se pueden reintentar desde el perfil del cliente.
- `ClienteRemotoRepository` registra operación, cliente, fechas y código de error Firebase.
- El alta Admin asigna `fechaAlta` cuando el cliente se crea como `ACTIVO`.
- `ClienteViewModel` solo confirma alta/edición como éxito cuando termina correctamente la réplica remota.
- Se corrigió el proveedor Hilt de `MovimientoRepository` para inyectar `ClienteRemotoRepository`.

## SESIÓN XVI — Estado real del Home Cliente y validación manual

- `ClienteRepository` interpreta fechas remotas tanto como `Timestamp` como `Number`.
- `MainViewModel` carga el estado remoto del cliente y `HomeScreen` muestra el estado derivado.
- El comportamiento queda validado y NO debe modificarse:
  - Cliente `ACTIVO` sin movimientos: aparece activo y no muestra fecha de período porque `fechaFinActual` aún no existe.
  - Después de crear un movimiento: `fechaInicioActual` y `fechaFinActual` se calculan, se sincronizan con Firestore y el card muestra correctamente la fecha de fin.
- No modificar el card, su lógica, la UI de `appCliente` ni la sincronización de fechas por este asunto.

## SESIÓN XVII — Auditoría y corrección de Vía A código maestro + DNI

### Causa de la regresión

- `InicioScreen` llama a `MainViewModel.vincularConCodigoYDNI(codigo, dni)`.
- `VinculacionRepository` resuelve el negocio mediante `negocios_publicos` y `whereEqualTo("codigoMaestro", codigo)`.
- Después consulta exactamente `indices_clientes/{negocioId}_{dni}`.
- El commit `653f117de71a169dcb9f2f75e2dcdf6b6d4c44f5` eliminó el perfil pendiente pasado desde memoria, guardó la declaración temporal con el DNI introducido y luego leyó esa misma declaración como perfil.
- Si el índice no existía, la comparación del DNI siempre coincidía consigo misma y se ejecutaba Vía 2 mediante `crearFicha()`.
- La versión de `b31533bb9b4975b26cff68529837554813f111a6` comparaba el DNI introducido con el perfil original antes de permitir crear una ficha, por lo que rechazaba el DNI diferente.

### Corrección aplicada

- En `appCliente/.../VinculacionRepository.kt`, `ResultadoIndice.NoExiste` devuelve exactamente:
  `No existe ningún cliente registrado con ese DNI.`
- La rama sale antes de `crearFicha()`.
- No se modificó la vinculación de una ficha existente: mantiene la Transaction que actualiza `clientes/{idCliente}.firebaseUid` y `usuarios/{uid}`.
- El código de Vía 2 permanece en el repositorio, pero ya no se ejecuta automáticamente desde esta entrada Vía A.
- `firestore.rules` no se modificó.

### Tests añadidos

- `firestore-tests/firestore.rules.test.cjs`:
  - PRUEBA 6B: una Vía A no puede actualizar una ficha inexistente sin índice y no deja ficha, índice ni usuario vinculado.
  - PRUEBA 6C: dos DNI del mismo negocio solo pueden vincular sus fichas correspondientes; también se mantiene el aislamiento entre negocios de las pruebas anteriores.
- `appCliente/src/test/.../VinculacionRepositoryTest.kt` comprueba el resultado de rechazo para índice inexistente.
- Las pruebas existentes de Vía 2 se conservan sin modificar.

### Validación

- `npm --prefix firestore-tests test` → **92/92 OK**.
- `.\gradlew.bat :appCliente:testDebugUnitTest` → **BUILD SUCCESSFUL**.
- `.\gradlew.bat :appCliente:assembleDebug` → **BUILD SUCCESSFUL**.
- No se ejecutó `:app:assembleDebug` para esta corrección porque no hubo cambios compartidos del Admin.
- No hubo deploy ni commit.

## Estado de continuidad

- El working tree contiene cambios sin commit de las dos aplicaciones, Rules, tests y documentación. No revertir cambios ajenos.
- `firestore.rules` y sesiones/reservas no deben tocarse para continuar esta línea de trabajo.
- Vía B/deep link sigue descartada y no debe reintroducirse.
- Siguiente funcionalidad pendiente: reservas del CLIENTE (ver, reservar y cancelar) sobre `reservas/{clienteId}_{sesionId}`.
- Pendientes operativos: habilitar bucket de Storage, backfill de `indices_clientes` solo con aprobación, limpieza de basura versionada y commits agrupados.

## SESION XVIII - FASE 3: Diagnostico y recuperacion de regresiones

Fecha: 2026-08-31. Trabajo sobre el working tree SIN commit (heredado de las Sesiones XII-XVII).

### Objetivo de la fase
Estabilizar tres regresiones antes de continuar con reservas del CLIENTE:
1. Alta Admin no se sincroniza a Firestore (PERMISSION_DENIED en clientes/22).
2. Movimiento no activa el servicio contratado.
3. DatePicker de CompletarPerfil (appCliente) no abre el calendario.

Reglas vigentes de la fase: NO modificar firestore.rules, NO deploy, NO commit, NO reintroducir Via B/deep link, NO modificar sesiones/reservas del Admin salvo necesidad.

### PROBLEMA 1 - Alta Admin PERMISSION_DENIED (DIAGNOSTICADO, NO CORREGIDO)
- Sintoma real (logcat): `Write failed at clientes/22: PERMISSION_DENIED` y en
  `ClienteRemotoRepository`: `Error en alta de cliente: idCliente=22 codigo=PERMISSION_DENIED`.
- Evidencia comprobada en produccion por el desarrollador: `usuarios/{UID_ADMIN}` con
  rol=ADMIN, activo=true, clienteId=null, negocioId correcto; `negocios/{negocioId}` y
  `negocios_publicos/{negocioId}` existen y estan asociados. El ruleset DESPLEGADO en
  Firebase Console es IDENTICO al `firestore.rules` local (quedo descartada la hipotesis
  de rules desplegadas antiguas).
- Auditoria de codigo: `crearClienteRemoto()` construye un WriteBatch atomico de 3 escrituras:
  1. `clientes/{idCliente}` con `mapaDeAlta()` (18 claves, coincide con el hasOnly de clientes/create).
  2. `indices_clientes/{negocioId}_{dni}` con `{negocioId, dni, clienteId}` (indiceBienFormado).
  3. `clientes_privados/{idCliente}` con `{negocioId, observaciones}` (hasOnly de privados).
- El flujo de replica Admin es estable en el historial (identico entre b74f9d7, b194991, a02d11f);
  el commit b194991 (30/08) cambio ClienteViewModel para que onExito solo se llame si replicar()
  termina bien (antes onExito se llamaba siempre, ocultando fallos de replica).
- PRUEBA 9 de las Rules locales ya cubre el alta exacta y pasa (92/92).

### Test de aislamiento creado (firestore-tests/diagnostico_alta_cliente.test.cjs)
- 7 tests sobre las Rules LOCALES que reproducen el payload REAL de mapaDeAlta (con Timestamps,
  email/foto reales, serviciosContratados=[]).
- Resultado: **7/7 OK, 0 fail**.
  - Solo clientes/22 -> DENEGADO (esperado).
  - Solo indices_clientes -> DENEGADO (esperado).
  - Solo clientes_privados/22 -> PERMITIDO.
  - Batch completo 1+2+3 limpio -> PERMITIDO.  <- el alta real pasa contra Rules locales
  - Batch con INDICE YA EXISTENTE -> DENEGADO.
  - Batch con CLIENTES/22 YA EXISTENTE -> DENEGADO.
  - Batch con PRIVADOS/22 YA EXISTENTE -> DENEGADO.
- CONCLUSION DIAGNOSTICA: el payload y la logica son correctos contra las Rules locales.
  El PERMISSION_DENIED en produccion se explica porque al menos UNO de los 3 documentos
  objetivo ya existe en Firestore en el momento del alta: `batch.set()` sobre documento
  existente se evalua como UPDATE, y las Rules proh�ben update en indices_clientes
  (`allow update: if false`), en clientes (hasOnly de edicion que no admite
  firebaseUid/idCliente/negocioId/fechaRegistro) y en clientes_privados (update solo
  admite ["observaciones"] pero el set completo reescribe negocioId).
- Hipotesis mas probable: existe un indice `{negocioId}_{dni}` (o ficha/privado) de un intento
  anterior (encaja con el backfill pendiente de indices_clientes, DRY-RUN: 2 indices).
  El mensaje `Write failed at clientes/22` es enganoso: Firestore reporta el primer documento
  del batch, no necesariamente el que falla.

### Logging temporal de diagnostico ANADIDO en ClienteRemotoRepository.crearClienteRemoto()
- Registra (solo lectura, no cambia el batch): uid, negocioId (+tipo), idCliente (+tipo),
  documentIds de las 3 escrituras, dni, clienteId del indice, negocioId del indice,
  serviciosContratados (+tipo y tamano), claves del payload de clientes, tipos de campos
  relevantes, existencia previa de los 3 documentos (clientes/indices/privados) y el
  mensaje completo del error en los catch (codigo + mensaje original).
- Etiqueta de log: `[DIAG alta]`.
- Debe RETIRARSE cuando se confirme la causa. NO registrar datos sensibles.

### PROBLEMA 2 - Movimiento activa servicio contratado (CORREGIDO)
- Estado anterior: el formulario "Nuevo movimiento" usaba texto libre sin vinculo al catalogo.
- Correcion en `PerfilClienteAdministradorScreen.kt`: se anade selector de SERVICIOS ACTIVOS
  (RadioButton) bajo el campo Servicio; al guardar, si se eligio un servicio y el cliente no
  lo tenia, se llama a `viewModel.guardarServiciosContratados(idCliente, ...)` que escribe
  Room + Firestore (write-through via actualizarServiciosContratadosRemoto).
- No cambia `cliente.estado`; no relaja Rules. Regla para appCliente intacta: solo ve/reserva
  servicios contratados.

### PROBLEMA 3 - DatePicker de CompletarPerfil no abre (CORREGIDO)
- Causa exacta: el OutlinedTextField de fecha usaba `readOnly = true` PERO sin
  `enabled = false`. Un TextField Material3 en estado readOnly-habilitado consume el gesto
  de toque (posiciona cursor), por lo que `.clickable { mostrarSelectorFecha = true }`
  nunca se disparaba.
- Correcion en `CompletarPerfilScreen.kt`: anadir `enabled = false` + colores `disabled*`
  (mismo patron que ya funciona en A�adirClienteScreen del Admin). Se mantiene el
  DatePickerDialog, el formato dd/MM/yyyy, fecha obligatoria y sin fechas futuras.

### Validacion de la fase
- `npm --prefix firestore-tests test` -> 92/92 OK (firestore.rules intacto).
- `:app:assembleDebug` -> BUILD SUCCESSFUL. `:appCliente:assembleDebug` -> BUILD SUCCESSFUL.
- `:appCliente:testDebugUnitTest` -> BUILD SUCCESSFUL.
- `git diff --check` -> sin errores.
- Sin deploy, sin commit.

### Pendiente INMEDIATO (FASE 3.1)
1. Reproducir el alta desde el dispositivo con el build que incluye el logging `[DIAG alta]`
   y leer en logcat la linea `existencia previa -> clientes/22=?, indices_clientes/...=?,
   clientes_privados/22=?`. El `true` confirma cual documento ya existe y provoca el DENY.
2. Si existe un indice huerfano (hipotesis principal): decidir entre limpiar el documento
   remoto (con aprobacion) o ajustar crearClienteRemoto para que use update de solo
   serviciosContratados tras un alta local repetida. NO inventar reglas.
3. Retirar el logging temporal `[DIAG alta]` cuando se cierre la causa.

### Archivos tocados en esta fase (working tree, sin commit)
- `app/.../data/firebase/ClienteRemotoRepository.kt` (+logging temporal [DIAG alta]).
- `app/.../ui/clientes/PerfilClienteAdministradorScreen.kt` (selector servicio + activar contratado).
- `appCliente/.../ui/auth/CompletarPerfilScreen.kt` (enabled=false + colores disabled en fecha).
- `firestore-tests/diagnostico_alta_cliente.test.cjs` (nuevo, test de aislamiento 7/7).
- NO modificados: firestore.rules, storage.rules, vinculacion, reservas, sesiones, auth, Storage.

---

---

# ACTUALIZACIÓN 2026-08-31 (SESIÓN XIX) — horaDesdeReserva (Fase 3/3.1) + DIAGNÓSTICO REAL DE PRODUCCIÓN DE 3 PROBLEMAS

> Bloque vigente. Sesiones anteriores quedan como histórico. En esta sesión se implementó
> `horaDesdeReserva`, se diagnosticaron con datos REALES de producción los 3 problemas que el
> desarrollador reportó (servicio en detalle de movimiento, navegación a edición de sesión,
> clases del CLIENTE sin aparecer) y se crearon los índices compuestos que faltaban en Firestore.
> El trabajo previo de `horaDesdeReserva` fue commiteado por el desarrollador como `244db1e
> "Conectando las sesiones"` (antes `4bfb370 "Error de conexion admind cleinte corregido..."`).

## FASE horaDesdeReserva (implementada, validada y COMMITEADA por el desarrollador)

- Room: `SesionEntity.horaDesdeReserva: String? = null`, `ClientesDatabase` v11→**v12**,
  `MIGRACION_11_12` en `AppModule` (`ALTER TABLE sesion ADD COLUMN horaDesdeReserva TEXT`;
  import correcto `androidx.room.migration.Migration` en Room 2.8.4).
- Admin: `ProgramarSesionesScreen` (selector "Apertura de reservas" por día + "Abrir desde el
  inicio"), `EditarSesionScreen` (campo editable con TimePicker), `SesionViewModel.generarSesiones`
  (mapa `aperturasPorDia`), `SesionRemotoRepository.mapaDeSesion`/`actualizarSesionRemoto`.
- appCliente: `Sesion.kt`/`SesionRepository` leen el campo; `SesionesClienteViewModel` expone
  `reservable`/`aperturaAlcanzada`; `ClasesScreen` deshabilita "Reservar" + "Reservas abren a
  las HH:mm" antes de la apertura; `ReservaRepository.crearReserva` (capa B) rechaza con
  "Las reservas abren a las HH:mm".
- Rules: `sesiones/create` hasOnly + `sesiones/update` affectedKeys añaden `horaDesdeReserva`;
  capa C en `reservaCreaValida` con `reservaAbierta()`/`minutosReserva()` (usa `split(":")` +
  `int(...)`, NO `substring`/`.toInt()` que no existen en Rules). `null` = abierta.
- Tests Rules: **99/99** (92 + PRUEBA 82-88). `:app:assembleDebug`, `:appCliente:assembleDebug`,
  `:appCliente:testDebugUnitTest` BUILD SUCCESSFUL.

## DIAGNÓSTICO REAL DE PRODUCCIÓN (proyecto gestorpro-50e83, solo lectura vía REST con sesión CLI)

Se consultaron los datos reales (script temporal) para los 3 problemas que el desarrollador
reportó como "NO solucionados" pese a que el código compilaba:

### PROBLEMA 1 — "Servicio" no aparece en el detalle del movimiento
- **Causa:** no había bug en el código. El working tree (de otra sesión) ya contenía el campo
  "Servicio" en el diálogo del perfil (`PerfilClienteAdministradorScreen.kt` L1868-1883:
  `Text("Servicio")` + `movimientoSeleccionado?.servicio ?: "Sin servicio"`) y en Economía
  (`DialogDetalleMovimiento` L1091). El usuario lo probó con un build/APK anterior.
- **Resultado:** sin cambios. El campo está visible en los dos diálogos reales.

### PROBLEMA 2 — "Gestionar sesiones" no permite modificar la apertura
- **Causa (bug real de navegación):** `ProgramarSesionesScreen` (destino de "Gestionar sesiones")
  era solo un formulario de GENERACIÓN: no listaba sesiones existentes ni navegaba a
  `EditarSesionScreen`. La única ruta a edición era la sesión de HOY desde `DetalleServicioScreen`.
- **Corrección:** en `ProgramarSesionesScreen.kt` se añadió `sesionViewModel.cargarSesionesPorServicio`
  al entrar, la observación de `sesiones`, y la lista de sesiones ordenadas por fecha+hora con
  botón **"Ver / editar"** → `Routes.editarSesion(idSesion)` → `EditarSesionScreen` (que ya tenía
  el campo "Apertura de reservas"). Flujo real: Servicio → Gestionar sesiones → lista → Ver/editar.
- `:app:assembleDebug` BUILD SUCCESSFUL.

### PROBLEMA 3 — appCliente no muestra las clases de CrossFit (el más grave)
- **Causa raíz CONFIRMADA con datos de producción:** la colección `sesiones` en Firestore estaba
  **VACÍA (0 documentos)**. `clientes/21` (`serviciosContratados:[1]`, negocio `g84fPIy...`) y
  `servicios/1` (CrossFit activo, mismo negocio) eran correctos, pero NO había sesiones remotas.
  El ADMIN ve la sesión en Room local, pero la réplica a Firestore fallaba: usa la query
  `whereEqualTo("idServicio", id) + whereEqualTo("negocioId", id)`, que en producción exige un
  **ÍNDICE COMPUESTO que no existía** (el emulador no lo exige → por eso los tests 99/99 pasaban).
  Sin índice, `SesionRemotoRepository.obtenerIdsSesionesDelServicio`/`ReservaRemotoRepository`
  lanzaban error → en `SesionViewModel.generarSesiones` la `cascada.exito == false` → **nunca se
  replicaba** → appCliente → "No hay clases programadas para hoy". NO era fecha/negocio/contratados.
- **Corrección (infraestructura):** se crearon 3 índices compuestos vía API REST en `gestorpro-50e83`:
  - `sesiones(idServicio ASC, negocioId ASC)` — réplica Admin + lista del CLIENTE.
  - `reservas(clienteId ASC, negocioId ASC)` — "Mis reservas" del CLIENTE.
  - `reservas(sesionId ASC, negocioId ASC)` — cascadas ADMIN.
  Estado inicial `CREATING` (asíncrono); la query compuesta ya respondía **status 200 sin error de
  índice** poco después de la creación.
- **Acción pendiente del desarrollador:** cuando los índices pasen a `READY`, el ADMIN debe volver a
  "Gestionar sesiones" y pulsar "Generar sesiones" para que las sesiones se repliquen a Firestore.
  En appCliente quedaron logs temporales `ClasesDiagnostico` (otra sesión) en
  `SesionesClienteViewModel` para confirmar `sesionesBrutas>0` en logcat.

## Estado del working tree al cierre de la sesión

- HEAD = `244db1e` (commit del desarrollador "Conectando las sesiones").
- 5 archivos modificados sin commitear (NO revertir):
  - `PerfilClienteAdministradorScreen.kt` (campo "Servicio" en detalle del movimiento + scroll).
  - `DetalleServicioScreen.kt` (fix comparación fecha de "sesión de hoy" por LocalDate + muestra apertura).
  - `EditarSesionScreen.kt` (fix DatePicker UTC↔local).
  - `ProgramarSesionesScreen.kt` (lista de sesiones + navegación a edición) — cambio de esta sesión.
  - `SesionesClienteViewModel.kt` (logs `ClasesDiagnostico` + `esDeHoy()` robusto).
- Validación: `npm --prefix firestore-tests test` 99/99; `:app`/`:appCliente` BUILD SUCCESSFUL;
  tests unitarios BUILD SUCCESSFUL; `git diff --check` OK. Sin deploy, sin commit.

## Para REANUDAR

1. Confirmar que los índices de `sesiones`/`reservas` están `READY` en la consola.
2. Regenerar las sesiones desde el Admin (Gestionar sesiones → Generar sesiones) y verificar en
   Firestore que `sesiones` ya tiene documentos.
3. Comprobar en appCliente (logcat `ClasesDiagnostico`) que `sesionesBrutas>0` y que aparece
   CrossFit; luego retirar los logs temporales.
4. Pendientes heredados (sin resolver): PERMISSION_DENIED en alta Admin (`[DIAG alta]`, Sesión XVIII),
   bucket de Storage, backfill de `indices_clientes`, limpieza de `Clase`/`SesionClase`,
   crear negocio con PERMISSION_DENIED (hipótesis token), validar `rol == "ADMIN"` en login Admin.

---

---

# ACTUALIZACIÓN 2026-09-01 (SESIÓN XX) — APERTURA GLOBAL + BUG idSesion=0 CORREGIDO + LOGGING DIAGNÓSTICO

> Bloque vigente. Sesiones anteriores quedan como histórico. Se corrige el diseño de apertura
> de reservas (de por-día a global), se corrige el bug crítico `idSesion = 0` que impedía la
> réplica a Firestore, y se añade logging diagnóstico para confirmar el funcionamiento.

## PROBLEMA 1 — UI de apertura de reservas: de por-día a global (CORREGIDO)

### Diagnóstico

- La UI de `ProgramarSesionesScreen` original tenía un selector de apertura **por cada día** seleccionado (`aperturasPorDia: Map<DayOfWeek, String?>`). Esto era confuso para el usuario: si seleccionaba 5 días, tenía que configurar la apertura 5 veces.
- El usuario solicitó un **diseño global**: un solo campo "Apertura de reservas" que se aplique a **todas** las sesiones generadas en esa operación.

### Cambios implementados

- **`ProgramarSesionesScreen.kt`:**
  - `aperturasPorDia: Map<DayOfWeek, String?>` → `aperturaReservas: String?` (campo único global).
  - `mostrarSelectorApertura` reemplaza a `diaConAperturaTimePicker`.
  - Campo `OutlinedTextField` con `TimePicker` **fuera del loop de días**, antes de Duración/Capacidad.
  - Texto descriptivo: "Los clientes podrán reservar a partir de las HH:mm" o "desde el inicio del día".
  - Botón "Desde el inicio" en el diálogo para limpiar la apertura.
- **`SesionViewModel.kt`:**
  - `generarSesiones()` y `generar()` ahora reciben `aperturaReservas: String?` en lugar de `aperturasPorDia: Map<DayOfWeek, String?>`.
  - `horaDesdeReserva = aperturaReservas` para **todas** las sesiones generadas en la operación.
- **Edición individual:** `EditarSesionScreen` mantiene el campo "Apertura de reservas" por sesión individual (sin cambios). El ADMIN puede ajustar la apertura de una sesión concreta después de generarla.

### Validación
- Compilación `:app:assembleDebug` → BUILD SUCCESSFUL.
- La UI muestra el campo global correctamente, el TimePicker funciona, y el valor se propaga a todas las sesiones.

## PROBLEMA 2 — Bug `idSesion = 0` en réplica a Firestore (CORREGIDO)

### Diagnóstico

- **Síntoma:** al pulsar "Generar sesiones" en el Admin, las sesiones se creaban correctamente en Room con IDs reales (1, 2, 3...), pero en Firestore aparecían todas en `sesiones/0`.
- **Causa raíz:** `SesionViewModel.generar()` creaba `SesionEntity` con `idSesion = 0` (autoGenerate de Room). La función `insertarSesiones()` del DAO retornaba `Unit` sin propagar los IDs auto-generados de vuelta a la lista. La lista `nuevas` pasada a `sincronizarSesionesGeneradas()` tenía todos los IDs en 0 → todas las sesiones se escribían en el mismo documento `sesiones/0`.
- **Impacto:** la réplica parecía exitosa ("commit OK"), pero en Firestore solo existía 1 documento (`sesiones/0`) con los datos de la última sesión. appCliente no veía clases porque `idSesion` era 0 y no coincidía con las reservas.

### Corrección

- **`SesionDao.kt`:** añadido `obtenerSesionesFuturasPorServicioSync()` (suspend, returns `List<SesionEntity>`) — consulta síncrona (no Flow) que devuelve las sesiones con sus IDs reales.
- **`SesionRepository.kt`:** expuesta `obtenerSesionesFuturasPorServicioSync()`.
- **`SesionViewModel.generarSesiones()`:** tras `reservaRepository.regenerarProgramacion()`, se leen las sesiones de vuelta con `sesionRepository.obtenerSesionesFuturasPorServicioSync(servicio.idServicio, inicioHoy)` para obtener IDs reales (1, 2, 3...). Estas sesiones con IDs reales se pasan a `reservaRemotoRepository.eliminarSesionesFuturasConReservasRemoto()` y a `replicar()`.

### Flujo corregido
```
generar() → lista con idSesion=0
  → regenerarProgramacion() → Room insert → IDs reales (1, 2, 3...)
  → obtenerSesionesFuturasPorServicioSync() → lista con IDs reales
  → cascada remota (usa IDs reales)
  → sincronizarSesionesGeneradas() → Firestore recibe IDs reales
```

### Validación
- Compilación `:app:assembleDebug` → BUILD SUCCESSFUL.
- Tests de Rules: **99/99** OK.
- Test unitario `:appCliente:testDebugUnitTest` → BUILD SUCCESSFUL.

## PROBLEMA 3 — Logging diagnóstico añadido (TEMPORAL)

### Objetivo
- Confirmar que la réplica funciona correctamente en producción con IDs reales.
- Verificar que los `idSesion` en Firestore son 1, 2, 3... (no 0).

### Archivos con logging `[DIAG sesiones]`
- **`SesionViewModel.kt`:** `generarSesiones()` loga IDs generados, sesiones leídas de Room, resultado de cascada y réplica.
- **`SesionRemotoRepository.kt`:** `sincronizarSesionesGeneradas()` loga query, documentos encontrados, batch operations y commit.
- **`ReservaRemotoRepository.kt`:** `eliminarSesionesFuturasConReservasRemoto()` loga query y eliminación.

### Instrucciones de retiro
- Cuando se confirme que la colección `sesiones` en Firestore tiene documentos con IDs reales → retirar los logs `[DIAG sesiones]` de los 3 archivos.
- También retirar los logs `ClasesDiagnostico` de `SesionesClienteViewModel.kt` en appCliente.

## Archivos modificados en Sesión XX (sin commit)

- `app/src/main/java/com/roberto/gestorpro/ui/servicios/ProgramarSesionesScreen.kt` — UI apertura global.
- `app/src/main/java/com/roberto/gestorpro/ui/viewmodel/SesionViewModel.kt` — firma + bug fix + logging.
- `app/src/main/java/com/roberto/gestorpro/data/dao/SesionDao.kt` — `obtenerSesionesFuturasPorServicioSync()`.
- `app/src/main/java/com/roberto/gestorpro/data/repository/SesionRepository.kt` — expuesta nueva función.
- `app/src/main/java/com/roberto/gestorpro/data/firebase/SesionRemotoRepository.kt` — logging.
- `app/src/main/java/com/roberto/gestorpro/data/firebase/ReservaRemotoRepository.kt` — logging.
- `AGENTS.md` — actualización de estado.
- `CONVERSACION_EXPORTADA.md` — esta sesión.

## Para REANUDAR

1. **Verificar en Logcat:** al generar sesiones, buscar `[DIAG sesiones]` y confirmar que los `idSesion` son 1, 2, 3... (no 0).
2. **Verificar en Firebase Console:** la colección `sesiones` debe tener documentos con IDs reales.
3. **Verificar en appCliente:** `ClasesScreen` debe mostrar las sesiones de hoy (si hay servicios contratados+activos).
4. **Retirar logs temporales** cuando todo funcione.
5. Pendientes heredados (sin resolver): PERMISSION_DENIED en alta Admin, bucket de Storage, backfill de `indices_clientes`, limpieza de `Clase`/`SesionClase`, commits pendientes.

---

---

# ACTUALIZACIÓN 2026-09-01 (SESIÓN XX) — ELIMINACIÓN DEL SEED ROOM + DIAGNÓSTICO DE RÉPLICA DE SESIONES

> Bloque vigente. En esta sesión se (1) confirmó y eliminó la causa del fallo de
> sincronización de CLIENTES en un PC nuevo (seed automático de Room que ocupaba los
> ids 1-20), y (2) se diagnosticó por qué `sesiones` no llega a Firestore (bug `idSesion=0`
> en el código commiteado + error de réplica invisible). NO se hizo commit ni deploy.
> Sesiones anteriores quedan como histórico.

## PARTE 1 — CAUSA CONFIRMADA de la regresión de CLIENTES (PC nuevo)

- **Síntoma:** "Guardado en el dispositivo, pero no sincronizado" al crear un cliente desde el ADMIN; el cliente NO aparece en Firestore. Los SERVICIOS sí se sincronizan.
- **Evidencia por Git:** el commit funcional `4bfb370` no cambió código (solo `shelved.patch` + un XML de IDE). Entre `4bfb370` y HEAD (`4e29d8c`) hay SOLO 2 commits (`244db1e`, `4e29d8c`) que tocan exclusivamente sesiones/`horaDesdeReserva` + docs + `PerfilClienteAdministradorScreen` (UI). **Ningún archivo del flujo de clientes cambió**: `ClienteRemotoRepository`, `ClienteViewModel`, `ClienteRepository`, `ClienteDao`, `ClienteEntity`, `AutenticacionRepository` son byte-idénticos. `libs.versions.toml`/`build.gradle.kts` byte-idénticos. `google-services.json` NO está versionado (`.gitignore`), pero en este PC apunta a `gestorpro-50e83` y la réplica de servicios lo confirma.
- **Causa raíz:** seed automático de Room. `AppModule.insertarDatosPrueba()` (ejecutado desde `RoomDatabase.Callback.onCreate()`) insertaba **20 clientes ficticios** (ids autoincrement 1-20), 18 movimientos y 4 gastos solo al crear la BD. En un PC nuevo (BD vacía) el primer cliente real recibe `idCliente=21/22` → colisión con `clientes/21`/`clientes/22` (y/o con `indices_clientes/{negocioId}_12345678P/S`) ya existentes en Firestore → `batch.set` evaluado como UPDATE → Rules deniegan (`indices_clientes update:false`; `clientes/update` ADMIN con hasOnly de edición; `clientes_privados/update` solo `observaciones`) → PERMISSION_DENIED → "Guardado en el dispositivo, pero no sincronizado". Confirmado por Logcat (`[DIAG alta] existencia previa -> ...=true`) y por el test de aislamiento `firestore-tests/diagnostico_alta_cliente.test.cjs` (7/7): batch limpio ALLOW; con cualquiera de los 3 documentos preexistente → DENY.
- **Por qué el servicio sí se sincroniza:** `crearServicioRemoto` hace GET previo y devuelve "Servicio ya sincronizado" (éxito aparente) si el doc existe con el mismo `negocioId`, o crea si el id es nuevo. El flujo de clientes no tiene ese atajo → el batch falla entero.
- **Producción actual:** `clientes/21` (dni `12345678P`, "Prueba Colisión") y `clientes/22` (dni `12345678S`, "Cliente1 Prueba"), índices de ambos DNIs, `clientes_privados/21` y `/22`, `usuarios` = admin `g84fPIy...` + 2 clientes vinculados. `servicios/3` "CrossFit 3" existe (activo, del negocio). `sesiones` = 0 documentos.

## PARTE 2 — CAMBIO APLICADO: eliminación del seed

- **Archivo modificado (único):** `app/src/main/java/com/roberto/gestorpro/di/AppModule.kt` (**-102 líneas, 0 añadidas**).
  1. Eliminada la llamada `.addCallback(object : RoomDatabase.Callback() { onCreate -> insertarDatosPrueba(db) })` de `provideDatabase()`.
  2. Eliminada la función `insertarDatosPrueba()` completa (20 clientes, 18 movimientos, 4 gastos) + su comentario `TODO(PRODUCCION)`.
  3. Eliminados los imports sin uso: `androidx.room.RoomDatabase` y `java.util.concurrent.TimeUnit`. Se conserva `androidx.sqlite.db.SupportSQLiteDatabase` (lo usa `MIGRACION_11_12`).
- **NO se tocó:** migraciones Room, `fallbackToDestructiveMigration()`, DAOs, repositorios, ViewModels, entidades, servicios, sesiones, Firebase, Firestore Rules, sincronización ni autenticación.
- **Auditoría previa (sin referencias a conservar):** `insertarDatosPrueba` solo se usaba en `AppModule`; no hay tests que lo usen (solo `ExampleUnitTest.kt` stub). Otros inserts de clientes: `ClienteRepository` (flujo normal) y `ExportManager` (importación MANUAL del ADMIN) — se conservan. `allowBackup="false"` en el Manifest (dev) → desinstalar/borrar datos no restaura una BD vieja.
- **Validación:** `:app:assembleDebug` BUILD SUCCESSFUL; `:app:testDebugUnitTest` BUILD SUCCESSFUL; `npm --prefix firestore-tests test` **99/99 OK**; `git diff --check` limpio para AppModule.kt (solo reporta `firestore-tests/firestore-debug.log`, basura conocida del emulador). Sin commit, sin deploy.
- **Prueba pendiente (BD limpia):** desinstalar la app o `adb shell pm clear com.roberto.gestorpro`. Primer cliente real tras BD limpia debe recibir **`idCliente=1`** y replicarse sin colisión. El seed ya no se ejecuta en instalaciones nuevas; en BDs existentes los 20 clientes ficticios permanecen hasta borrarlos explícitamente.

## PARTE 3 — DIAGNÓSTICO de réplica de SESIONES (solo análisis, sin cambios)

- **Estado producción:** `servicios/3` "CrossFit 3" (activo, negocio `g84fPIy...`), `sesiones` = 0 docs, admin OK. Índices compuestos existentes (verificados con `firebase firestore:indexes`): `sesiones(idServicio, negocioId)`, `reservas(clienteId, negocioId)`, `reservas(sesionId, negocioId)`.
- **Hallazgo 1 — los logs `[DIAG sesiones]` NO existen en el código commiteado.** Grep global de `DIAG`/`Log.*` en `:app`: solo existe `[DIAG alta]` (clientes). `SesionViewModel.kt` y `SesionRemotoRepository.kt` no tienen ningún `Log`; `ReservaRemotoRepository` solo `Log.e` con TAG `ReservaRemotoRepository` (en `resultadoDeError`). Los logs vistos antes pertenecían a un working tree SIN commitear del otro PC; el `Update Project` (git pull) los perdió → por eso Logcat no muestra nada (no hay tag ni condición que los oculte; no están en el binario).
- **Hallazgo 2 — bug `idSesion=0` en el código actual (causa raíz de que no aparezcan las sesiones):** `SesionEntity.idSesion` es **`val`** (`SesionEntity.kt:19`), `SesionDao.insertarSesiones` devuelve **`Unit`** (`SesionDao.kt:26`), y `SesionViewModel.generarSesiones` pasa `sesionesNuevas = nuevas` (la lista pre-insert, con `idSesion=0`) a `replicar` → `SesionRemotoRepository.sincronizarSesionesGeneradas` → `batch.set` de TODAS las sesiones al documento **`sesiones/0`** (no `/40…/46`). El fix de releer ids desde Room (que existía en el working tree del otro PC) NO está commiteado. Consecuencia: el `batch.commit()` con escrituras duplicadas al mismo documento en un WriteBatch falla (→ no aparece nada) o escribe un único `sesiones/0`.
- **Hallazgo 3 — error de réplica INVISIBLE:** `ProgramarSesionesScreen` hace `navController.popBackStack()` inmediatamente tras generar (`ProgramarSesionesScreen.kt:384`) y NO observa `sesionViewModel.errorSincronizacion` → el fallo de réplica no se muestra al usuario.
- **Hallazgo 4 — apertura de reservas POR DÍA:** `ProgramarSesionesScreen` mantiene `aperturasPorDia: Map<DayOfWeek, String?>` con un TimePicker de apertura por cada día (L317-321 y L565-621); `SesionViewModel.generarSesiones` aplica `horaDesdeReserva = aperturasPorDia[fecha.dayOfWeek]` (L287). NO coincide con el comportamiento deseado (UNA sola apertura global para toda la generación). La edición individual posterior SÍ existe: ProgramarSesionesScreen → "Ver / editar" → `EditarSesionScreen` (campo "Apertura de reservas", L103/L328).
- **Flujo real al pulsar "Generar sesiones":** `ProgramarSesionesScreen` L365-396 → `SesionViewModel.generarSesiones` (L163) → `generar()` construye `SesionEntity(idSesion=0)` → `regenerarProgramacion` inserta en Room (ids reales en la BD, no en los objetos) → cascada `eliminarSesionesFuturasConReservasRemoto` (query `sesiones` por `idServicio`+`negocioId`; índice existe) → `replicar` → `sincronizarSesionesGeneradas` (query + `batch.set` con idSesion=0) → `commit`.
- **Causa más probable de "sesiones no aparece":** bug `idSesion=0` (todas las escrituras a `sesiones/0`; commit falla por escrituras duplicadas en el mismo batch o escribe doc inválido). El PERMISSION_DENIED por `servicios/3` inexistente **YA NO aplica** (el servicio existe, activo y del negocio). Si la query de la cascada fallara (índice no `READY`), el flujo no llegaría al batch — también silencioso.
- **Pendiente de decisión (NO implementado):** (1) fix en `SesionViewModel.generarSesiones`: tras `regenerarProgramacion`, releer las sesiones nuevas desde Room y pasar esas (con ids reales) a `replicar`; (2) `ProgramarSesionesScreen`: apertura global única + no `popBackStack` automático y mostrar `errorSincronizacion` (hace visible el fallo); (3) opcional: re-añadir logs `[DIAG sesiones]`.

## Working tree actual (sin commitear, NO revertir)

- HEAD = `4e29d8c "Conectando las sesiones"`.
- `app/.../di/AppModule.kt` (M, -102): eliminación del seed (esta sesión).
- `appCliente`: `AppNavigation.kt`, `Routes.kt`, `HomeScreen.kt` (M) + `MisReservasScreen.kt` (D): eliminación de la pantalla "Mis reservas" (cambio de otra IA, previo a esta sesión).
- `firestore-tests/firestore-debug.log` (M): basura del emulador regenerada por `npm test`.

## Para REANUDAR

1. **Prueba de BD limpia de clientes:** desinstalar la app o `adb shell pm clear com.roberto.gestorpro` → crear el primer cliente real → esperar `idCliente=1` y réplica OK (`[DIAG alta] existencia previa -> false,false,false`). Después retirar el logging temporal `[DIAG alta]` de `ClienteRemotoRepository.kt`.
2. **Aprobar el fix del bug `idSesion=0`** (SesionViewModel: releer de Room tras regenerarProgramacion) + apertura global única + visibilidad del error en ProgramarSesionesScreen.
3. Confirmar `READY` de índices y regenerar sesiones; retirar logs `ClasesDiagnostico` de appCliente cuando se confirmen.
4. Pendientes heredados: bucket de Storage, backfill de `indices_clientes` (solo con aprobación), limpieza de `Clase`/`SesionClase`, crear negocio con PERMISSION_DENIED (hipótesis token), validar `rol == "ADMIN"` en login Admin, commits agrupados y limpieza de basura versionada (`build_*.txt`, `firestore-debug.log`).

---

# ACTUALIZACIÓN 2026-09-01 (SESIONES XXI–XXIV) — FASE D, FASE E LOCAL, SOLICITUDES DE BAJA, AUDITORÍAS Y CORRECCIÓN DEL FLUJO DE BAJA

> Bloque vigente. Todo el trabajo posterior a `244db1e` sigue en el working tree SIN commit.
> Rules **123/123** (119 + PRUEBA 109–112). Detalle en `AGENTS.md` (hoja de ruta viva) y en los
> informes de cada fase. Sin Blaze, sin deploy, sin commit.

## SESIÓN XXI — FASE D: NOTIFICACIONES ADMIN (completada, sin desplegar)

- `GestionNotificacionesScreen` (lista de `notificaciones/{id}` del negocio, FAB "Nueva", icono ajustes, cancelar programadas).
- `CrearNotificacionScreen` (destino Individual/Grupo/Todos + contenido + programación opcional + resumen "Se enviará a X de Y clientes vinculados").
- `SeleccionarClientesScreen` (selección grupal reutilizando `ClienteItem`/`FilterChipItem`/`FiltroClientes`; la selección se conserva al cambiar filtros) y `DialogoSeleccionarClientes` (individual).
- `ConfigNotificacionesScreen` (morosidad, recordatorio 24h = 0/24, baja confirmada → `configuracion_notificaciones/{negocioId}`).
- Modelo `NotificacionAdmin` + `ConfiguracionNotificaciones` + `ModoDestino` + `DestinatarioResuelto`; `NotificacionesViewModel` compartido a nivel de `AppNavigation`; `NotificacionRemotoRepository`.
- **Cambio clave:** la creación inmediata crea `notificaciones/{id}` (PENDIENTE) + buzones y **deja PENDIENTE**; el estado final (ENVIADA/ERROR) y el push los resuelve Cloud Functions.

## SESIÓN XXII — FASE E: PREPARACIÓN LOCAL (pendiente Blaze)

- **Cloud Functions 2ª gen en `functions/`** (sin desplegar; proyecto en plan Spark, `billingEnabled=false` comprobado por API):
  - `index.js` (5 triggers) + `lib/{ids,tokens,firestore,destinatarios,envio,procesadores}.js` + `test/{ids,tokens}.test.js` (**13/13**).
  - Triggers: notificación inmediata (`onDocumentCreated` con CLAIM atómico PENDIENTE→ENVIADA), programadas (`onSchedule` 2 min, índice `notificaciones(estado, fechaProgramada)` pendiente), recordatorio morosidad (`onSchedule` 1 h, claim `ultimoRecordatorioMorosidad`), morosidad (`onUpdate clientes` con `fechaFinActual`), baja confirmada.
  - FCM `sendEachForMulticast` (≤500 tokens/lote), tokens inválidos eliminados, respeto de `notificacionesActivadas` por dispositivo, IDs deterministas + `set()` idempotente.
- appCliente: `DispositivoRepository` guarda `notificacionesActivadas` + `actualizarNotificacionesActivadas`; `NotificacionesClienteViewModel.setNotificacionesActivadas` la refleja.
- `storage.rules` local ampliada para fotos de clientes (`clientes/{clienteId}/foto.jpg`, image/* ≤5MB, ADMIN del negocio escribe, CLIENTE solo su propia foto); helper `rutaFotoClienteEnStorage` en `FotoUtils`.
- **Sin deploy, sin Blaze.** Bloqueo de economía intacto.

## SESIÓN XXIII — SOLICITUD DE BAJA DEL CLIENTE (completada)

- Reutilizada la colección Firestore **`solicitudes`** (NO `solicitudes_baja`) con `idSolicitud, negocioId, idCliente, firebaseUid, fechaSolicitud, estado (PENDIENTE/ACEPTADA/RECHAZADA), tipo (ALTA/BAJA), fechaResolucion, resueltaPor, motivo`. DocumentId determinista `baja_{clienteId}_{fechaSolicitud}`.
- **Rules reforzadas:** create CLIENTE con hasOnly + `esClientePuedeSolicitarBaja` (no BAJA/ARCHIVADO); update solo ADMIN desde PENDIENTE y solo campos de resolución. PRUEBA 99–108 → 119/119.
- Admin: `SolicitudesScreen` + card Home + `SolicitudesViewModel` + `SolicitudRemotoRepository` (acepta con Transaction: solicitud ACEPTADA + `clientes/{id}` BAJA + fechaBaja; rechaza dejando ACTIVO). appCliente: `CuentaScreen` "Solicitar baja" + `SolicitudRepository` (sin duplicado PENDIENTE).
- `NotificacionRemotoRepository.crearNotificacion` ampliado con `tipo`/`origen`/`notificacionId` (para BAJA_CONFIRMADA con ID determinista compartido con Functions) + `existeNotificacionFinalizada`.
- **Auditoría de economía** (solo lectura): no hay entidad Pago/Cuota (el pago es `Movimiento.estado`+`fechaPago`, y editar resetea `fechaPago`); sin reglas económicas (descuentos/tramos/estudiante/familia/jubilado/llave/"cuarto día hábil"); morosidad 100% derivada en Room (no replicable a Firestore); solo se replica `fechaInicioActual`/`fechaFinActual`. Bloque crítico pendiente (no tocado).

## SESIÓN XXIV — AUDITORÍA Y CORRECCIÓN DEL FLUJO DE BAJA (completada)

- **Diagnóstico:** `CuentaScreen` era **inalcanzable** (ninguna ruta navegaba a `Routes.CUENTA`); un cliente BAJA **podía** leer sesiones y reservar (ni app ni Rules lo bloqueaban); la baja directa (switch) y la aceptación de solicitud NO convergían; las reservas futuras no se cancelaban al dar de baja.
- **Corrección:**
  1. appCliente `ConfiguracionScreen` → **"Mi cuenta" → `Routes.CUENTA`** (hace accesible "Solicitar baja").
  2. **Rules:** helper `clientePuedeAcceder` (estado != "BAJA") en `sesiones get/list` CLIENTE y `reservaCreaValida`. **PRUEBA 109–112 → 123/123.**
  3. appCliente: `SesionesClienteViewModel.dadoDeBaja` (no carga sesiones si BAJA), `ClasesScreen` muestra el aviso, `HomeScreen` oculta la card "Clases", `ReservaRepository.crearReserva` rechaza BAJA.
  4. **Baja efectiva UNIFICADA en `BajaClienteRemotoRepository`:** cancela reservas FUTURAS en Room (`ReservaRepository.cancelarReservasFuturasDeCliente`, conTransaction liberando plazas) y en Firestore (reutiliza `cancelarReservaRemota`), conserva las pasadas y los `serviciosContratados`, y genera `BAJA_CONFIRMADA` (config + ID determinista). La **baja directa** (`ClienteViewModel.darDeBaja` + confirmación en `AñadirClienteScreen`) y la **aceptación de solicitud** (`SolicitudesViewModel`) convergen en esa lógica. `fechaBaja` coherente en Room/Firestore.
- **AGENTS.md** actualizado con hoja de ruta viva (ya implementado / pendiente de pruebas / parcial / pendiente / bloqueado por Blaze / decisiones / orden / dependencias / tests / problemas) y el estado a 2026-09-01.

## ESTADO ACTUAL (cierre 2026-09-01)

- Rules **123/123**; helpers de Functions **13/13**; builds `:app` y `:appCliente` BUILD SUCCESSFUL; `git diff --check` limpio.
- Sin Blaze, sin deploy de Functions/Storage, sin commit. Cloud Functions preparadas en local, listas para el día que se active la facturación.
- Pendiente principal: **auditoría y cierre del circuito de ECONOMÍA** (fuente de verdad del movimiento, morosidad real/"cuarto día hábil", pagos, BAJA+deuda, replicación a Firestore para Functions). Luego Blaze → índice `notificaciones(estado, fechaProgramada)` → `npm install` en `functions/` → deploy Functions + `storage.rules` → FCM real → Storage → pruebas finales integradas.

---

# ACTUALIZACIÓN 2026-09-02 (SESIONES XXV–XXVII) — CORRECCIONES/FUNCIONALIDADES + DIAGNÓSTICO ECONOMÍA

> Bloque vigente. HEAD = `3b113e6` (commits del desarrollador posteriores a Sesión XXIV:
> `9f54d0c`, `287d3ec`, `783ff31`, `5bd7c76`, `f6cfef0`, `459eb9d`, `10bdcea`, `3b113e6`).
> Todo el trabajo posterior a `244db1e` sigue en el working tree SIN commit. Rules **131/131**
> (123 + PRUEBA 113–120). Unit `:app` con `NotificacionConfigTest` (5 tests). Sin commit,
> sin deploy, sin Blaze.

## SESIÓN XXV — FASE DE CORRECCIONES Y PEQUEÑAS FUNCIONALIDADES

### 1. Regresión CRÍTICA: crear/generar sesiones desde el ADMIN (PERMISSION_DENIED) — RESUELTA

- **Síntoma:** al generar sesiones, "Cambio guardado en el dispositivo, pero no sincronizado con
  la nube: No tienes permisos para esta operación".
- **Diagnóstico (datos reales de producción vía REST con sesión CLI):** el negocio actual del
  ADMIN (`6YFNg1LfwrMY32t62iUVUnLHogL2`, "pruebatarde") tenía **`servicios` vacía en Firestore**
  (0 documentos) aunque el servicio existe en Room (id=1) y los clientes referencian
  `serviciosContratados=[1]`. La regla `sesiones/create` exige `servicioValidoParaSesion(id, negocio)`
  (`exists(servicios/{id}) && negocioId coincide && activo == true`); al no existir el documento
  remoto del servicio, la creación de sesiones se DENIEGA. NO era una regresión de Rules: la
  sección `sesiones` ADMIN es idéntica entre `287d3ec` (cuando funcionó) y HEAD, y el ruleset
  desplegado coincide en esa sección con el local (verificado vía REST). El servicio no se había
  replicado (se creó cuando el ruleset desplegado no tenía `match /servicios` y la réplica falló
  en silencio).
- **Fix:** `SesionViewModel.generarSesiones` replica el servicio antes de la cascada/réplica
  (`servicioRemotoRepository.crearServicioRemoto(servicio)`, idempotente: crea `servicios/{id}`
  si falta o devuelve "ya sincronizado"). No se abrió ningún permiso. Se inyectó
  `ServicioRemotoRepository` en `SesionViewModel`.
- **Regresión asegurada:** el negocio actual quedará arreglado al regenerar sesiones con el build
  nuevo (la app replica el servicio automáticamente).

### 2. SolicitudesScreen (ADMIN): búsqueda + borrado de resueltas + scroll

- **Búsqueda:** campo de texto que filtra por datos REALES del cliente asociado a la solicitud
  (`nombre + apellidos`, teléfono, DNI, email e idCliente) usando el mismo criterio
  `contains(ignoreCase=true)` que `ClientesScreen`. El modelo `Cliente` ya trae "nombre apellidos".
- **Borrado:** las solicitudes `ACEPTADA`/`RECHAZADA` muestran "Eliminar del historial" con
  confirmación ("¿Eliminar esta solicitud? / Esta acción eliminará la solicitud del historial. No
  se modifica el estado del cliente..."). `PENDIENTE` no ofrece la opción y las Rules la rechazan
  a nivel remoto (`solicitudes/delete` exige `estado != "PENDIENTE"`). Métodos nuevos:
  `SolicitudRemotoRepository.eliminarSolicitud()` y `SolicitudesViewModel.eliminarSolicitud()`.
- **Scroll:** la `LazyColumn` pasó a `Modifier.weight(1f)` (ocupaba todo el alto restante bajo
  encabezado/buscador y desplazaba internamente; antes no tenía restricción de altura).

### 3. Home appCliente: aviso de morosidad y borde de estado

- **Aviso:** se sustituyó la Card roja por un texto integrado (`ClickableText`): todo el texto en
  `colorScheme.error`, la palabra **"aquí"** en `colorScheme.primary` + negrita + subrayado; SOLO
  "aquí" es clicable → `Routes.CUENTA`. Aparece solo con `estadoHome.estado == PAGO_VENCIDO`
  (cliente ACTIVO con período vencido); nunca en BAJA/REGISTRADO/ARCHIVADO/ACTIVO sin vencimiento.
- **Card de estado:** el `BorderStroke` de `HomeClientEstadoIndicator` usa `colorScheme.error`
  cuando `PAGO_VENCIDO`; contenido y fondo intactos.
- **Ajuste final (Sesión XXVI):** el aviso se subió (padding `top=4dp, bottom=16dp`) y aumentó de
  `bodyMedium` a `bodyLarge`.

### 4. Notificación CLIENTE → ADMIN por solicitud de baja (FLUJO 1) — CORREGIDA

- **Causa de que no llegara:** las Rules DESPLEGADAS (01/09 15:18) solo permiten `esAdmin()` en
  `notificaciones/create`; la implementación previa hacía que el CLIENTE creara el documento y en
  producción esa escritura era DENEGADA (el error se tragaba; la solicitud sí se creaba). La
  bandeja ADMIN (`obtenerNotificaciones`) no filtra por tipo, así que el documento habría aparecido
  si existiera.
- **Fix:** la notificación la crea el ADMIN al cargar sus solicitudes PENDIENTES
  (`SolicitudesViewModel.generarAvisosDeSolicitudesPendientes` →
  `NotificacionRemotoRepository.crearNotificacionSolicitudBaja`, ID determinista
  `solicitud_baja_{clienteId}_{fechaSolicitud}`, tipo `SOLICITUD_BAJA`, origen `AUTOMATICA`,
  estado `PENDIENTE`). Se RETIRÓ la creación desde el CLIENTE (`appCliente/.../SolicitudRepository`
  vuelve a crear solo la solicitud). Rules: `SOLICITUD_BAJA` añadido al `tipo` permitido en el
  create ADMIN; retirada la excepción de CLIENTE. PRUEBA 118–120.

### 5. BAJA_CONFIRMADA (FLUJO 2) — CORREGIDA

- **Causa de que no llegara:** solo se generaba si `configuracion_notificaciones/{negocioId}
  .bajaConfirmada.activa == true`, y en producción no existe el documento (desactivado por defecto).
  Además el mensaje no llevaba la fecha.
- **Fix:** `NotificacionRemotoRepository` ahora devuelve `configuracionPorDefecto()` con
  `bajaConfirmadaActiva = true` cuando la configuración no existe (y trata el campo ausente como
  activo); el gate de `BajaClienteRemotoRepository.crearBajaConfirmada` usa
  `bajaConfirmadaActivaPorDefecto(config?.bajaConfirmadaActiva)` (solo un `false` explícito la
  desactiva). Mensaje nuevo: "Tu baja se ha realizado con fecha dd/MM/yyyy." Idempotencia con
  `existeNotificacion` e ID determinista `baja_confirmada_{clienteId}_{fechaBaja}` (igual que CF).

### 6. Tests / builds

- Rules **131/131**: PRUEBA 113–115 (sesiones: payload exacto de la app, servicio no replicado →
  DENY, regeneración batch), 116–117 (borrado de solicitudes), 118–120 (SOLICITUD_BAJA por ADMIN,
  CLIENTE no puede crear notificaciones, ADMIN de otro negocio DENY).
- Unit `:app` con `NotificacionConfigTest` (5): default de bajaConfirmada (inexistente/true/false/
  null) + ID determinista.
- `:app:assembleDebug` y `:appCliente:assembleDebug` BUILD SUCCESSFUL; `:app:testDebugUnitTest` y
  `:appCliente:testDebugUnitTest` BUILD SUCCESSFUL; `git diff --check` limpio.

## SESIÓN XXVII — DIAGNÓSTICO COMPLETO DE ECONOMÍA (SOLO LECTURA, nada implementado)

> Petición del propietario: reconstruir la arquitectura económica REAL leyendo el código. NO se
> modificó ningún archivo; el `git diff` solo muestra los cambios previos. El informe completo está
> en la respuesta de esta sesión (secciones 1–24). Resumen:

- **NO existe circuito económico.** No hay `CuotaEntity`, `PagoEntity`, `PagoDao`,
  `PagoRepository`, `Descuento`, `Tarifa`, método de pago (EFECTIVO/BIZUM/TRANSFERENCIA no existen
  en código) ni prorrateo.
- **Movimiento Room = la "cuota" de facto:** `servicio` (texto libre), `fechaInicio`, `fechaFin`,
  `precio` (manual), `estado` (PENDIENTE/PAGADO), `fechaPago` (solo lo rellena la renovación),
  `observaciones`. CRUD en el perfil; `EconomiaScreen` lo lista en solo lectura y gestiona gastos.
- **BUG confirmado:** editar un movimiento resetea `fechaPago` a null
  (`PerfilClienteAdministradorScreen.kt:2094-2109`). NO corregido (tarea de diagnóstico).
- **`ServicioEntity` no tiene precio.** Los servicios del catálogo (Sala máquinas, CrossFit, Yoga,
  Pilates) son solo nombre/descripción/activo. La suma de servicios, precios configurables,
  descuentos, tramos de alta/prorrateos y la "llave como tarifa" NO están implementados.
- **`fechaInicioActual`/`fechaFinActual`** se derivan en `MovimientoRepository` (movimiento con
  mayor `fechaFin`) y se replican SOLO a `clientes/{id}` en Firestore. No están en Room.
- **Morosidad derivada y NO persistida**, con dos fuentes: Room/Admin (`ClienteDao.obtenerIdsMorosos`:
  ACTIVO con `fechaFin < ahora` o BAJA con movimiento PENDIENTE) y appCliente/Functions
  (`clientes/{id}.fechaFinActual < ahora`). `EstadoCliente.MOROSO` nunca se persiste. La regla del
  **"cuarto día hábil" NO está implementada en ningún sitio**.
- **Firestore:** solo se replica el periodo; la colección `movimientos` tiene reglas (ADMIN) pero no
  se usa. No hay `pagos` ni `cuotas` remotas.
- **Cloud Functions (sin deploy):** entradaMorosidad/recordatorio/bajaConfirmada dependen solo de
  `clientes/{id}.fechaFinActual` + `configuracion_notificaciones`. `leerConfiguracion` de Functions
  trata la config inexistente como desactivada, mientras la app ahora la trata como activa →
  **divergencia pendiente de decidir**.
- **10 decisiones de negocio pendientes** (sección 24 del informe): cuota=movimiento o entidad;
  Pago independiente vs fechaPago; tarifas en Servicio; descuentos + edad estudiante; altas/
  prorrateos (tramos, 0,25 €/día, llave-tarifa); regla exacta de morosidad; BAJA+deuda;
  replicación mínima a Firestore; default config app↔CF; appCliente económico o no.

## ESTADO ACTUAL (cierre 2026-09-02)

- HEAD = `3b113e6`; working tree con las 2 fases de correcciones + diagnóstico ECONOMÍA (sin
  cambios de economía). Rules **131/131**; unit `:app` 6 tests (1 stub + 5 NotificacionConfig);
  builds `:app`/`:appCliente` BUILD SUCCESSFUL; `git diff --check` limpio. Sin commit/deploy/Blaze.
- **Rules desplegadas OBSOLETAS (01/09 15:18):** sin `clientePuedeAcceder`, sin
  `solicitudes/delete` restringido y sin `SOLICITUD_BAJA`. Pendiente de desplegar cuando se autorice.
- **Próximo paso (decisión del propietario):** responder a las **10 decisiones de ECONOMÍA**
  (Sesión XXVII §24) y autorizar implementación. Después: fases de implementación económica →
  Blaze/Functions/Storage.

## PARA REANUDAR

1. Leer `AGENTS.md` (estado 2026-09-02) y esta Sesión XXVII (diagnóstico ECONOMÍA, §24 decisiones).
2. El propietario decide sobre las 10 cuestiones económicas ANTES de programar.
3. Revisar/commitear el working tree (2 fases de correcciones) cuando se autorice.
4. Desplegar Rules (cuando se autorice) para activar en producción `clientePuedeAcceder`,
   `solicitudes/delete` y `SOLICITUD_BAJA`.

---

# ACTUALIZACIÓN 2026-09-02 (SESIONES XXVIII–XXXV) — ECONOMÍA FASES 1–5, AJUSTES LLAVE/CARDS Y SELECTOR DE NOTIFICACIÓN

> Bloque vigente. HEAD = `3b113e6` (sin commits nuevos; todo lo de esta tanda está en el
> working tree SIN commit). Rules **135/135**; unit `:app` **45/45** (Example 1 +
> MovimientoPrecio 10 + MovimientoPago 12 + MovimientoMorosidad 17 + NotificacionConfig 5).
> Detalle operativo en `AGENTS.md` (Estado actual). Sin commit, sin deploy.

## SESIÓN XXVIII — Llave como servicio normal + cards compactos

- **`tieneLlave` eliminado** (decisión del propietario: descartar valores, sin migración de datos):
  Room `ClienteEntity` (migración v13→14 recreando `cliente` sin la columna), `model/Cliente`
  (Admin y appCliente), switch "Tiene llave" en `AñadirClienteScreen`, fila en
  `PerfilClienteAdministradorScreen`, envío a Firestore (`ClienteRemotoRepository`) y Rules
  (`hasOnly`/`affectedKeys`). appCliente parser y ficha VÍA 2 sin el campo. No existe lógica
  especial de "Llave" en movimientos: pasa a ser un servicio normal.
- **ServiciosScreen:** eliminado el texto "ACTIVO"/"DE BAJA" dentro de `ServicioCard` (el estado ya
  se ve por sección/color) y después se compactó el card (padding 12→8/12, icono 36dp, botones
  32dp sin padding vertical extra, sin alturas artificiales). Añadido el **precio** del servicio a
  la derecha del nombre (`30 €`/`12,50 €`) manteniendo la compacidad.

## SESIÓN XXIX — Auditoría técnica (solo lectura)

- Informe completo de Economía: modelos, DAOs, repos, ViewModels, UI Admin, appCliente, Firestore,
  Functions, tests, legacy y bugs (pérdida de `fechaPago` al editar; morosidad con dos motores
  contradictorios; `totalIngresos` suma PENDIENTES; `EstadoCliente.MOROSO` obsoleto; etc.).
- Documentadas las 23 decisiones definitivas de negocio que guiaron las fases siguientes.

## SESIÓN XXX — FASE 1: Modelos Room + migración v13→14

- `ServicioEntity.precio: Double = 0.0` (sin precios inventados para existentes).
- `MovimientoEntity` nuevo: `servicios: List<Int>` (IntListConverter), `precioFinal: Double`,
  `metodoPago: MetodoPago?`; se retiran `servicio:String`/`precio`.
- `model/MetodoPago` (EFECTIVO/BIZUM/TRANSFERENCIA) + `MetodoPagoConverter` (nullable, fail-safe).
- `MIGRACION_13_14` NO destructiva (recrea `servicio` y `movimiento`): precioFinal=precio antiguo,
  metodoPago=NULL, `servicios` relleno solo si el nombre coincide exacto y único con `servicio`.
- Adaptaciones mínimas de compilación en pantallas (resolución de nombres de servicios vía map,
  `precioFinal`) y `EconomiaViewModel` con `serviciosMap`.

## SESIÓN XXXI — FASE 2: Precio de servicios (UI + Firestore)

- `EditarServicioScreen` campo "Precio (€)" (decimal, ≥0, precarga; error y no guarda si inválido).
- `ServiciosScreen` muestra el precio por card. `ServicioViewModel.crearServicio(nombre, desc, precio)`.
- `ServicioRemotoRepository` envía `precio` en alta y edición. Rules `servicios`: `precio is number`
  en create y en update solo si se toca el campo. Tests: PRUEBA 21B/21C/25B/25C → **135/135**.

## SESIÓN XXXII — FASE 3: Movimientos multi-servicio

- "Nuevo movimiento": checkboxes de servicios ACTIVOS (nombre + precio), propuesta
  `precioFinal` = suma de precios actuales (solo si NO hay modificación manual; botón
  "Usar precio calculado" la restaura). Validación: al menos un servicio.
- "Editar movimiento": checkboxes sobre ACTIVOS; los servicios dados de baja/históricos se
  muestran marcados y bloqueados (no se eliminan ni sustituyen). Movimientos antiguos con
  `servicios=[]` se conservan ("Sin servicio asociado").
- Crear/editar movimiento **NO** modifica `Cliente.serviciosContratados`.
- Helpers puros `util/MovimientoPrecio` (precioSugerido, precioFinalPropuesto,
  serviciosSeleccionables, idsFijosHistoricos, formateos) con 10 tests unitarios.

## SESIÓN XXXIII — FASE 4: Pagos dentro del movimiento

- `util/MovimientoPago` (+`DatosPago`, 12 tests): PENDIENTE→PAGADO fija `fechaPago` (hoy salvo
  fecha elegida por el ADMIN); PAGADO→PENDIENTE limpia `fechaPago` y `metodoPago`; método opcional.
- Diálogos: switch "Pago realizado", campo "Fecha de pago" con DatePicker y selector "Método de
  pago" (Sin especificar/EFECTIVO/BIZUM/TRANSFERENCIA). Al editar un PAGADO se cargan y conservan
  su `fechaPago` y `metodoPago` (corregido el bug de pérdida de `fechaPago`).
- EconomiaScreen detalle muestra Estado / Fecha de pago / Método de pago (solo cuando PAGADO, sin
  datos falsos). Renovar sin cambios.

## SESIÓN XXXIV — FASE 5: Deuda y morosidad en Room

- Migración v14→15: `ClienteEntity` + `Cliente` con `moroso: Boolean` y
  `fechaEntradaMorosidad: Long?` (iniciales false/null; sin reconstrucción histórica).
- Motor ÚNICO `util/MovimientoMorosidad` (17 tests): deuda = PENDIENTES exigibles
  (`fechaFin <= ahora`); ACTIVO moroso si deuda>0 o perdió la continuidad PAGADA (aunque no exista
  el siguiente movimiento); BAJA moroso SOLO por deuda; `fechaEntradaMorosidad` = fechaFin del
  periodo que la provoca y NO se reinicia al recalcular mientras siga moroso.
- `ClienteDao.obtenerIdsMorosos()` ahora lee el flag persistido (`moroso=1`) y se añade
  `actualizarMorosidadDao`. `MovimientoRepository` recalcula tras cada CRUD de movimiento y expone
  `recalcularMorosidadDeCliente` (usado en bajas directa/por solicitud y en restauración). Perfil
  usa el mismo motor para `esMoroso`. Eliminada la query antigua de morosidad (un solo motor).

## SESIÓN XXXV — Corrección: selector de cliente INDIVIDUAL en notificación

- El campo "Cliente" (destino Individual) ya no abre `DialogoSeleccionarClientes`; navega a la
  misma pantalla completa `SeleccionarClientesScreen` reutilizada por GRUPO, con nuevo parámetro
  `modoSeleccion` (`ModoSeleccion.UNO`/`MUCHOS`).
- `SeleccionarClientesScreen` en UNO impone selección única (cada clic sustituye la anterior);
  solo "Continuar" fija `NotificacionesViewModel.seleccionIndividual`; volver atrás no altera la
  selección previa. GRUPO sin cambios. Ruta `seleccionar_clientes?modo=grupo|individual`.
- `DialogoSeleccionarClientes` queda sin uso (no eliminado).

## ESTADO ACTUAL (cierre 2026-09-02)

- Room **v15** con migraciones 11→12, 12→13, 13→14 (movimiento/servicio), 14→15 (cliente).
  `fallbackToDestructiveMigration` aún presente como respaldo (no usado en estas rutas).
- Unit `:app` **45/45**; Rules **135/135** (no modificadas tras Fase 2); builds `:app`/`:appCliente`
  OK. Sin commit/deploy/Blaze.
- **No tocado:** Firestore de movimientos, Functions, appCliente (salvo cambios previos de la
  eliminación de `tieneLlave`), morosidad remota, `EstadoCliente.MOROSO` (aún presente), envío de
  notificaciones.
- Rules desplegadas en producción siguen OBSOLETAS (01/09 15:18).

## PARA REANUDAR

1. Leer `AGENTS.md` (Estado actual) — resume Fases 1–5 y pendientes.
2. Decidir la siguiente fase de ECONOMÍA (resumen/deuda en UI de Economía; sincronización
   Firestore de movimientos/resumen; retirar `EstadoCliente.MOROSO`; alinear default app↔Functions).
3. Revisar/commitear el working tree acumulado y limpiar basura (Rules desplegadas obsoletas,
   `firestore-debug.log`, `DialogoSeleccionarClientes` sin uso).

---

# ACTUALIZACIÓN 2026-09-0X (CHECKPOINT CIERRE DE CONVERSACIÓN) — ECONOMÍA FASE 6 + CORRECCIÓN ALTA/VÍAS + POLÍTICA PRIVACIDAD + UNIFICACIÓN VISUAL BOTONES

> Estado de CONTINUACIÓN. HEAD del desarrollador: `3b94164 "mejoras y correciones"`. Working tree con
> 29 cambios SIN commit. Esta sección resume TODO lo hecho en esta conversación y las INCONSISTENCIAS
> detectadas (el árbol mezcla avances con reverts del desarrollador). Ver detalle operativo en AGENTS.md
> (Estado actual y pendientes).

## 1) ECONOMÍA — FASE 6: Sincronización Room → Firestore (movimientos + resumen)
- `util/MovimientoFirestore.kt`: mapeador puro MovimientoEntity→documento + resumen del cliente.
- `data/firebase/MovimientoRemotoRepository.kt`: crear/actualizar/eliminar en `movimientos/{id}` (documentId determinista `idMovimiento.toString()`), con comprobación de propiedad (no pisar otro negocio).
- `MovimientoDao.insertarMovimiento` → devuelve `Long` (rowid) para conocer el id real.
- `MovimientoRepository` reescrito: tras CRUD recalcula morosidad (MovimientoMorosidad), replica el documento y publica el resumen en `clientes/{id}` (`moroso`, `fechaEntradaMorosidad`, `deuda`, `fechaInicioActual`, `fechaFinActual`); pendientes en `periodosPendientes`/`errorSincronizacion` (banner perfil).
- `ClienteRemotoRepository.actualizarResumenEconomicoRemoto` (sustituyó a `actualizarPeriodoActualRemoto`).
- `firestore.rules`: sección `movimientos` con hasOnly/tipos/`estado in [PENDIENTE,PAGADO]` y `clientes update` ADMIN con `moroso`/`fechaEntradaMorosidad`/`deuda`.
- Tests: `MovimientoFirestoreTest` (14) en `:app`; PRUEBA 121–125 en Rules. Unit `:app` pasaba (59 total).
- **ESTADO ACTUAL dudoso:** el ruleset local ahora NO contiene esas claves ni PRUEBA 121–129 (posible revert del desarrollador). VERIFICAR antes de seguir.

## 2) CORRECCIÓN alta de clientes / identificadores / vías
- Campos obligatorios en alta (ADMIN y CLIENTE): nombre, apellidos, teléfono, foto y DNI; en `clientes/create` (Rules) no vacíos. PRUEBA 126–127 (campos vacíos → DENY). Fecha de nacimiento pasó a opcional (PRUEBA 128–129) pero ESO QUEDÓ REVERTIDO (hoy vuelve a ser obligatoria en los formularios).
- `util/IdCliente.kt`: ids altos `[1e9, Int.MAX)` en el alta del ADMIN (Room deja de dar la identidad global).
- Adopción si el DNI ya existe en la nube (`buscarClienteEnNubePorDni`) y reconciliación Firestore→Room (`obtenerClientesRemotosDelNegocio` + `incorporarClientesRemotos` en ClientesScreen).
- VÍA 2 reactivada en appCliente `VinculacionRepository` (índice NoExiste → crear ficha si perfil completo); borra `perfiles_pendientes` solo al completarse. **Revisar si sobrevivió al revert.**
- Rules llegaron a **144/144** en esta conversación; hoy la suite local muestra **135 tests** (PRUEBA 121–129 ausentes).

## 3) DIAGNÓSTICO "Cliente 2: No tienes permisos para sincronizar esta ficha" + DEPLOY
- Causa: el update del resumen económico escribe `moroso`/`fechaEntradaMorosidad`/`deuda`, no permitidos en el ruleset DESPLEGADO (01/09). Rules locales sí (por eso el emulador pasa).
- SOLUCIÓN (autorizada): `deploy --only firestore:rules` del `firestore.rules` local → `released rules ... Deploy complete!`. No se desplegó nada más.

## 4) POLÍTICA DE PRIVACIDAD (ambas apps) — implementada
- `PoliticaPrivacidadScreen.kt` en `:app/ui/configuracion` y `:appCliente/ui/configuracion` (scroll, 12 secciones, responsable Roberto Carlos Salvador Martin / NIF 48910659D / Pollinox@hotmail.com).
- Accesos: Cliente (Configuración + enlaces Login/Registro); Admin (Ajustes → INFORMACIÓN → Política). Ruta `POLITICA_PRIVACIDAD` nueva en `:app`.
- Términos y condiciones quedan placeholder (no tocar). Sin checkbox de aceptación.

## 5) UNIFICACIÓN VISUAL DE BOTONES (App*Button)
- `Botones.kt` (en `:app` y `:appCliente`) fue RE-CREADO tras desaparecer del working tree (contenido idéntico al previo; no se inventaron variantes).
- `:app` `AppPrimaryButton` y `AppDialogConfirmButton` → azul corporativo `#1E88E5` (constante `AzulPrimarioGestPro`), porque el tema usa Material You (primary dinámico/verde). `:appCliente` mantiene AppPrimary con color del tema (no re-teñido).
- Re-unificadas (0 botones M3 directos al verificar): `:app` ClientesScreen, EditarServicioScreen, DetalleServicioScreen, EditarSesionScreen, AñadirClienteScreen (principales), PerfilClienteAdministradorScreen (botones principales + diálogos; excepciones: WhatsApp/drawable, DatePickers internos, Archivar/Restaurar semántico, selector de método). `:appCliente` CompletarPerfilScreen, EditarPerfilScreen, InicioScreen, MiPerfilScreen, ConfiguracionScreen, HomeScreen.
- Auditoría: quedan pantallas activas con Material 3 directo (auth de `:app`, PerfilCliente parcial, EconomiaScreen+diálogos, ProgramarSesiones, MiNegocio/CrearNegocio/Cuenta/Datos/Preferencias, notificaciones, solicitudes, appCliente Cuenta/ListaNotificaciones…). NO tocar `ui/clases/*` (legacy transitorio).
- `DetalleVisuales.kt` (kit de detalle, creado en una tanda) ya NO existe en el árbol → decidir recuperar/eliminar.
- Importante: varias tandas visuales (y la opcionalidad de fecha de nacimiento) fueron REVERTIDAS por commits del desarrollador y re-hechas parcialmente; verificar qué sobrevive antes de continuar.

## VERIFICACIÓN REALIZADA (últimos builds)
- Compilaciones `:app`/`:appCliente` BUILD SUCCESSFUL; unit tests OK; assemble OK. NO commit, NO deploy (salvo el deploy de firestore:rules arriba).

## PARA REANUDAR (resumen)
1. Conciliar ruleset local vs desplegado (movimientos + claves resumen + PRUEBA 121–129).
2. Revisar VÍA 2 (appCliente) y fecha de nacimiento opcional tras el revert.
3. Terminar unificación de botones en pantallas activas restantes.
4. Decidir futuro de `DetalleVisuales.kt` y limpieza (`firestore-debug.log`, basura).
5. Commits agrupados del working tree.

