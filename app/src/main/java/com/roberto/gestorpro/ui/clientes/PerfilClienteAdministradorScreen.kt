package com.roberto.gestorpro.ui.clientes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Key
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.roberto.gestorpro.model.EstadoCliente
import com.roberto.gestorpro.navigation.Routes
import com.roberto.gestorpro.ui.utils.guardaFotoEnInterna
import com.roberto.gestorpro.ui.components.MovimientoItem
import com.roberto.gestorpro.ui.components.ServicioItem
import com.roberto.gestorpro.ui.viewmodel.ClienteViewModel
import com.roberto.gestorpro.ui.viewmodel.MovimientoViewModel
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.KeyboardType
import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.model.EstadoMovimiento


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
     * Indica si el cliente es moroso calculándolo desde sus movimientos.
     * Un cliente ACTIVO es moroso si tiene algún movimiento con fechaFin < ahora.
     * Un cliente de BAJA es moroso si tiene algún movimiento con estado PENDIENTE.
     * Sirve para mostrar el borde rojo en la foto del perfil.
     */
    val esMoroso = when (cliente?.estado) {
        EstadoCliente.ACTIVO -> movimientos.any { it.fechaFin < System.currentTimeMillis() }
        EstadoCliente.BAJA -> movimientos.any { it.estado == EstadoMovimiento.PENDIENTE }
        else -> false
    }

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
     * launcherFoto
     * ------------
     * ✔ TIPO: variable (val) → ActivityResultLauncher<PickVisualMediaRequest>
     * Es el lanzador que abre el selector de fotos del sistema (galería o cámara).
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
     * LaunchedEffect(fotoSeleccionada)
     * ---------------------------------
     * ✔ TIPO: efecto de composición (LaunchedEffect)
     * Se lanza cuando fotoSeleccionada cambia.
     * Sirve para limpiar el estado al navegar Away y evitar que la foto
     * temporal se conserve en sesiones posteriores inesperadamente.
     */
    LaunchedEffect(fotoSeleccionada) {
        if (fotoSeleccionada == null) {
            onDispose { }
        }
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

    var servicioMovimiento by rememberSaveable { mutableStateOf("") }
    var precioMovimiento by rememberSaveable { mutableStateOf("") }
    var observacionesMovimiento by rememberSaveable { mutableStateOf("") }
    var fechaInicioMovimiento by rememberSaveable { mutableStateOf<Long?>(null) }
    var fechaFinMovimiento by rememberSaveable { mutableStateOf<Long?>(null) }

    var mostrarDatePickerInicio by rememberSaveable { mutableStateOf(false) }
    var mostrarDatePickerFin by rememberSaveable { mutableStateOf(false) }

    val fechaInicioFormateada =
        fechaInicioMovimiento?.let { formatearFecha(it) } ?: ""

    val fechaFinFormateada =
        fechaFinMovimiento?.let { formatearFecha(it) } ?: ""

    var movimientoPagado by rememberSaveable {
        mutableStateOf(false)
    }


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
     * servicioEditado
     * ---------------
     * ✔ TIPO: variable con estado (var) → String
     * Es el campo "Servicio" editable dentro del diálogo de detalle.
     * Sirve para que el administrador pueda modificar el nombre del servicio.
     */
    var servicioEditado by rememberSaveable { mutableStateOf("") }

    /**
     * precioEditado
     * -------------
     * ✔ TIPO: variable con estado (var) → String
     * Es el campo "Precio" editable dentro del diálogo de detalle.
     * Sirve para que el administrador pueda modificar el precio del servicio.
     */
    var precioEditado by rememberSaveable { mutableStateOf("") }

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
     * errorServicioEditado
     * --------------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es el indicador de error del campo Servicio en el diálogo de detalle.
     * Sirve para resaltar el campo si está vacío al pulsar Guardar cambios.
     */
    var errorServicioEditado by rememberSaveable { mutableStateOf(false) }

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
     * LaunchedEffect(movimientoSeleccionado)
     * --------------------------------------
     * ✔ TIPO: efecto de composición (LaunchedEffect)
     * Se lanza cada vez que se selecciona un movimiento para ver su detalle.
     * Sirve para precargar los campos editables con los datos actuales del movimiento.
     */
    LaunchedEffect(movimientoSeleccionado) {
        movimientoSeleccionado?.let { mov ->
            servicioEditado = mov.servicio
            precioEditado = mov.precio.toString()
            fechaInicioEditada = mov.fechaInicio
            fechaFinEditada = mov.fechaFin
            pagadoEditado = mov.estado == EstadoMovimiento.PAGADO
            observacionesEditadas = mov.observaciones ?: ""
            errorServicioEditado = false
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Perfil de cliente",
                    style = MaterialTheme.typography.titleLarge
                )
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

            /**
             * OutlinedButton de la foto
             * -------------------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.OutlinedButton)
             * Es el botón que abre el selector de fotos del sistema.
             * Sirve para elegir la foto por primera vez o cambiarla si ya hay una seleccionada.
             */
            OutlinedButton(
                onClick = {
                    launcherFoto.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            ) {
                Text(if (fotoSeleccionada.isNotBlank()) "Cambiar foto" else "Seleccionar foto")
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
                contentColor = Color(0xFF1E88E5),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pestañaSeleccionada]),
                        height = 3.dp,
                        color = Color(0xFF1E88E5)
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
                            color = if (pestañaSeleccionada == 0) Color(0xFF1E88E5) else Color.Gray
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
                            color = if (pestañaSeleccionada == 1) Color(0xFF1E88E5) else Color.Gray
                        )
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            /**
             * OutlinedButton de vinculación en la nube
             * ---------------------------------------
             * ✔ TIPO: función @Composable (OutlinedButton)
             * Es el acceso a la gestión del enlace individual del cliente
             * (generar, compartir, regenerar o revocar su token de Vía B).
             * Sirve para abrir EnlaceVinculacionScreen con este cliente.
             */
            OutlinedButton(
                onClick = { navController.navigate(Routes.enlaceVinculacion(idCliente)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vinculación en la nube (enlace individual)")
            }

            if (pestañaSeleccionada == 0) {
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
                    Text(
                        text = cliente?.telefono ?: "No disponible",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Telefono", cliente?.telefono ?: "")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Telefono copiado", Toast.LENGTH_SHORT).show()
                            }
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = "Icono de DNI",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cliente?.dni ?: "Cargando...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Icono de email",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cliente?.email ?: "Sin email",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Email", cliente?.email ?: "")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Email copiado", Toast.LENGTH_SHORT).show()
                            }
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Icono de fecha de nacimiento",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fecha de nacimiento",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = cliente?.let { formatearFecha(it.fechaNacimiento) } ?: "No disponible",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Observaciones",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF1E88E5)
                        )
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
                                TextButton(onClick = { mostrarDialogObservaciones = false }) {
                                    Text("Cerrar")
                                }
                            }
                        )
                    }
                }

                /**
                 * Título de la sección de servicios
                 * ---------------------------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
                 * Es el encabezado de la sección de servicios contratados del cliente.
                 * Sirve para encabezar la lista de servicios con el mismo estilo del formulario.
                 */
                Text(
                    text = "Servicios contratados",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Icono de llave",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tiene llave",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (cliente?.tieneLlave == true) "Sí" else "No",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                /**
                 * ServicioItem de Sala de máquinas
                 * --------------------------------
                 * ✔ TIPO: componente @Composable (ServicioItem)
                 * Es el elemento que muestra el servicio de Sala de máquinas.
                 * Sirve para indicar que el cliente tiene contratado este servicio.
                 */
                ServicioItem(
                    nombreServicio = "Sala de máquinas",
                    iconoServicio = Icons.Default.FitnessCenter
                )

                /**
                 * ServicioItem de CrossFit
                 * -----------------------
                 * ✔ TIPO: componente @Composable (ServicioItem)
                 * Es el elemento que muestra el servicio de CrossFit.
                 * Sirve para indicar que el cliente tiene contratado este servicio.
                 */
                ServicioItem(
                    nombreServicio = "CrossFit",
                    iconoServicio = Icons.Default.Bolt
                )

                /**
                 * Button de Modificar cliente
                 * ---------------------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Button)
                 * Es el botón que abre el formulario de edición del cliente.
                 * Sirve para navegar a la pantalla de modificar con los datos precargados,
                 * con el mismo estilo azul que el botón de guardar del formulario.
                 */
                Button(
                    onClick = {
                        navController.navigate(Routes.modificarCliente(idCliente))
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E88E5),
                        contentColor = Color.White
                    )
                ) {
                    Text("Modificar cliente")
                }

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
                Button(
                    onClick = {
                        mostrarFormularioMovimiento = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E88E5),
                        contentColor = Color.White
                    )
                ) {
                    Text("Nuevo movimiento")
                }

                /**
                 * Button de Renovar último movimiento
                 * -----------------------------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Button)
                 * Es el botón que abre el diálogo de renovación del último movimiento del cliente.
                 * Sirve para crear un nuevo periodo copiando el servicio y precio del movimiento
                 * con fecha de fin más reciente, eligiendo si empieza hoy o al terminar aquel.
                 * Se desactiva cuando el cliente no tiene movimientos registrados.
                 */
                Button(
                    onClick = {
                        val ultimo = movimientos.maxByOrNull { it.fechaFin }
                        if (ultimo != null) {
                            movimientoARenovar = ultimo
                            renovarDesdeHoy = ultimo.fechaFin < System.currentTimeMillis()
                            mostrarDialogoRenovar = true
                        }
                    },
                    enabled = movimientos.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF43A047),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFBDBDBD)
                    )
                ) {
                    Text("Renovar")
                }

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
                        MovimientoItem(
                            movimiento = movimiento,
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
                                    text = "${aRenovar.servicio} · ${"%.2f".format(aRenovar.precio)} €",
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
                                    TextButton(
                                        onClick = {
                                            mostrarDialogoRenovar = false
                                        }
                                    ) {
                                        Text("Cancelar")
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            movimientoViewModel.renovarMovimiento(aRenovar, renovarDesdeHoy)
                                            mostrarDialogoRenovar = false
                                        },
                                        enabled = duracionRenovacion > 0,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF43A047),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text("Aceptar")
                                    }
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
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                Text(
                                    text = "Nuevo movimiento",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color(0xFF1E88E5),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )

                                OutlinedTextField(
                                    value = servicioMovimiento,
                                    onValueChange = {
                                        servicioMovimiento = it
                                        errorServicioMovimiento = false
                                    },
                                    label = {
                                        Text("Servicio")
                                    },
                                    isError = errorServicioMovimiento,
                                    supportingText = {
                                        if (errorServicioMovimiento) {
                                            Text("El servicio es obligatorio")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = precioMovimiento,
                                    onValueChange = {
                                        precioMovimiento = it
                                        errorPrecioMovimiento = false
                                    },
                                    label = {
                                        Text("Precio")
                                    },
                                    isError = errorPrecioMovimiento,
                                    supportingText = {
                                        if (errorPrecioMovimiento) {
                                            Text("Introduce un precio válido")
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

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
                                        onCheckedChange = {
                                            movimientoPagado = it
                                        }
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

                                Button(
                                    onClick = {

                                        errorServicioMovimiento = servicioMovimiento.isBlank()

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

                                            val movimiento = MovimientoEntity(
                                                idCliente = idCliente,
                                                servicio = servicioMovimiento,
                                                fechaInicio = fechaInicioMovimiento!!,
                                                fechaFin = fechaFinMovimiento!!,
                                                precio = precioValido!!,
                                                estado = if (movimientoPagado) {
                                                    EstadoMovimiento.PAGADO
                                                } else {
                                                    EstadoMovimiento.PENDIENTE
                                                },
                                                observaciones = observacionesMovimiento.ifBlank {
                                                    null
                                                }
                                            )

                                            movimientoViewModel.insertarMovimiento(movimiento)

                                            mostrarFormularioMovimiento = false

                                            servicioMovimiento = ""
                                            precioMovimiento = ""
                                            fechaInicioMovimiento = null
                                            fechaFinMovimiento = null
                                            movimientoPagado = false
                                            observacionesMovimiento = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E88E5),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Guardar movimiento")
                                }
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

                                OutlinedTextField(
                                    value = servicioEditado,
                                    onValueChange = {
                                        servicioEditado = it
                                        errorServicioEditado = false
                                    },
                                    label = { Text("Servicio") },
                                    isError = errorServicioEditado,
                                    supportingText = {
                                        if (errorServicioEditado) {
                                            Text("El servicio es obligatorio")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = precioEditado,
                                    onValueChange = {
                                        precioEditado = it
                                        errorPrecioEditado = false
                                    },
                                    label = { Text("Precio") },
                                    isError = errorPrecioEditado,
                                    supportingText = {
                                        if (errorPrecioEditado) {
                                            Text("Introduce un precio válido")
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
                                        onCheckedChange = {
                                            pagadoEditado = it
                                        }
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

                                Button(
                                    onClick = {
                                        errorServicioEditado = servicioEditado.isBlank()

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
                                            !errorServicioEditado &&
                                            !errorPrecioEditado &&
                                            !errorFechaInicioEditada &&
                                            !errorFechaFinEditada &&
                                            fechasValidas
                                        ) {
                                            val movimientoActualizado = MovimientoEntity(
                                                idMovimiento = movimientoSeleccionado!!.idMovimiento,
                                                idCliente = movimientoSeleccionado!!.idCliente,
                                                servicio = servicioEditado,
                                                fechaInicio = fechaInicioEditada!!,
                                                fechaFin = fechaFinEditada!!,
                                                precio = precioValido!!,
                                                estado = if (pagadoEditado) {
                                                    EstadoMovimiento.PAGADO
                                                } else {
                                                    EstadoMovimiento.PENDIENTE
                                                },
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
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E88E5),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Guardar cambios")
                                }

                                OutlinedButton(
                                    onClick = {
                                        mostrarConfirmarEliminar = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.Red
                                    )
                                ) {
                                    Text("Eliminar movimiento")
                                }
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
                                Button(
                                    onClick = {
                                        movimientoViewModel.eliminarMovimiento(
                                            movimientoSeleccionado!!
                                        )
                                        mostrarConfirmarEliminar = false
                                        movimientoSeleccionado = null
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Eliminar")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        mostrarConfirmarEliminar = false
                                    }
                                ) {
                                    Text("Cancelar")
                                }
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
                }

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
