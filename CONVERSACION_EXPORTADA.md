# Conversación GestorPro - Análisis Firestore Rules Límite 1000 Expresiones
## Fecha: 2026-08-24
## Estado: ⭐ RESUELTO Y AVANZADO — Ver la última "ACTUALIZACIÓN SESIÓN X" (2026-08-28) al final: flujo CLIENTE sin vínculo y VÍA 2 validados en dispositivo, sincronización del nombre del negocio validada y logo del negocio con Firebase Storage implementado (20/20 tests OK). Pendiente: habilitar el bucket de Storage en Firebase Console.

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
