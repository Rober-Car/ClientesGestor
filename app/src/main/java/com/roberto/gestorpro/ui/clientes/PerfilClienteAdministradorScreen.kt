package com.roberto.gestorpro.ui.clientes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Person
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.roberto.gestorpro.R
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.roberto.gestorpro.model.EstadoCliente
import com.roberto.gestorpro.navigation.Routes
import com.roberto.gestorpro.ui.utils.guardaFotoEnInterna
import com.roberto.gestorpro.ui.utils.crearFotoTemporal
import com.roberto.gestorpro.ui.utils.guardarFotoDeCamara
import com.roberto.gestorpro.ui.utils.uriDeFotoTemporal
import com.roberto.gestorpro.ui.components.AppDangerOutlinedButton
import com.roberto.gestorpro.ui.components.AppDialogDangerConfirmButton
import com.roberto.gestorpro.ui.components.AppDialogTextButton
import com.roberto.gestorpro.ui.components.AppNavigationBackButton
import com.roberto.gestorpro.ui.components.AppPrimaryButton
import com.roberto.gestorpro.ui.components.AppSecondaryButton
import com.roberto.gestorpro.ui.components.AppTextLinkButton
import com.roberto.gestorpro.ui.components.BotonSelectorFoto
import com.roberto.gestorpro.ui.viewmodel.ClienteViewModel
import com.roberto.gestorpro.ui.viewmodel.MovimientoViewModel
import com.roberto.gestorpro.util.MovimientoPrecio
import com.roberto.gestorpro.util.MovimientoPago
import com.roberto.gestorpro.util.MovimientoMorosidad
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import androidx.compose.material3.DatePicker
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.KeyboardType
import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.model.EstadoMovimiento
import com.roberto.gestorpro.model.MetodoPago


/**
 * PerfilClienteAdministradorScreen.kt
 * ------------------------------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de perfil de cliente)
 * Es el archivo que define la pantalla de perfil de un cliente.
 * Sirve para mostrar los datos y la gestión detallada de cada cliente.
 */

/**
 * PerfilClienteScreen
 * -------------------
 * ✔ TIPO: función @Composable
 * Es la pantalla de perfil del cliente con su estructura base.
 * Sirve para mostrar la información del cliente y su gestión.
 */
