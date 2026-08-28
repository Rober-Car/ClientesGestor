package com.roberto.gestorpro.ui.clases

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.data.entity.ClaseEntity
import com.roberto.gestorpro.data.entity.SesionClaseEntity
import com.roberto.gestorpro.navigation.Routes
import com.roberto.gestorpro.ui.viewmodel.ClaseViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleClaseScreen(
    navController: NavHostController,
    idClase: Int,
    viewModel: ClaseViewModel = hiltViewModel()
) {
    LaunchedEffect(idClase) {
        viewModel.cargarDetalleClase(idClase)
    }

    val clase by viewModel.claseSeleccionada.collectAsStateWithLifecycle()
    val sesiones by viewModel.sesiones.collectAsStateWithLifecycle()
    val reservasPorSesion by viewModel.reservasPorSesion.collectAsStateWithLifecycle()
    val clientesMap by viewModel.clientesMap.collectAsStateWithLifecycle()

    var editando by rememberSaveable { mutableStateOf(false) }
    var mostrarEliminar by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogoEditar by rememberSaveable { mutableStateOf(false) }
    var sesionSeleccionada by remember { mutableStateOf<SesionClaseEntity?>(null) }

    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    Scaffold { innerPadding ->
        if (clase == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Cargando...", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            val c = clase!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = c.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { mostrarDialogoEditar = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color(0xFF1E88E5)
                        )
                    }
                    IconButton(onClick = { mostrarEliminar = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetalleCampo("Horario", "${c.horaInicio} · ${c.duracionMinutos} min")
                        DetalleCampo("Días", formatDias(c.diasSemana))
                        DetalleCampo("Plazas", "${c.capacidadMaxima}")
                        DetalleCampo("Reserva desde", c.horaAperturaReservas)
                        DetalleCampo("Inicio", Instant.ofEpochMilli(c.fechaInicio)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(formatter))
                        DetalleCampo("Fin", Instant.ofEpochMilli(c.fechaFin)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(formatter))
                        DetalleCampo("Estado", if (c.activa) "Activa" else "Inactiva")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sesiones (${sesiones.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(sesiones) { sesion ->
                        val reservas = reservasPorSesion[sesion.idSesion] ?: emptyList()
                        val fechaSesion = Instant.ofEpochMilli(sesion.fecha)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(formatter)
                        val plazasOcupadas = c.capacidadMaxima - sesion.plazasDisponibles

                        SesionCard(
                            fecha = fechaSesion,
                            plazasOcupadas = plazasOcupadas,
                            plazasTotal = c.capacidadMaxima,
                            reservas = reservas,
                            clientesMap = clientesMap,
                            onClick = { sesionSeleccionada = sesion }
                        )
                    }
                }
            }
        }
    }

    if (mostrarEliminar && clase != null) {
        AlertDialog(
            onDismissRequest = { mostrarEliminar = false },
            title = { Text("Eliminar clase") },
            text = { Text("¿Seguro que quieres eliminar esta clase y todas sus sesiones?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarClase(clase!!)
                        mostrarEliminar = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoEditar && clase != null) {
        DialogEditarClase(
            clase = clase!!,
            onDismiss = { mostrarDialogoEditar = false },
            onGuardar = { claseModificada ->
                viewModel.actualizarClase(claseModificada)
                mostrarDialogoEditar = false
            }
        )
    }

    if (sesionSeleccionada != null) {
        val reservas = reservasPorSesion[sesionSeleccionada!!.idSesion] ?: emptyList()
        val fechaSesion = Instant.ofEpochMilli(sesionSeleccionada!!.fecha)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(formatter)

        DialogDetalleSesion(
            fecha = fechaSesion,
            reservas = reservas,
            clientesMap = clientesMap,
            onDismiss = { sesionSeleccionada = null },
            onClienteClick = { idCliente ->
                sesionSeleccionada = null
                navController.navigate(Routes.perfilCliente(idCliente))
            }
        )
    }
}

@Composable
fun DetalleCampo(etiqueta: String, valor: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun SesionCard(
    fecha: String,
    plazasOcupadas: Int,
    plazasTotal: Int,
    reservas: List<com.roberto.gestorpro.data.entity.ReservaEntity>,
    clientesMap: Map<Int, String>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fecha,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${reservas.size} reservas",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                text = "$plazasOcupadas/$plazasTotal",
                style = MaterialTheme.typography.bodyMedium,
                color = if (plazasOcupadas >= plazasTotal) Color(0xFFF44336) else Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun DialogDetalleSesion(
    fecha: String,
    reservas: List<com.roberto.gestorpro.data.entity.ReservaEntity>,
    clientesMap: Map<Int, String>,
    onDismiss: () -> Unit,
    onClienteClick: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Sesión del $fecha",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (reservas.isEmpty()) {
                    Text(
                        text = "No hay reservas aún",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    reservas.forEach { reserva ->
                        val nombreCliente = clientesMap[reserva.idCliente] ?: "Cliente #${reserva.idCliente}"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onClienteClick(reserva.idCliente) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = nombreCliente,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogEditarClase(
    clase: ClaseEntity,
    onDismiss: () -> Unit,
    onGuardar: (ClaseEntity) -> Unit
) {
    var nombre by remember { mutableStateOf(clase.nombre) }
    var horaInicio by remember { mutableStateOf(clase.horaInicio) }
    var duracionMinutos by remember { mutableStateOf(clase.duracionMinutos.toString()) }
    var capacidadMaxima by remember { mutableStateOf(clase.capacidadMaxima.toString()) }
    var horaAperturaReservas by remember { mutableStateOf(clase.horaAperturaReservas) }
    var fechaFin by remember { mutableStateOf(clase.fechaFin) }
    var diasSeleccionados by remember { mutableStateOf(ClaseViewModel.parseDiasSemana(clase.diasSemana)) }
    var fechaInicio by remember { mutableStateOf(clase.fechaInicio) }
    var activa by remember { mutableStateOf(clase.activa) }
    var mostrarDatePickerInicio by remember { mutableStateOf(false) }
    var mostrarDatePickerFin by remember { mutableStateOf(false) }

    val diasSemana = listOf(
        DayOfWeek.MONDAY to "Lun",
        DayOfWeek.TUESDAY to "Mar",
        DayOfWeek.WEDNESDAY to "Mié",
        DayOfWeek.THURSDAY to "Jue",
        DayOfWeek.FRIDAY to "Vie",
        DayOfWeek.SATURDAY to "Sáb",
        DayOfWeek.SUNDAY to "Dom"
    )

    Dialog(onDismissRequest = onDismiss) {
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Editar clase",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(text = "Días", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    diasSemana.forEach { (dia, abbr) ->
                        val seleccionado = diasSeleccionados.contains(dia)
                        FilterChip(
                            selected = seleccionado,
                            onClick = {
                                diasSeleccionados = if (seleccionado) diasSeleccionados - dia else diasSeleccionados + dia
                            },
                            label = { Text(abbr, style = MaterialTheme.typography.bodySmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1E88E5),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = horaInicio,
                    onValueChange = { horaInicio = it },
                    label = { Text("Hora (HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = duracionMinutos,
                        onValueChange = { duracionMinutos = it },
                        label = { Text("Duración min") },
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = capacidadMaxima,
                        onValueChange = { capacidadMaxima = it },
                        label = { Text("Plazas") },
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = horaAperturaReservas,
                    onValueChange = { horaAperturaReservas = it },
                    label = { Text("Reserva desde (HH:mm)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                val fechaInicioStr = Instant.ofEpochMilli(fechaInicio)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                OutlinedTextField(
                    value = fechaInicioStr,
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    label = { Text("Inicio") },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color.Transparent,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarDatePickerInicio = true }
                )

                val fechaFinStr = Instant.ofEpochMilli(fechaFin)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                OutlinedTextField(
                    value = fechaFinStr,
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    label = { Text("Fin") },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color.Transparent,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarDatePickerFin = true }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onGuardar(
                                clase.copy(
                                    nombre = nombre.trim(),
                                    diasSemana = ClaseViewModel.diasSemanaToString(diasSeleccionados),
                                    horaInicio = horaInicio,
                                    duracionMinutos = duracionMinutos.toIntOrNull() ?: clase.duracionMinutos,
                                    capacidadMaxima = capacidadMaxima.toIntOrNull() ?: clase.capacidadMaxima,
                                    horaAperturaReservas = horaAperturaReservas.ifBlank { clase.horaAperturaReservas },
                                    fechaInicio = fechaInicio,
                                    fechaFin = fechaFin,
                                    activa = activa
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }

    if (mostrarDatePickerInicio) {
        val hoy = java.time.LocalDate.now()
        val selectableDates = remember {
            object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = true
                override fun isSelectableYear(year: Int): Boolean = year >= hoy.year
            }
        }
        val datePickerState = rememberDatePickerState(selectableDates = selectableDates)
        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerInicio = false },
            confirmButton = {
                TextButton(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        fechaInicio = datePickerState.selectedDateMillis!!
                        mostrarDatePickerInicio = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerInicio = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (mostrarDatePickerFin) {
        val hoy = java.time.LocalDate.now()
        val selectableDates = remember {
            object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = true
                override fun isSelectableYear(year: Int): Boolean = year >= hoy.year
            }
        }
        val datePickerState = rememberDatePickerState(selectableDates = selectableDates)
        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerFin = false },
            confirmButton = {
                TextButton(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        fechaFin = datePickerState.selectedDateMillis!!
                        mostrarDatePickerFin = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerFin = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun formatDias(dias: String): String {
    return ClaseViewModel.parseDiasSemana(dias).sortedBy { it.value }.joinToString(", ") { dia ->
        when (dia) {
            DayOfWeek.MONDAY -> "Lunes"
            DayOfWeek.TUESDAY -> "Martes"
            DayOfWeek.WEDNESDAY -> "Miércoles"
            DayOfWeek.THURSDAY -> "Jueves"
            DayOfWeek.FRIDAY -> "Viernes"
            DayOfWeek.SATURDAY -> "Sábado"
            DayOfWeek.SUNDAY -> "Domingo"
        }
    }
}
