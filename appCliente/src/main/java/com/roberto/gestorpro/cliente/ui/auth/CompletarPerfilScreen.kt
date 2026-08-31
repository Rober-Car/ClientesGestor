package com.roberto.gestorpro.cliente.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.roberto.gestorpro.cliente.data.firebase.PerfilPendiente
import com.roberto.gestorpro.cliente.navigation.Routes
import com.roberto.gestorpro.cliente.ui.components.BotonSelectorFoto
import com.roberto.gestorpro.cliente.ui.utils.crearFotoTemporal
import com.roberto.gestorpro.cliente.ui.utils.guardaFotoEnInterna
import com.roberto.gestorpro.cliente.ui.utils.guardarFotoDeCamara
import com.roberto.gestorpro.cliente.ui.utils.uriDeFotoTemporal
import com.roberto.gestorpro.cliente.ui.viewmodel.MainViewModel
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

/**
 * CompletarPerfilScreen
 * ---------------------
 * Formulario de datos personales del CLIENTE sin negocio (VÍA 2).
 * Guarda el perfil en perfiles_pendientes/{uid} y vuelve al Inicio para que el
 * cliente introduzca el código maestro + DNI.
 */
@Composable
fun CompletarPerfilScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val operandoRemoto by mainViewModel.operandoRemoto.collectAsStateWithLifecycle()
    val perfilPendiente by mainViewModel.perfilPendiente.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var nombre by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var dni by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var fechaNacimientoMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var mostrarSelectorFecha by rememberSaveable { mutableStateOf(false) }
    var foto by rememberSaveable { mutableStateOf("") }
    var mensajeError by rememberSaveable { mutableStateOf("") }
    var fotoTemporal by remember { mutableStateOf<File?>(null) }
    val scope = rememberCoroutineScope()

    // Carga el perfil pendiente existente (perfiles_pendientes/{uid}) para que
    // los campos aparezcan rellenados si el usuario ya los completó antes.
    LaunchedEffect(Unit) {
        mainViewModel.cargarPerfilPendiente()
    }

    // Prefill: solo rellena los campos que siguen vacíos (respeta lo que el
    // usuario esté tecleando en esta sesión).
    LaunchedEffect(perfilPendiente) {
        val p = perfilPendiente ?: return@LaunchedEffect
        if (nombre.isBlank()) nombre = p.nombre
        if (apellidos.isBlank()) apellidos = p.apellidos
        if (dni.isBlank()) dni = p.dni
        if (telefono.isBlank()) telefono = p.telefono
        if (email.isBlank()) email = p.email ?: ""
        if (foto.isBlank()) foto = p.foto
        if (fechaNacimientoMillis == null && p.fechaNacimiento > 0L) {
            fechaNacimientoMillis = p.fechaNacimiento
        }
    }

    val launcherFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val ruta = guardaFotoEnInterna(context, uri)
            if (ruta != null) foto = ruta
        }
    }

    val launcherTomarFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { resultado ->
        if (resultado) {
            val ruta = guardarFotoDeCamara(context, fotoTemporal)
            if (ruta != null) foto = ruta
        }
        fotoTemporal = null
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Completa tu registro",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = "Tus datos se guardarán temporalmente hasta que te vincules " +
                    "a un gimnasio con su código maestro.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Los campos marcados con * son obligatorios.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Foto del rostro *",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (foto.isNotBlank()) {
                    AsyncImage(
                        model = File(foto),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF1E88E5), CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                BotonSelectorFoto(
                    tieneFoto = foto.isNotBlank(),
                    onElegirGaleria = {
                        launcherFoto.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onHacerFoto = {
                        val temporal = crearFotoTemporal(context)
                        if (temporal != null) {
                            fotoTemporal = temporal
                            launcherTomarFoto.launch(uriDeFotoTemporal(context, temporal))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = dni,
                onValueChange = { dni = it.uppercase() },
                label = { Text("DNI *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = fechaNacimientoMillis?.let(::formatearFechaCompletarPerfil) ?: "",
                onValueChange = {},
                label = { Text("Fecha de nacimiento *") },
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                },
                supportingText = {
                    Text("Toca para abrir el calendario")
                },
                singleLine = true,
                readOnly = true,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = Color.Transparent,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarSelectorFecha = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (mensajeError.isNotBlank()) {
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    mensajeError = ""
                    scope.launch {
                        val perfil = PerfilPendiente(
                            nombre = nombre.trim(),
                            apellidos = apellidos.trim(),
                            dni = dni.trim(),
                            telefono = telefono.trim(),
                            email = email.trim().ifBlank { null },
                            foto = foto,
                            fechaNacimiento = fechaNacimientoMillis ?: 0L
                        )
                        val error = mainViewModel.guardarPerfilPendiente(perfil)
                        if (error != null) {
                            mensajeError = error
                        } else {
                            // El perfil queda guardado en perfiles_pendientes/{uid};
                            // el cliente NO está vinculado todavía. Se va al Home
                            // sin vincular (sin buscar ficha ni ejecutar vinculación).
                            navController.navigate(Routes.HOME) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                },
                enabled = !operandoRemoto &&
                    nombre.isNotBlank() && apellidos.isNotBlank() && dni.isNotBlank() &&
                    telefono.isNotBlank() && fechaNacimientoMillis != null && foto.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text("Guardar perfil", color = Color.White)
            }

            if (operandoRemoto) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }

    if (mostrarSelectorFecha) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaNacimientoMillis?.let(::fechaParaDatePicker),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= fechaMaximaParaDatePicker()
            }
        )

        DatePickerDialog(
            onDismissRequest = { mostrarSelectorFecha = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            fechaNacimientoMillis = fechaDesdeDatePicker(it)
                        }
                        mostrarSelectorFecha = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSelectorFecha = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * formatearFechaCompletarPerfil
 * -----------------------------
 * Convierte los milisegundos del perfil pendiente a texto dd/MM/aaaa para
 * rellenar el campo de fecha al reabrir la pantalla.
 */
private fun formatearFechaCompletarPerfil(millis: Long): String {
    return try {
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (_: Exception) {
        ""
    }
}

/** Convierte el epoch local almacenado al formato UTC que usa el DatePicker. */
private fun fechaParaDatePicker(millis: Long): Long =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

/** Convierte el día elegido por el DatePicker al epoch local del modelo. */
private fun fechaDesdeDatePicker(utcMillis: Long): Long =
    Instant.ofEpochMilli(utcMillis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

private fun fechaMaximaParaDatePicker(): Long =
    LocalDate.now(ZoneId.systemDefault())
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