@Composable
fun PerfilClienteScreen(
    /**
     * navController
     * -------------
     * ✔ TIPO: parámetro (param) → NavHostController
     * Es el controlador de navegación que recibe la pantalla de perfil.
     * Sirve para poder volver atrás hacia la lista de clientes.
     */
    navController: NavHostController,
    /**
     * idCliente
     * ---------
     * ✔ TIPO: parámetro (param) → Int
     * Es el identificador del cliente que se quiere ver en el perfil.
     * Sirve para cargar sus datos y movimientos desde la base de datos.
     */
    idCliente: Int,
    /**
     * viewModel
     * ---------
     * ✔ TIPO: parámetro (param) → ClienteViewModel (inyectado por Hilt)
     * Es el ViewModel de clientes que recibe la pantalla.
     * Sirve para obtener el cliente seleccionado y gestionar sus datos.
     */
    viewModel: ClienteViewModel = hiltViewModel(),
    /**
     * movimientoViewModel
     * -------------------
     * ✔ TIPO: parámetro (param) → MovimientoViewModel (inyectado por Hilt)
     * Es el ViewModel de movimientos que recibe la pantalla.
     * Sirve para cargar y gestionar los movimientos (servicios) del cliente.
     */
    movimientoViewModel: MovimientoViewModel = hiltViewModel()
) {

    /**
     * LifecycleResumeEffect(idCliente)
     * --------------------------------
     * ✔ TIPO: efecto de ciclo de vida (LifecycleResumeEffect)
     * Se ejecuta al entrar en la pantalla y cada vez que se vuelve a ella.
     * Sirve para cargar el cliente por su id y para refrescar los datos
     * cuando el usuario vuelve del formulario de modificar cliente.
     */
    LifecycleResumeEffect(idCliente) {
        viewModel.obtenerClientePorId(idCliente)
        viewModel.cargarServicios()
        onPauseOrDispose { }
    }

    /**
     * LaunchedEffect(idCliente)
     * -------------------------
     * ✔ TIPO: efecto de composición (LaunchedEffect)
     * Se lanza cuando la pantalla se muestra por primera vez o cambia el id.
     * Sirve para cargar los movimientos del cliente cuyo id llega en el argumento de navegación.
     */
    LaunchedEffect(idCliente) {
        movimientoViewModel.cargarMovimientosPorCliente(idCliente)
    }

    /**
     * movimientos
     * -----------
     * ✔ TIPO: variable inmutable (val) → List<MovimientoEntity>
     * Es la lista de movimientos (servicios) del cliente observada desde el ViewModel.
     * Sirve para que la pestaña de Economía se actualice automáticamente cuando cambian los datos.
     */
    val movimientos by movimientoViewModel.movimientos.collectAsState()

    /**
     * cliente
     * -------
     * ✔ TIPO: variable inmutable (val) → ClienteEntity?
     * Es el cliente seleccionado observado desde el ViewModel.
     * Sirve para mostrar sus datos en la cabecera y en la pestaña de información.
     */
    val cliente by viewModel.clienteSeleccionado.collectAsState()

    val serviciosMap by viewModel.serviciosMap.collectAsStateWithLifecycle()

    val serviciosActivos by viewModel.serviciosActivos.collectAsStateWithLifecycle()

    val errorSincronizacion by viewModel.errorSincronizacion.collectAsStateWithLifecycle()

    val clienteSinSincronizar by viewModel.clienteSinSincronizar.collectAsStateWithLifecycle()

    val errorSincronizacionPeriodo by movimientoViewModel.errorSincronizacion.collectAsStateWithLifecycle()
    val periodosPendientes by movimientoViewModel.periodosPendientes.collectAsStateWithLifecycle()
    val periodoPendiente = idCliente in periodosPendientes

    /**
     * textoEstado
     * -----------
     * ✔ TIPO: variable inmutable (val) → String
     * Es el texto legible del estado del cliente en el perfil.
     * Sirve para traducir el enum a "Activo", "Moroso", "Baja" o "Registrado".
     */
    val textoEstado = when (cliente?.estado) {
        EstadoCliente.ACTIVO -> "Activo"
        EstadoCliente.MOROSO -> "Moroso"
        EstadoCliente.BAJA -> "Baja"
        else -> "Registrado"
    }

    /**
     * colorEstado
     * -----------
     * ✔ TIPO: variable inmutable (val) → Color
     * Es el color asociado al estado del cliente.
     * Sirve para pintar el chip del estado con el mismo color que la lista de clientes.
     */
    val colorEstado = when (cliente?.estado) {
        EstadoCliente.ACTIVO -> Color(0xFF4CAF50)
        EstadoCliente.MOROSO -> Color.Red
        EstadoCliente.BAJA -> Color.Gray
        else -> Color(0xFF64B5F6)
    }

    /**
     * esMoroso
     * --------
     * ✔ TIPO: variable inmutable (val) → Boolean
     * Indica si el cliente es moroso según la ÚNICA lógica MovimientoMorosidad
     * (flag independiente del estado administrativo). Sirve para mostrar el
     * borde rojo en la foto del perfil.
     */
    val esMoroso = cliente?.estado?.let { estadoCliente ->
        MovimientoMorosidad
            .resultadoDe(estadoCliente, movimientos, System.currentTimeMillis())
            .moroso
    } == true

    val context = LocalContext.current

    /**
     * fotoSeleccionada
     * ---------------
     * ✔ TIPO: variable con estado (var) → String?
     * Es la ruta de la foto elegida por el usuario para cambiar el perfil del cliente.
     * Sirve para almacenar temporalmente la ruta de la foto seleccionada
     * y mostrarla en la vista previa antes de guardar los datos.
     */
    var fotoSeleccionada by rememberSaveable { mutableStateOf<String?>(null) }

    /**
     * fotoTemporal
     * ------------
     * ✔ TIPO: variable con estado (var) → File?
     * Es el archivo temporal que rellena la app de cámara al hacer una foto.
     * Sirve para guardar la referencia hasta que el resultado del lanzador
     * de cámara devuelve el control y procesar la foto capturada.
     */
    var fotoTemporal by remember { mutableStateOf<File?>(null) }

    /**
     * launcherFoto
     * ------------
     * ✔ TIPO: variable (val) → ActivityResultLauncher<PickVisualMediaRequest>
     * Es el lanzador que abre el selector de fotos del sistema (galería).
     * Sirve para que el usuario elija una imagen; al volver, guarda la foto en
     * el almacenamiento interno y muestra la ruta en la variable fotoSeleccionada.
     */
    val launcherFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val ruta = guardaFotoEnInterna(context, uri)
            if (ruta != null) {
                fotoSeleccionada = ruta
            }
        }
    }

    /**
     * launcherTomarFoto
     * -----------------
     * ✔ TIPO: variable (val) → ActivityResultLauncher<Uri>
     * Es el lanzador que abre la app de cámara para hacer una foto nueva.
     * Sirve para capturar la imagen en el archivo temporal y, si la foto se
     * tomó correctamente, guardarla en el almacenamiento interno.
     */
    val launcherTomarFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { resultado ->
        if (resultado) {
            val ruta = guardarFotoDeCamara(context, fotoTemporal)
            if (ruta != null) {
                fotoSeleccionada = ruta
            }
        } else {
            fotoTemporal?.delete()
        }
        fotoTemporal = null
    }

    /**
     * pestañaSeleccionada
     * -------------------
     * ✔ TIPO: variable con estado (var) → Int
     * Es el índice de la pestaña activa en la pantalla de perfil.
     * Sirve para alternar entre la pestaña de Datos (0) y la de Economía (1).
     */
    var pestañaSeleccionada by remember {
        mutableStateOf(0)
    }

    /**
     * mostrarFormularioMovimiento
     * ---------------------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es la variable que controla si se muestra el formulario de nuevo movimiento.
     * Sirve para abrir y cerrar el diálogo o pantalla de añadir movimiento.
     */
    var mostrarFormularioMovimiento by rememberSaveable {
        mutableStateOf(false)
    }

    var idsServiciosMovimiento by rememberSaveable { mutableStateOf<List<Int>>(emptyList()) }
    var precioMovimiento by rememberSaveable { mutableStateOf("") }
    var precioMovimientoManual by rememberSaveable { mutableStateOf(false) }
    var observacionesMovimiento by rememberSaveable { mutableStateOf("") }
    var fechaInicioMovimiento by rememberSaveable { mutableStateOf<Long?>(null) }
    var fechaFinMovimiento by rememberSaveable { mutableStateOf<Long?>(null) }

    var mostrarDatePickerInicio by rememberSaveable { mutableStateOf(false) }
    var mostrarDatePickerFin by rememberSaveable { mutableStateOf(false) }
    var mostrarDatePickerPago by rememberSaveable { mutableStateOf(false) }

    val fechaInicioFormateada =
        fechaInicioMovimiento?.let { formatearFecha(it) } ?: ""

    val fechaFinFormateada =
        fechaFinMovimiento?.let { formatearFecha(it) } ?: ""

    var movimientoPagado by rememberSaveable {
        mutableStateOf(false)
    }

    // Fecha de pago elegida en el formulario (null hasta que se marque PAGADO).
    var fechaPagoMovimiento by rememberSaveable { mutableStateOf<Long?>(null) }
    // Método de pago elegido: nombre o null (= "Sin especificar").
    var metodoPagoMovimientoNombre by rememberSaveable { mutableStateOf<String?>(null) }

    val fechaPagoFormateada =
        fechaPagoMovimiento?.let { formatearFecha(it) } ?: ""

    var errorServicioMovimiento by rememberSaveable { mutableStateOf(false) }
    var errorPrecioMovimiento by rememberSaveable { mutableStateOf(false) }
    var errorFechaInicioMovimiento by rememberSaveable { mutableStateOf(false) }
    var errorFechaFinMovimiento by rememberSaveable { mutableStateOf(false) }

    /* ============================================================
     * ============ ESTADO DEL DIÁLOGO RENOVAR MOVIMIENTO =========
     * ============================================================ */
    /**
     * mostrarDialogoRenovar
     * ---------------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Controla si se muestra el diálogo de renovación de movimiento.
     * Sirve para abrir y cerrar el diálogo donde se elige el inicio del nuevo periodo.
     */
    var mostrarDialogoRenovar by rememberSaveable {
        mutableStateOf(false)
    }

    /**
     * renovarDesdeHoy
     * ---------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Indica la opción elegida en el diálogo de renovación.
     * Sirve para decidir si el nuevo periodo empieza hoy (true)
     * o en la fecha de fin del último movimiento (false).
     */
    var renovarDesdeHoy by rememberSaveable {
        mutableStateOf(true)
    }

    /**
     * movimientoARenovar
     * ------------------
     * ✔ TIPO: variable con estado (var) → MovimientoEntity? (nullable)
     * Es el último movimiento del cliente que se va a renovar.
     * Sirve como base para calcular las fechas del nuevo periodo y copiar servicio y precio.
     * Es null cuando no hay ningún movimiento seleccionado para renovar.
     */
    var movimientoARenovar by remember {
        mutableStateOf<MovimientoEntity?>(null)
    }

    /* ============================================================
     * ============ ESTADO DEL DIÁLOGO DETALLE MOVIMIENTO =========
     * ============================================================ */
    /**
     * movimientoSeleccionado
     * ---------------------
     * ✔ TIPO: variable con estado (var) → MovimientoEntity?
     * Es el movimiento que el administrador ha pulsado para ver su detalle.
     * Sirve para abrir el diálogo con los datos de ese movimiento y poder editarlos o eliminarlos.
     * Es null cuando no hay ningún movimiento seleccionado (diálogo cerrado).
     */
    var movimientoSeleccionado by rememberSaveable { mutableStateOf<MovimientoEntity?>(null) }

    /**
     * idsServiciosEditados
     * --------------------
     * IDs de los servicios ACTIVOS marcados en el diálogo de detalle (editables).
     */
    var idsServiciosEditados by rememberSaveable { mutableStateOf<List<Int>>(emptyList()) }

    /**
     * idsServiciosEditadosFijos
     * -------------------------
     * IDs del movimiento histórico que ya NO son servicios activos (de baja o
     * eliminados). Se conservan: se muestran marcados y no se pueden quitar.
     */
    var idsServiciosEditadosFijos by rememberSaveable { mutableStateOf<List<Int>>(emptyList()) }

    /**
     * precioEditado
     * -------------
     * Campo "Precio final" editable dentro del diálogo de detalle.
     */
    var precioEditado by rememberSaveable { mutableStateOf("") }

    /**
     * precioEditadoManual
     * -------------------
     * true si el ADMIN ha tecleado el precio en la edición (no se sobrescribe
     * de forma automática con la suma de servicios).
     */
    var precioEditadoManual by rememberSaveable { mutableStateOf(false) }

    /**
     * fechaInicioEditada
     * ------------------
     * ✔ TIPO: variable con estado (var) → Long?
     * Es la fecha de inicio editable dentro del diálogo de detalle.
     * Sirve para que el administrador pueda modificar la fecha de inicio del servicio.
     */
    var fechaInicioEditada by rememberSaveable { mutableStateOf<Long?>(null) }

    /**
     * fechaFinEditada
     * ---------------
     * ✔ TIPO: variable con estado (var) → Long?
     * Es la fecha de fin editable dentro del diálogo de detalle.
     * Sirve para que el administrador pueda modificar la fecha de fin del servicio.
     */
    var fechaFinEditada by rememberSaveable { mutableStateOf<Long?>(null) }

    /**
     * pagadoEditado
     * -------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es el interruptor "Pago realizado" editable dentro del diálogo de detalle.
     * Sirve para que el administrador pueda cambiar el estado de pago del servicio.
     */
    var pagadoEditado by rememberSaveable { mutableStateOf(false) }

    // Fecha de pago existente/elegida al editar (null si PENDIENTE).
    var fechaPagoEditada by rememberSaveable { mutableStateOf<Long?>(null) }
    // Método de pago existente/elegido al editar (nombre o null).
    var metodoPagoEditadoNombre by rememberSaveable { mutableStateOf<String?>(null) }
    var mostrarDatePickerPagoDetalle by rememberSaveable { mutableStateOf(false) }

    /**
     * observacionesEditadas
     * ---------------------
     * ✔ TIPO: variable con estado (var) → String
     * Es el campo "Observaciones" editable dentro del diálogo de detalle.
     * Sirve para que el administrador pueda modificar las notas del servicio.
     */
    var observacionesEditadas by rememberSaveable { mutableStateOf("") }

    /**
     * mostrarDatePickerInicioDetalle
     * -----------------------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es la variable que controla si el selector de fecha de inicio está visible en el diálogo.
     * Sirve para abrir y cerrar el DatePickerDialog de inicio dentro del detalle.
     */
    var mostrarDatePickerInicioDetalle by rememberSaveable { mutableStateOf(false) }

    /**
     * mostrarDatePickerFinDetalle
     * ---------------------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es la variable que controla si el selector de fecha de fin está visible en el diálogo.
     * Sirve para abrir y cerrar el DatePickerDialog de fin dentro del detalle.
     */
    var mostrarDatePickerFinDetalle by rememberSaveable { mutableStateOf(false) }

    /**
     * errorPrecioEditado
     * ------------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es el indicador de error del campo Precio en el diálogo de detalle.
     * Sirve para resaltar el campo si el precio no es válido al pulsar Guardar cambios.
     */
    var errorPrecioEditado by rememberSaveable { mutableStateOf(false) }

    /**
     * errorFechaInicioEditada
     * -----------------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es el indicador de error del campo Fecha de inicio en el diálogo de detalle.
     * Sirve para resaltar el campo si no se ha seleccionado fecha al pulsar Guardar cambios.
     */
    var errorFechaInicioEditada by rememberSaveable { mutableStateOf(false) }

    /**
     * errorFechaFinEditada
     * --------------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es el indicador de error del campo Fecha de fin en el diálogo de detalle.
     * Sirve para resaltar el campo si no se ha seleccionado fecha o si es anterior a la de inicio.
     */
    var errorFechaFinEditada by rememberSaveable { mutableStateOf(false) }

    /**
     * mostrarConfirmarEliminar
     * -----------------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es la variable que controla si se muestra el diálogo de confirmación de eliminación.
     * Sirve para pedir confirmación al administrador antes de borrar un movimiento definitivamente.
     */
    var mostrarConfirmarEliminar by rememberSaveable { mutableStateOf(false) }

    var mostrarConfirmarArchivar by rememberSaveable { mutableStateOf(false) }

    /**
     * alternarServicioNuevo
     * ---------------------
     * Marca/desmarca un servicio activo en el formulario de nuevo movimiento.
     * Si el ADMIN aún NO ha tecleado un precio manual, actualiza la propuesta
     * de "Precio final" con la suma de los precios actuales de la selección;
     * si el precio es manual, NO lo sobrescribe (el ADMIN puede usar luego el
     * botón "Usar precio calculado" para recalcular explícitamente).
     */
    fun alternarServicioNuevo(idServicio: Int) {
        idsServiciosMovimiento = if (idServicio in idsServiciosMovimiento) {
            idsServiciosMovimiento - idServicio
        } else {
            (idsServiciosMovimiento + idServicio).distinct()
        }
        errorServicioMovimiento = false
        if (!precioMovimientoManual) {
            val suma = serviciosActivos
                .filter { it.idServicio in idsServiciosMovimiento }
                .sumOf { it.precio }
            precioMovimiento = MovimientoPrecio.precioCampo(suma)
        }
    }

    /**
     * abrirFormularioNuevoMovimiento
     * ------------------------------
     * Abre el diálogo de nuevo movimiento con el formulario en blanco.
     */
    fun abrirFormularioNuevoMovimiento() {
        idsServiciosMovimiento = emptyList()
        precioMovimiento = ""
        precioMovimientoManual = false
        observacionesMovimiento = ""
        fechaInicioMovimiento = null
        fechaFinMovimiento = null
        movimientoPagado = false
        fechaPagoMovimiento = null
        metodoPagoMovimientoNombre = null
        errorServicioMovimiento = false
        errorPrecioMovimiento = false
        errorFechaInicioMovimiento = false
        errorFechaFinMovimiento = false
        mostrarDatePickerInicio = false
        mostrarDatePickerFin = false
        mostrarDatePickerPago = false
        mostrarFormularioMovimiento = true
    }

    /**
     * alternarServicioEditado
     * -----------------------
     * Marca/desmarca un servicio ACTIVO en el diálogo de edición. El precio
     * nunca se recalcula automáticamente en edición (se conserva el valor
     * histórico/manual); el ADMIN lo ajusta a mano si lo necesita.
     */
    fun alternarServicioEditado(idServicio: Int) {
        idsServiciosEditados = if (idServicio in idsServiciosEditados) {
            idsServiciosEditados - idServicio
        } else {
            (idsServiciosEditados + idServicio).distinct()
        }
    }

    /**
     * mostrarDialogoServicios
     * -----------------------
     * Controla si se muestra el diálogo de edición de los servicios
     * contratados del cliente.
     */
    var mostrarDialogoServicios by rememberSaveable { mutableStateOf(false) }

    /**
     * LaunchedEffect(movimientoSeleccionado)
     * --------------------------------------
     * ✔ TIPO: efecto de composición (LaunchedEffect)
     * Se lanza cada vez que se selecciona un movimiento para ver su detalle.
     * Sirve para precargar los campos editables con los datos actuales del movimiento.
     */
    LaunchedEffect(movimientoSeleccionado) {
        movimientoSeleccionado?.let { mov ->
            val activosIds = serviciosActivos.map { it.idServicio }.toSet()
            idsServiciosEditados = mov.servicios.filter { it in activosIds }
            idsServiciosEditadosFijos =
                MovimientoPrecio.idsFijosHistoricos(mov.servicios, activosIds)
            precioEditado = MovimientoPrecio.precioCampo(mov.precioFinal)
            // En edición el importe se trata como manual: los cambios de
            // selección NO sobrescriben el precio histórico por accidente.
            precioEditadoManual = true
            fechaInicioEditada = mov.fechaInicio
            fechaFinEditada = mov.fechaFin
            pagadoEditado = mov.estado == EstadoMovimiento.PAGADO
            fechaPagoEditada = mov.fechaPago
            metodoPagoEditadoNombre = mov.metodoPago?.name
            observacionesEditadas = mov.observaciones ?: ""
            errorPrecioEditado = false
            errorFechaInicioEditada = false
            errorFechaFinEditada = false
        }
    }

    /**
     * Scaffold
     * --------
     * ✔ TIPO: función @Composable (androidx.compose.material3.Scaffold)
     * Es el contenedor base de la pantalla de perfil del cliente.
     * Sirve como estructura general y respeta las barras del sistema (status y navegación).
     */
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->

        /**
         * Column principal
         * ----------------
         * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
         * Es el contenedor vertical general de la pantalla de perfil.
         * Sirve para apilar con un margen lateral la cabecera, los datos y los servicios,
         * permitiendo hacer scroll si el contenido no cabe en la pantalla.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            /**
             * Row de la cabecera
             * ------------------
             * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Row)
             * Es la fila superior que junta la flecha de volver con el título.
             * Sirve para retroceder a la lista y mostrar en qué pantalla estamos.
             */
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    AppNavigationBackButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Perfil de cliente",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            /**
             * Aviso de sincronización con la nube
             * -----------------------------------
             * Aparece cuando un cambio local (p. ej. los servicios contratados)
             * se guardó en Room pero la réplica a Firestore falló. Informa de
             * que Room está actualizado pero Firestore no, y ofrece el reintento
             * manual de sincronización sin revertir nada.
             */
            if (
                    errorSincronizacion != null ||
                    clienteSinSincronizar != null ||
                    (errorSincronizacionPeriodo != null && periodoPendiente) ||
                    periodoPendiente
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            CircleShape
                        )
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = errorSincronizacion
                            ?: errorSincronizacionPeriodo.takeIf { periodoPendiente }
                            ?: "Hay cambios pendientes de sincronizar con la nube.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )

                    AppSecondaryButton(
                        text = "Reintentar sincronización",
                        onClick = {
                            if (clienteSinSincronizar != null) {
                                viewModel.reintentarSincronizacion()
                            } else {
                                movimientoViewModel.reintentarSincronizacionPeriodo(idCliente)
                            }
                        },
                        enabled = clienteSinSincronizar != null || periodoPendiente
                    )
                }
            }

            /**
             * Column de la cabecera del cliente
             * ----------------------------------
             * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
             * Es el bloque que centra la foto, el nombre y el estado del cliente.
             * Sirve para presentar visualmente al cliente al inicio del perfil.
             */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                val fotoMostrar = fotoSeleccionada ?: cliente?.foto ?: null

                if (fotoMostrar == null) {
                    /**
                     * Box del placeholder de la foto
                     * ------------------------------
                     * ✔ TIPO: bloque condicional (if) + función @Composable (androidx.compose.foundation.layout.Box)
                     * Es el círculo con el icono de persona que se muestra cuando el cliente no tiene foto.
                     * Sirve para mantener la cabecera visualmente completa con el mismo estilo del formulario.
                     */
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(3.dp, colorEstado, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Foto del cliente",
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = fotoMostrar?.let { File(it) } ?: null,
                        contentDescription = "Foto del cliente",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(3.dp, colorEstado, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                /**
                 * Text del nombre
                 * ---------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
                 * Es el nombre del cliente que se muestra en el perfil.
                 * Sirve para presentar el nombre con un estilo destacado y centrado.
                 */
                Text(
                    text = cliente?.nombre ?: "Cargando...",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = colorEstado.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = textoEstado,
                        style = MaterialTheme.typography.labelLarge,
                        color = colorEstado,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            /**
             * TabRow de pestañas
             * ------------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.TabRow)
             * Es la barra de pestañas que alterna entre "Datos" y "Economía".
             * Sirve para organizar la información del cliente en dos secciones separadas.
             */
            TabRow(
                selectedTabIndex = pestañaSeleccionada,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Color(0xFF2196F3),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pestañaSeleccionada]),
                        height = 3.dp,
                        color = Color(0xFF2196F3)
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = pestañaSeleccionada == 0,
                    onClick = { pestañaSeleccionada = 0 },
                    text = {
                        Text(
                            text = "Datos",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (pestañaSeleccionada == 0) Color(0xFF2196F3) else Color.Gray
                        )
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Tab(
                    selected = pestañaSeleccionada == 1,
                    onClick = { pestañaSeleccionada = 1 },
                    text = {
                        Text(
                            text = "Economía",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (pestañaSeleccionada == 1) Color(0xFF2196F3) else Color.Gray
                        )
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (pestañaSeleccionada == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    val telefono = cliente?.telefono ?: return@IconButton
                                    val uri = Uri.parse("https://wa.me/34$telefono")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_whatsapp),
                                    contentDescription = "Abrir WhatsApp",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Telefono", cliente?.telefono ?: "")
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Telefono copiado", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                            ) {
                                Text(
                                    text = "Teléfono",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = cliente?.telefono ?: "No disponible",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = "Icono de DNI",
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "DNI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = cliente?.dni ?: "Cargando...",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Icono de email",
                        tint = Color(0xFF8E24AA),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Email", cliente?.email ?: "")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Email copiado", Toast.LENGTH_SHORT).show()
                            }
                        )
                    ) {
                        Text(
                            text = "Email",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = cliente?.email ?: "Sin email",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Icono de fecha de nacimiento",
                        tint = Color(0xFFFB8C00),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Fecha de nacimiento",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = cliente?.let {
                                it.fechaNacimiento?.let { fecha -> formatearFecha(fecha) }
                                    ?: "No especificada"
                            } ?: "No disponible",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                /**
                 * Sección de observaciones
                 * ------------------------
                 * ✔ TIPO: bloque condicional (let) + fila de observaciones
                 * Es la sección que muestra las notas adicionales del cliente si existen.
                 * Sirve para anotar detalles relevantes que no encajan en los demás campos.
                 */
                cliente?.observaciones?.takeIf { it.isNotBlank() }?.let { observaciones ->
                    var mostrarDialogObservaciones by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mostrarDialogObservaciones = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = "Icono de observaciones",
                            tint = Color(0xFF78909C),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Observaciones",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = observaciones,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1
                            )
                        }
                    }

                    if (mostrarDialogObservaciones) {
                        AlertDialog(
                            onDismissRequest = { mostrarDialogObservaciones = false },
                            title = { Text("Observaciones") },
                            text = {
                                Text(
                                    text = observaciones,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            confirmButton = {
                                AppDialogTextButton(
                                    text = "Cerrar",
                                    onClick = { mostrarDialogObservaciones = false }
                                )
                            }
                        )
                    }
                }

                }

                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Servicios contratados",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                /**
                 * Servicios contratados del cliente
                 * ----------------------------------
                 * Lista dinámica de los servicios contratados (resuelve los ids
                 * de serviciosContratados contra ServicioEntity). Solo nombres.
                 */
                val serviciosContratados = cliente?.serviciosContratados.orEmpty()
                val nombresServicios = serviciosContratados.mapNotNull { serviciosMap[it] }

                if (nombresServicios.isEmpty()) {
                    Text(
                        text = "No tienes servicios contratados.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                } else {
                    nombresServicios.forEach { nombreServicio ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = nombreServicio,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                /**
                 * Button de Editar servicios
                 * --------------------------
                 * Abre el diálogo para seleccionar los servicios activos que
                 * tiene contratados este cliente.
                 */
                AppSecondaryButton(
                    text = "Editar servicios",
                    onClick = { mostrarDialogoServicios = true }
                )

                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                /**
                 * Button de Modificar cliente
                 * ---------------------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Button)
                 * Es el botón que abre el formulario de edición del cliente.
                 * Sirve para navegar a la pantalla de modificar con los datos precargados,
                 * con el mismo estilo azul que el botón de guardar del formulario.
                 */
                AppSecondaryButton(
                    text = "Modificar cliente",
                    onClick = {
                        navController.navigate(Routes.modificarCliente(idCliente))
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp)
                )

                val esArchivado = cliente?.estado == EstadoCliente.ARCHIVADO

                androidx.compose.material3.OutlinedButton(
                    onClick = { mostrarConfirmarArchivar = true },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (esArchivado) Color(0xFF4CAF50) else Color(0xFFE53935)
                    )
                ) {
                    Text(if (esArchivado) "Restaurar cliente" else "Archivar cliente")
                }
            }else {
                /**
                 * Sección de Economía
                 * -------------------
                 * ✔ TIPO: bloque else de la pestaña de Economía
                 * Es la sección que muestra los movimientos económicos del cliente.
                 * Sirve para listar servicios contratados, su estado de pago y añadir nuevos movimientos.
                 */

                /**
                 * Button de Nuevo movimiento
                 * -------------------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Button)
                 * Es el botón que abre el formulario para añadir un nuevo movimiento.
                 * Sirve para registrar un nuevo servicio contratado por el cliente.
                 */
                AppPrimaryButton(
                    text = "Nuevo movimiento",
                    onClick = {
                        abrirFormularioNuevoMovimiento()
                    }
                )

                /**
                 * Button de Renovar último movimiento
                 * -----------------------------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Button)
                 * Es el botón que abre el diálogo de renovación del último movimiento del cliente.
                 * Sirve para crear un nuevo periodo copiando el servicio y precio del movimiento
                 * con fecha de fin más reciente, eligiendo si empieza hoy o al terminar aquel.
                 * Se desactiva cuando el cliente no tiene movimientos registrados.
                 */
                AppSecondaryButton(
                    text = "Renovar",
                    onClick = {
                        val ultimo = movimientos.maxByOrNull { it.fechaFin }
                        if (ultimo != null) {
                            movimientoARenovar = ultimo
                            renovarDesdeHoy = ultimo.fechaFin < System.currentTimeMillis()
                            mostrarDialogoRenovar = true
                        }
                    },
                    enabled = movimientos.isNotEmpty()
                )

                /**
                 * Contenido de movimientos
                 * ------------------------
                 * ✔ TIPO: bloque condicional (if/else) que muestra la lista o un aviso vacío.
                 * Sirve para mostrar los movimientos del cliente o indicar que no hay ninguno registrado.
                 */
                if (movimientos.isEmpty()) {

                    /**
                     * Texto de lista vacía
                     * --------------------
                     * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
                     * Es el mensaje que se muestra cuando el cliente no tiene movimientos.
                     * Sirve para informar al usuario de que aún no se ha registrado ningún servicio.
                     */
                    Text(
                        text = "No hay movimientos registrados",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center
                    )

                } else {

                    /**
                     * Lista de movimientos
                     * --------------------
                     * ✔ TIPO: bloque forEach que recorre la lista de movimientos.
                     * Sirve para crear un MovimientoItem por cada servicio contratado por el cliente.
                     */
                    movimientos.forEach { movimiento ->
                        ItemMovimientoPerfil(
                            movimiento = movimiento,
                            nombreServicios = movimiento.servicios
                                .mapNotNull { serviciosMap[it] }
                                .joinToString(" + ")
                                .ifBlank { "Sin servicio asociado" },
                            onClick = {
                                movimientoSeleccionado = movimiento
                            }
                        )
                    }
                }

                /**
                 * Diálogo de renovación de movimiento
                 * -----------------------------------
                 * ✔ TIPO: bloque condicional (if) + @Composable (Dialog)
                 * Es el diálogo que muestra los datos de la renovación antes de crearla.
                 * Sirve para elegir si el nuevo periodo empieza hoy o en la fecha de fin
                 * del último movimiento, mostrando las fechas calculadas en vivo.
                 */
                val aRenovar = movimientoARenovar
                if (mostrarDialogoRenovar && aRenovar != null) {

                    /**
                     * ahoraRenovacion / duracionRenovacion
                     * ------------------------------------
                     * ✔ TIPO: variables locales (val) → Long
                     * Son la hora actual y la duración en milisegundos del periodo del último movimiento.
                     * Sirven para calcular y mostrar las fechas resultantes de cada opción del diálogo.
                     */
                    val ahoraRenovacion = System.currentTimeMillis()
                    val duracionRenovacion = aRenovar.fechaFin - aRenovar.fechaInicio
                    val nombreServiciosRenovar = aRenovar.servicios
                        .mapNotNull { serviciosMap[it] }
                        .joinToString(" + ")
                        .ifBlank { "Sin servicio asociado" }

                    Dialog(
                        onDismissRequest = {
                            mostrarDialogoRenovar = false
                        }
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 8.dp
                        ) {

                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                Text(
                                    text = "Renovar movimiento",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color(0xFF1E88E5),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "$nombreServiciosRenovar · ${"%.2f".format(aRenovar.precioFinal)} €",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Duración del periodo: ${duracionRenovacion / (1000 * 60 * 60 * 24)} días",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )

                                /**
                                 * Opción 1: continuar desde el fin del último periodo
                                 * ---------------------------------------------------
                                 * ✔ TIPO: fila @Composable (Row) con RadioButton
                                 * Es la opción que continúa el periodo exactamente donde terminó el último movimiento.
                                 * Sirve para mantener la continuidad de periodos del cliente.
                                 */
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { renovarDesdeHoy = false },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = !renovarDesdeHoy,
                                        onClick = { renovarDesdeHoy = false }
                                    )
                                    Column {
                                        Text(
                                            text = "Continuar",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "${formatearFecha(aRenovar.fechaFin)} → ${formatearFecha(aRenovar.fechaFin + duracionRenovacion)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                /**
                                 * Opción 2: empezar hoy
                                 * ---------------------
                                 * ✔ TIPO: fila @Composable (Row) con RadioButton
                                 * Es la opción que inicia el nuevo periodo en la fecha actual.
                                 * Sirve para renovar un cliente cuyo periodo anterior ya venció.
                                 */
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { renovarDesdeHoy = true },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = renovarDesdeHoy,
                                        onClick = { renovarDesdeHoy = true }
                                    )
                                    Column {
                                        Text(
                                            text = "Desde hoy",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "${formatearFecha(ahoraRenovacion)} → ${formatearFecha(ahoraRenovacion + duracionRenovacion)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                if (duracionRenovacion <= 0) {
                                    Text(
                                        text = "El último movimiento tiene un periodo no válido y no se puede renovar.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Red,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppDialogTextButton(
                                        text = "Cancelar",
                                        onClick = {
                                            mostrarDialogoRenovar = false
                                        }
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    AppDialogTextButton(
                                        text = "Aceptar",
                                        enabled = duracionRenovacion > 0,
                                        onClick = {
                                            movimientoViewModel.renovarMovimiento(aRenovar, renovarDesdeHoy)
                                            mostrarDialogoRenovar = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                /**
                 * Diálogo de nuevo movimiento
                 * ---------------------------
                 * ✔ TIPO: bloque condicional (if) + @Composable (Dialog)
                 * Es el diálogo que se muestra para añadir un nuevo movimiento.
                 * Sirve para capturar los datos del servicio antes de guardarlo en la base de datos.
                 */
                if (mostrarFormularioMovimiento) {
                    Dialog(
                        onDismissRequest = {
                            mostrarFormularioMovimiento = false
                        }
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 8.dp
                        ) {

                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                Text(
                                    text = "Nuevo movimiento",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color(0xFF1E88E5),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Servicios del movimiento",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (serviciosActivos.isEmpty()) {
                                    Text(
                                        text = "No hay servicios activos para añadir",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                } else {
                                    serviciosActivos.forEach { servicio ->
                                        val marcado = servicio.idServicio in idsServiciosMovimiento
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    alternarServicioNuevo(servicio.idServicio)
                                                }
                                                .padding(vertical = 4.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = marcado,
                                                onCheckedChange = {
                                                    alternarServicioNuevo(servicio.idServicio)
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = servicio.nombre,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = MovimientoPrecio.importeLegible(servicio.precio),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                if (errorServicioMovimiento) {
                                    Text(
                                        text = "Selecciona al menos un servicio",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                OutlinedTextField(
                                    value = precioMovimiento,
                                    onValueChange = {
                                        precioMovimiento = it
                                        precioMovimientoManual = true
                                        errorPrecioMovimiento = false
                                    },
                                    label = {
                                        Text("Precio final (€)")
                                    },
                                    placeholder = { Text("Ej: 50") },
                                    isError = errorPrecioMovimiento,
                                    supportingText = {
                                        if (errorPrecioMovimiento) {
                                            Text("Introduce un precio válido (0 o mayor)")
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (precioMovimientoManual) {
                                    AppTextLinkButton(
                                        text = "Usar precio calculado",
                                        onClick = {
                                            precioMovimiento = MovimientoPrecio.precioCampo(
                                                MovimientoPrecio.precioSugerido(
                                                    serviciosActivos.filter {
                                                        it.idServicio in idsServiciosMovimiento
                                                    }
                                                )
                                            )
                                            precioMovimientoManual = false
                                            errorPrecioMovimiento = false
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }

                                OutlinedTextField(
                                    value = fechaInicioFormateada,
                                    onValueChange = { },
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Fecha de inicio") },
                                    placeholder = { Text("dd/MM/aaaa") },
                                    isError = errorFechaInicioMovimiento,
                                    supportingText = {
                                        if (errorFechaInicioMovimiento) {
                                            Text("La fecha de inicio es obligatoria")
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledContainerColor = Color.Transparent,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Seleccionar fecha de inicio"
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            mostrarDatePickerInicio = true
                                        }
                                )

                                OutlinedTextField(
                                    value = fechaFinFormateada,
                                    onValueChange = { },
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Fecha de fin") },
                                    placeholder = { Text("dd/MM/aaaa") },
                                    isError = errorFechaFinMovimiento,
                                    supportingText = {
                                        if (errorFechaFinMovimiento) {
                                            Text("La fecha de fin es obligatoria")
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledContainerColor = Color.Transparent,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Seleccionar fecha de fin"
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            mostrarDatePickerFin = true
                                        }
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Pago realizado"
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    Switch(
                                        checked = movimientoPagado,
                                        onCheckedChange = { activar ->
                                            movimientoPagado = activar
                                            if (activar && fechaPagoMovimiento == null) {
                                                fechaPagoMovimiento = System.currentTimeMillis()
                                            }
                                        }
                                    )
                                }

                                if (movimientoPagado) {
                                    OutlinedTextField(
                                        value = fechaPagoFormateada,
                                        onValueChange = { },
                                        readOnly = true,
                                        enabled = false,
                                        label = { Text("Fecha de pago") },
                                        placeholder = { Text("dd/MM/aaaa") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledContainerColor = Color.Transparent,
                                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Seleccionar fecha de pago"
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                mostrarDatePickerPago = true
                                            }
                                    )

                                    SelectorMetodoPago(
                                        nombre = metodoPagoMovimientoNombre,
                                        onCambio = { metodoPagoMovimientoNombre = it }
                                    )
                                }

                                OutlinedTextField(
                                    value = observacionesMovimiento,
                                    onValueChange = {
                                        observacionesMovimiento = it
                                    },
                                    label = {
                                        Text("Observaciones")
                                    },
                                    modifier = Modifier.fillMaxWidth()

                                )

                                AppPrimaryButton(
                                    text = "Guardar movimiento",
                                    onClick = {

                                        errorServicioMovimiento = idsServiciosMovimiento.isEmpty()

                                        val precioValido = precioMovimiento
                                            .replace(",", ".")
                                            .toDoubleOrNull()

                                        errorPrecioMovimiento =
                                            precioValido == null || precioValido < 0

                                        errorFechaInicioMovimiento =
                                            fechaInicioMovimiento == null

                                        errorFechaFinMovimiento =
                                            fechaFinMovimiento == null

                                        val fechasValidas =
                                            fechaInicioMovimiento != null &&
                                                    fechaFinMovimiento != null &&
                                                    fechaFinMovimiento!! >= fechaInicioMovimiento!!

                                        if (!fechasValidas) {
                                            errorFechaFinMovimiento = true
                                        }

                                        if (
                                            !errorServicioMovimiento &&
                                            !errorPrecioMovimiento &&
                                            !errorFechaInicioMovimiento &&
                                            !errorFechaFinMovimiento &&
                                            fechasValidas
                                        ) {

                                            val pagoNuevo = MovimientoPago.resolver(
                                                nuevoPagado = movimientoPagado,
                                                eraPagado = false,
                                                fechaPagoElegida = fechaPagoMovimiento,
                                                metodoPago = MovimientoPago.metodoPagoDe(
                                                    metodoPagoMovimientoNombre
                                                ),
                                                ahora = System.currentTimeMillis()
                                            )

                                            val movimiento = MovimientoEntity(
                                                idCliente = idCliente,
                                                servicios = idsServiciosMovimiento.distinct(),
                                                fechaInicio = fechaInicioMovimiento!!,
                                                fechaFin = fechaFinMovimiento!!,
                                                precioFinal = precioValido!!,
                                                estado = pagoNuevo.estado,
                                                fechaPago = pagoNuevo.fechaPago,
                                                metodoPago = pagoNuevo.metodoPago,
                                                observaciones = observacionesMovimiento.ifBlank {
                                                    null
                                                }
                                            )

                                            // NOTA (Fase 3): crear un movimiento NO modifica
                                            // los serviciosContratados del cliente (son
                                            // conceptos independientes).
                                            movimientoViewModel.insertarMovimiento(movimiento)

                                            mostrarFormularioMovimiento = false

                                            idsServiciosMovimiento = emptyList()
                                            precioMovimiento = ""
                                            precioMovimientoManual = false
                                            fechaInicioMovimiento = null
                                            fechaFinMovimiento = null
                                            movimientoPagado = false
                                            fechaPagoMovimiento = null
                                            metodoPagoMovimientoNombre = null
                                            observacionesMovimiento = ""
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                if (mostrarDatePickerInicio) {

                    val selectableDatesInicio = remember {
                        val hoy = LocalDate.now()
                        val fechaMinimaUtc = hoy.minusYears(120).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                        object : SelectableDates {
                            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                                utcTimeMillis >= fechaMinimaUtc

                            override fun isSelectableYear(year: Int): Boolean =
                                year >= hoy.minusYears(120).year
                        }
                    }

                    val datePickerState = rememberDatePickerState(
                        selectableDates = selectableDatesInicio
                    )

                    DatePickerDialog(
                        onDismissRequest = {
                            mostrarDatePickerInicio = false
                        },
                        confirmButton = {
                            TextButton(
                                enabled = datePickerState.selectedDateMillis != null,
                                onClick = {
                                    fechaInicioMovimiento =
                                        datePickerState.selectedDateMillis

                                    mostrarDatePickerInicio = false
                                }
                            ) {
                                Text("Aceptar")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    mostrarDatePickerInicio = false
                                }
                            ) {
                                Text("Cancelar")
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState
                        )
                    }
                }

                if (mostrarDatePickerFin) {

                    val selectableDatesFin = remember {
                        val hoy = LocalDate.now()
                        val fechaInicioUtc = fechaInicioMovimiento
                            ?: hoy.minusYears(120).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                        object : SelectableDates {
                            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                                utcTimeMillis >= fechaInicioUtc

                            override fun isSelectableYear(year: Int): Boolean =
                                year >= hoy.minusYears(120).year
                        }
                    }

                    val datePickerState = rememberDatePickerState(
                        selectableDates = selectableDatesFin
                    )

                    DatePickerDialog(
                        onDismissRequest = {
                            mostrarDatePickerFin = false
                        },
                        confirmButton = {
                            TextButton(
                                enabled = datePickerState.selectedDateMillis != null,
                                onClick = {
                                    fechaFinMovimiento =
                                        datePickerState.selectedDateMillis

                                    mostrarDatePickerFin = false
                                }
                            ) {
                                Text("Aceptar")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    mostrarDatePickerFin = false
                                }
                            ) {
                                Text("Cancelar")
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState
                        )
                    }
                }

                if (mostrarDatePickerPago) {

                    val selectableDatesPago = remember {
                        val hoy = LocalDate.now()
                        val fechaMinimaUtc = hoy.minusYears(120).atStartOfDay(ZoneOffset.UTC)
                            .toInstant().toEpochMilli()
                        object : SelectableDates {
                            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                                utcTimeMillis >= fechaMinimaUtc

                            override fun isSelectableYear(year: Int): Boolean =
                                year >= hoy.minusYears(120).year
                        }
                    }

                    val datePickerStatePago = rememberDatePickerState(
                        selectableDates = selectableDatesPago
                    )

                    DatePickerDialog(
                        onDismissRequest = {
                            mostrarDatePickerPago = false
                        },
                        confirmButton = {
                            TextButton(
                                enabled = datePickerStatePago.selectedDateMillis != null,
                                onClick = {
                                    fechaPagoMovimiento =
                                        datePickerStatePago.selectedDateMillis
                                    mostrarDatePickerPago = false
                                }
                            ) {
                                Text("Aceptar")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    mostrarDatePickerPago = false
                                }
                            ) {
                                Text("Cancelar")
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerStatePago
                        )
                    }
                }

                /* ============================================================
                 * ============ DIÁLOGO DETALLE DEL MOVIMIENTO ================
                 * ============================================================ */
                if (movimientoSeleccionado != null) {

                    val fechaInicioFormateadaDetalle =
                        fechaInicioEditada?.let { formatearFecha(it) } ?: ""

                    val fechaFinFormateadaDetalle =
                        fechaFinEditada?.let { formatearFecha(it) } ?: ""

                    Dialog(
                        onDismissRequest = {
                            movimientoSeleccionado = null
                        }
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            shadowElevation = 8.dp
                        ) {

                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                Text(
                                    text = "Detalle",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color(0xFF1E88E5),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )

                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Servicios del movimiento",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val nombresMovimientoDetalle = movimientoSeleccionado?.servicios
                                        ?.mapNotNull { serviciosMap[it] }
                                        ?.joinToString(" + ")
                                        .orEmpty()
                                    Text(
                                        text = if (nombresMovimientoDetalle.isBlank()) {
                                            "Sin servicio asociado"
                                        } else {
                                            nombresMovimientoDetalle
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                if (serviciosActivos.isEmpty() && idsServiciosEditadosFijos.isEmpty()) {
                                    Text(
                                        text = "No hay servicios activos para añadir",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                                serviciosActivos.forEach { servicio ->
                                    val marcado = servicio.idServicio in idsServiciosEditados
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                alternarServicioEditado(servicio.idServicio)
                                            }
                                            .padding(vertical = 4.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = marcado,
                                            onCheckedChange = {
                                                alternarServicioEditado(servicio.idServicio)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = servicio.nombre,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = MovimientoPrecio.importeLegible(servicio.precio),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (idsServiciosEditadosFijos.isNotEmpty()) {
                                    Text(
                                        text = "Servicios dados de baja (se conservan)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                    idsServiciosEditadosFijos.forEach { idServicio ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp, horizontal = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = true,
                                                onCheckedChange = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = serviciosMap[idServicio]
                                                    ?: "Servicio $idServicio",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color.Gray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = precioEditado,
                                    onValueChange = {
                                        precioEditado = it
                                        errorPrecioEditado = false
                                    },
                                    label = { Text("Precio final (€)") },
                                    isError = errorPrecioEditado,
                                    supportingText = {
                                        if (errorPrecioEditado) {
                                            Text("Introduce un precio válido (0 o mayor)")
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = fechaInicioFormateadaDetalle,
                                    onValueChange = { },
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Fecha de inicio") },
                                    placeholder = { Text("dd/MM/aaaa") },
                                    isError = errorFechaInicioEditada,
                                    supportingText = {
                                        if (errorFechaInicioEditada) {
                                            Text("La fecha de inicio es obligatoria")
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledContainerColor = Color.Transparent,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Seleccionar fecha de inicio"
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            mostrarDatePickerInicioDetalle = true
                                        }
                                )

                                OutlinedTextField(
                                    value = fechaFinFormateadaDetalle,
                                    onValueChange = { },
                                    readOnly = true,
                                    enabled = false,
                                    label = { Text("Fecha de fin") },
                                    placeholder = { Text("dd/MM/aaaa") },
                                    isError = errorFechaFinEditada,
                                    supportingText = {
                                        if (errorFechaFinEditada) {
                                            Text("La fecha de fin es obligatoria")
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledContainerColor = Color.Transparent,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Seleccionar fecha de fin"
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            mostrarDatePickerFinDetalle = true
                                        }
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Pago realizado")
                                    Spacer(modifier = Modifier.weight(1f))
                                    Switch(
                                        checked = pagadoEditado,
                                        onCheckedChange = { activar ->
                                            pagadoEditado = activar
                                            // Al pasar a PAGADO se propone hoy como fecha de
                                            // pago (el ADMIN puede modificarla después).
                                            if (activar) {
                                                fechaPagoEditada = System.currentTimeMillis()
                                            }
                                        }
                                    )
                                }

                                if (pagadoEditado) {
                                    OutlinedTextField(
                                        value = fechaPagoEditada?.let { formatearFecha(it) } ?: "",
                                        onValueChange = { },
                                        readOnly = true,
                                        enabled = false,
                                        label = { Text("Fecha de pago") },
                                        placeholder = { Text("dd/MM/aaaa") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                            disabledContainerColor = Color.Transparent,
                                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Seleccionar fecha de pago"
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                mostrarDatePickerPagoDetalle = true
                                            }
                                    )

                                    SelectorMetodoPago(
                                        nombre = metodoPagoEditadoNombre,
                                        onCambio = { metodoPagoEditadoNombre = it }
                                    )
                                }

                                OutlinedTextField(
                                    value = observacionesEditadas,
                                    onValueChange = {
                                        observacionesEditadas = it
                                    },
                                    label = { Text("Observaciones") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                AppPrimaryButton(
                                    onClick = {

                                        val precioValido = precioEditado
                                            .replace(",", ".")
                                            .toDoubleOrNull()

                                        errorPrecioEditado =
                                            precioValido == null || precioValido < 0

                                        errorFechaInicioEditada =
                                            fechaInicioEditada == null

                                        errorFechaFinEditada =
                                            fechaFinEditada == null

                                        val fechasValidas =
                                            fechaInicioEditada != null &&
                                                    fechaFinEditada != null &&
                                                    fechaFinEditada!! >= fechaInicioEditada!!

                                        if (!fechasValidas) {
                                            errorFechaFinEditada = true
                                        }

                                        if (
                                            !errorPrecioEditado &&
                                            !errorFechaInicioEditada &&
                                            !errorFechaFinEditada &&
                                            fechasValidas
                                        ) {
                                            val pagoEditadoResuelto = MovimientoPago.resolver(
                                                nuevoPagado = pagadoEditado,
                                                eraPagado = movimientoSeleccionado!!.estado ==
                                                    EstadoMovimiento.PAGADO,
                                                fechaPagoElegida = fechaPagoEditada,
                                                metodoPago = MovimientoPago.metodoPagoDe(
                                                    metodoPagoEditadoNombre
                                                ),
                                                ahora = System.currentTimeMillis()
                                            )

                                            val movimientoActualizado = MovimientoEntity(
                                                idMovimiento = movimientoSeleccionado!!.idMovimiento,
                                                idCliente = movimientoSeleccionado!!.idCliente,
                                                // Servicios: activos marcados + fijos históricos
                                                // (de baja/eliminados) que se conservan. Si el
                                                // movimiento histórico no tenía servicios, se
                                                // mantiene la lista vacía.
                                                servicios = (idsServiciosEditados +
                                                        idsServiciosEditadosFijos).distinct(),
                                                fechaInicio = fechaInicioEditada!!,
                                                fechaFin = fechaFinEditada!!,
                                                precioFinal = precioValido!!,
                                                estado = pagoEditadoResuelto.estado,
                                                fechaPago = pagoEditadoResuelto.fechaPago,
                                                metodoPago = pagoEditadoResuelto.metodoPago,
                                                observaciones = observacionesEditadas.ifBlank {
                                                    null
                                                }
                                            )

                                            movimientoViewModel.actualizarMovimiento(
                                                movimientoActualizado
                                            )

                                            movimientoSeleccionado = null
                                        }
                                    },
                                    text = "Guardar cambios"
                                )

                                AppDangerOutlinedButton(
                                    text = "Eliminar movimiento",
                                    onClick = {
                                        mostrarConfirmarEliminar = true
                                    }
                                )
                            }
                        }
                    }

                    if (mostrarConfirmarEliminar) {
                        AlertDialog(
                            onDismissRequest = {
                                mostrarConfirmarEliminar = false
                            },
                            title = {
                                Text(
                                    text = "Eliminar movimiento",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            text = {
                                Text("¿Seguro que quieres eliminar este movimiento? Esta acción no se puede deshacer.")
                            },
                            confirmButton = {
                                AppDialogDangerConfirmButton(
                                    text = "Eliminar",
                                    onClick = {
                                        movimientoViewModel.eliminarMovimiento(
                                            movimientoSeleccionado!!
                                        )
                                        mostrarConfirmarEliminar = false
                                        movimientoSeleccionado = null
                                    }
                                )
                            },
                            dismissButton = {
                                AppDialogTextButton(
                                    text = "Cancelar",
                                    onClick = {
                                        mostrarConfirmarEliminar = false
                                    }
                                )
                            }
                        )
                    }

                    /* ============================================================
                     * ============ DATEPICKERS DEL DIÁLOGO DETALLE ==============
                     * ============================================================ */
                    if (mostrarDatePickerInicioDetalle) {

                        val selectableDatesInicioDetalle = remember {
                            val hoy = LocalDate.now()
                            val fechaMinimaUtc = hoy.minusYears(120)
                                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                            object : SelectableDates {
                                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                                    utcTimeMillis >= fechaMinimaUtc

                                override fun isSelectableYear(year: Int): Boolean =
                                    year >= hoy.minusYears(120).year
                            }
                        }

                        val datePickerStateInicioDetalle = rememberDatePickerState(
                            selectableDates = selectableDatesInicioDetalle
                        )

                        DatePickerDialog(
                            onDismissRequest = {
                                mostrarDatePickerInicioDetalle = false
                            },
                            confirmButton = {
                                TextButton(
                                    enabled = datePickerStateInicioDetalle.selectedDateMillis != null,
                                    onClick = {
                                        fechaInicioEditada =
                                            datePickerStateInicioDetalle.selectedDateMillis
                                        mostrarDatePickerInicioDetalle = false
                                    }
                                ) {
                                    Text("Aceptar")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        mostrarDatePickerInicioDetalle = false
                                    }
                                ) {
                                    Text("Cancelar")
                                }
                            }
                        ) {
                            DatePicker(
                                state = datePickerStateInicioDetalle
                            )
                        }
                    }

                    if (mostrarDatePickerFinDetalle) {

                        val selectableDatesFinDetalle = remember {
                            val hoy = LocalDate.now()
                            val fechaInicioUtcDetalle = fechaInicioEditada
                                ?: hoy.minusYears(120).atStartOfDay(ZoneOffset.UTC)
                                    .toInstant().toEpochMilli()
                            object : SelectableDates {
                                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                                    utcTimeMillis >= fechaInicioUtcDetalle

                                override fun isSelectableYear(year: Int): Boolean =
                                    year >= hoy.minusYears(120).year
                            }
                        }

                        val datePickerStateFinDetalle = rememberDatePickerState(
                            selectableDates = selectableDatesFinDetalle
                        )

                        DatePickerDialog(
                            onDismissRequest = {
                                mostrarDatePickerFinDetalle = false
                            },
                            confirmButton = {
                                TextButton(
                                    enabled = datePickerStateFinDetalle.selectedDateMillis != null,
                                    onClick = {
                                        fechaFinEditada =
                                            datePickerStateFinDetalle.selectedDateMillis
                                        mostrarDatePickerFinDetalle = false
                                    }
                                ) {
                                    Text("Aceptar")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        mostrarDatePickerFinDetalle = false
                                    }
                                ) {
                                    Text("Cancelar")
                                }
                            }
                        ) {
                            DatePicker(
                                state = datePickerStateFinDetalle
                            )
                        }
                    }

                    if (mostrarDatePickerPagoDetalle) {

                        val selectableDatesPagoDetalle = remember {
                            val hoy = LocalDate.now()
                            val fechaMinimaUtc = hoy.minusYears(120).atStartOfDay(ZoneOffset.UTC)
                                .toInstant().toEpochMilli()
                            object : SelectableDates {
                                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                                    utcTimeMillis >= fechaMinimaUtc

                                override fun isSelectableYear(year: Int): Boolean =
                                    year >= hoy.minusYears(120).year
                            }
                        }

                        val datePickerStatePagoDetalle = rememberDatePickerState(
                            selectableDates = selectableDatesPagoDetalle
                        )

                        DatePickerDialog(
                            onDismissRequest = {
                                mostrarDatePickerPagoDetalle = false
                            },
                            confirmButton = {
                                TextButton(
                                    enabled = datePickerStatePagoDetalle.selectedDateMillis != null,
                                    onClick = {
                                        fechaPagoEditada =
                                            datePickerStatePagoDetalle.selectedDateMillis
                                        mostrarDatePickerPagoDetalle = false
                                    }
                                ) {
                                    Text("Aceptar")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        mostrarDatePickerPagoDetalle = false
                                    }
                                ) {
                                    Text("Cancelar")
                                }
                            }
                        ) {
                            DatePicker(
                                state = datePickerStatePagoDetalle
                            )
                        }
                    }
                }

            }
        }

        if (mostrarDialogoServicios) {
            val clienteActual = cliente
            if (clienteActual != null) {
                DialogoEditarServiciosContratados(
                    contratadosActuales = clienteActual.serviciosContratados,
                    serviciosActivos = serviciosActivos,
                    onDismiss = { mostrarDialogoServicios = false },
                    onGuardar = { ids ->
                        viewModel.guardarServiciosContratados(idCliente, ids)
                        mostrarDialogoServicios = false
                        viewModel.obtenerClientePorId(idCliente)
                    }
                )
            }
        }

        if (mostrarConfirmarArchivar) {
            val esArchivado = cliente?.estado == EstadoCliente.ARCHIVADO
            AlertDialog(
                onDismissRequest = { mostrarConfirmarArchivar = false },
                title = { Text(if (esArchivado) "Restaurar cliente" else "Archivar cliente") },
                text = {
                    Text(
                        if (esArchivado) {
                            "¿Seguro que quieres restaurar a ${cliente?.nombre}? Volverá a aparecer en la lista principal como activo."
                        } else {
                            "¿Seguro que quieres archivar a ${cliente?.nombre}? No aparecerá en la lista principal, pero podrás restaurarlo más adelante."
                        }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        mostrarConfirmarArchivar = false
                        cliente?.let {
                            if (esArchivado) {
                                viewModel.restaurarCliente(it)
                            } else {
                                viewModel.archivarCliente(it)
                            }
                        }
                    }) {
                        Text(
                            if (esArchivado) "Restaurar" else "Archivar",
                            color = if (esArchivado) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfirmarArchivar = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

/**
 * ItemMovimientoPerfil
 * --------------------
 * ✔ TIPO: función @Composable privada (componente local)
 * Renderiza cada movimiento del cliente en la pestaña Economía con el mismo
 * lenguaje visual que EconomiaScreen (ItemMovimiento): tarjeta horizontal,
 * fondo verde muy suave, icono a la izquierda, servicio y fecha en el centro
 * e importe a la derecha. Es visualmente coherente con el área económica.
 */
@Composable
private fun ItemMovimientoPerfil(
    movimiento: MovimientoEntity,
    nombreServicios: String,
    onClick: () -> Unit
) {
    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "ES"))
    val fechaFinFormateada = java.time.Instant.ofEpochMilli(movimiento.fechaFin)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AttachMoney,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombreServicios,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                Text(
                    text = fechaFinFormateada,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                text = "+${formatter.format(movimiento.precioFinal)}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

/**
 * formatearFecha
 * --------------
 * ✔ TIPO: función privada (private fun) → String
 * Es la función que convierte un timestamp en milisegundos a texto legible.
 * Sirve para mostrar fechas con formato dd/MM/aaaa (nacimiento del cliente y fechas de movimiento).
 */
private fun formatearFecha(millis: Long): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

/**
 * SelectorMetodoPago
 * ------------------
 * Selector sencillo de método de pago opcional (FASE 4). Muestra un botón con
 * el valor actual y un menú desplegable con:
 *   - Sin especificar (null)
 *   - EFECTIVO / BIZUM / TRANSFERENCIA
 * No obliga a seleccionar nada (el método es opcional).
 */
@Composable
private fun SelectorMetodoPago(
    nombre: String?,
    onCambio: (String?) -> Unit
) {
    var desplegado by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Método de pago",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { desplegado = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = MovimientoPago.metodoPagoLabel(
                        MovimientoPago.metodoPagoDe(nombre)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            DropdownMenu(
                expanded = desplegado,
                onDismissRequest = { desplegado = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Sin especificar") },
                    onClick = {
                        onCambio(null)
                        desplegado = false
                    }
                )
                MetodoPago.entries.forEach { metodo ->
                    DropdownMenuItem(
                        text = { Text(metodo.name) },
                        onClick = {
                            onCambio(metodo.name)
                            desplegado = false
                        }
                    )
                }
            }
        }
    }
}
