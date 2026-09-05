package com.roberto.gestorpro.ui.servicios

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.ui.components.AppDialogTextButton
import com.roberto.gestorpro.ui.components.AppNavigationBackButton
import com.roberto.gestorpro.ui.components.AppPrimaryButton
import com.roberto.gestorpro.ui.viewmodel.SesionViewModel
import com.roberto.gestorpro.util.CapacidadSesion
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * EditarSesionScreen
 * ------------------
 * Pantalla "Ver / editar sesión": permite consultar una sesión concreta y
 * modificar su fecha, hora, apertura de reservas, duración y capacidad.
 * Las reservas existentes se conservan; si cambia la capacidad se recalculan
 * las plazas disponibles para no dejar reservas huérfanas.
 * La apertura de reservas (horaDesdeReserva) es individual de cada sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarSesionScreen(
    navController: NavHostController,
    idSesion: Int,
    viewModel: SesionViewModel = hiltViewModel()
) {
    val sesion by viewModel.sesionDetalle.collectAsStateWithLifecycle()
    val reservasActivas by viewModel.reservasActivasSesion.collectAsStateWithLifecycle()

    var fecha by remember { mutableStateOf<Long?>(null) }
    var hora by remember { mutableStateOf("") }
    var aperturaReserva by remember { mutableStateOf<String?>(null) }
    var duracion by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }
    var cargado by remember { mutableStateOf(false) }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var mostrarTimePickerApertura by remember { mutableStateOf(false) }

    var errorFecha by remember { mutableStateOf(false) }
    var errorHora by remember { mutableStateOf(false) }
    var errorDuracion by remember { mutableStateOf(false) }
    var errorCapacidad by remember { mutableStateOf(false) }

    LaunchedEffect(idSesion) {
        viewModel.cargarSesionConReservasActivas(idSesion)
    }

    LaunchedEffect(sesion) {
        if (!cargado && sesion != null) {
            val s = sesion!!
            fecha = s.fecha
            hora = s.hora
            aperturaReserva = s.horaDesdeReserva
            duracion = s.duracionMinutos.toString()
            capacidad = s.capacidad.toString()
            cargado = true
        }
    }

    val original = sesion
    // Inscritos REALES: se prefiere el conteo remoto (reservas en Firestore,
    // incluidas las creadas por appCliente). Solo si no se pudo leer se usa el
    // valor derivado de la Room local como respaldo.
    val inscritosLocal = original?.let {
        CapacidadSesion.inscritosDesdeDatosLocales(it.capacidad, it.plazasDisponibles)
    } ?: 0
    val inscritos = reservasActivas ?: inscritosLocal
    val plazasMostrar = if (original != null) {
        CapacidadSesion.plazasDisponiblesTrasCambioCapacidad(
            capacidad.toIntOrNull() ?: original.capacidad,
            inscritos
        )
    } else {
        0
    }

    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val fechaFormateada = fecha?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
    } ?: ""

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppNavigationBackButton(onClick = { navController.popBackStack() })
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Ver / editar sesión",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (original == null) {
                Text(
                    text = "Cargando sesión...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            } else {
                Text(
                    text = "Datos de la sesión",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = fechaFormateada,
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    label = { Text("Fecha") },
                    placeholder = { Text("dd/MM/aaaa") },
                    isError = errorFecha,
                    supportingText = {
                        if (errorFecha) Text("La fecha es obligatoria")
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
                        Icon(Icons.Default.DateRange, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            mostrarDatePicker = true
                            errorFecha = false
                        }
                )

                OutlinedTextField(
                    value = hora,
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    label = { Text("Hora") },
                    placeholder = { Text("HH:mm") },
                    isError = errorHora,
                    supportingText = {
                        if (errorHora) Text("Formato: HH:mm")
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
                        Icon(Icons.Default.Schedule, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            mostrarTimePicker = true
                            errorHora = false
                        }
                )

                OutlinedTextField(
                    value = aperturaReserva ?: "Desde el inicio",
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    label = { Text("Apertura de reservas") },
                    supportingText = {
                        Text(
                            text = if (aperturaReserva == null) {
                                "Los clientes pueden reservar desde el inicio del día"
                            } else {
                                "Los clientes pueden reservar a partir de esta hora"
                            }
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color.Transparent,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            mostrarTimePickerApertura = true
                        }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = duracion,
                        onValueChange = {
                            duracion = it
                            errorDuracion = false
                        },
                        label = { Text("Duración (min)") },
                        isError = errorDuracion,
                        supportingText = {
                            if (errorDuracion) Text("Inválido")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = capacidad,
                        onValueChange = {
                            capacidad = it
                            errorCapacidad = false
                        },
                        label = { Text("Capacidad") },
                        isError = errorCapacidad,
                        supportingText = {
                            if (errorCapacidad) Text("Inválido")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Text(
                    text = "Plazas disponibles: $plazasMostrar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AppPrimaryButton(
                    text = "Guardar cambios",
                    onClick = {
                        errorFecha = fecha == null
                        errorHora = !hora.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d$"))
                        errorDuracion = duracion.toIntOrNull() == null || duracion.toInt() <= 0
                        errorCapacidad = capacidad.toIntOrNull() == null || capacidad.toInt() <= 0

                        if (!errorFecha && !errorHora && !errorDuracion && !errorCapacidad) {
                            val nuevaCapacidad = capacidad.toInt()
                            val nuevasPlazas = CapacidadSesion
                                .plazasDisponiblesTrasCambioCapacidad(nuevaCapacidad, inscritos)
                            viewModel.actualizarSesion(
                                original.copy(
                                    fecha = fecha!!,
                                    hora = hora,
                                    duracionMinutos = duracion.toInt(),
                                    capacidad = nuevaCapacidad,
                                    plazasDisponibles = nuevasPlazas,
                                    horaDesdeReserva = aperturaReserva
                                )
                            )
                            navController.popBackStack()
                        }
                    }
                )
            }
        }
    }

    if (mostrarDatePicker) {
        val fechaUtcParaPicker = fecha?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val state = rememberDatePickerState(
            initialSelectedDateMillis = fechaUtcParaPicker,
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = true
                override fun isSelectableYear(year: Int): Boolean = true
            }
        )
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                AppDialogTextButton(
                    text = "Aceptar",
                    enabled = state.selectedDateMillis != null,
                    onClick = {
                        val utc = state.selectedDateMillis
                        fecha = utc?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        }
                        mostrarDatePicker = false
                    }
                )
            },
            dismissButton = {
                AppDialogTextButton(
                    text = "Cancelar",
                    onClick = { mostrarDatePicker = false }
                )
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (mostrarTimePicker) {
        val partes = hora.ifBlank { "18:00" }.split(":")
        val initialHour = partes.getOrNull(0)?.toIntOrNull() ?: 18
        val initialMinute = partes.getOrNull(1)?.toIntOrNull() ?: 0
        val timePickerState = TimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { mostrarTimePicker = false },
            confirmButton = {
                AppDialogTextButton(
                    text = "Aceptar",
                    onClick = {
                        val h = timePickerState.hour.toString().padStart(2, '0')
                        val m = timePickerState.minute.toString().padStart(2, '0')
                        hora = "$h:$m"
                        errorHora = false
                        mostrarTimePicker = false
                    }
                )
            },
            dismissButton = {
                AppDialogTextButton(
                    text = "Cancelar",
                    onClick = { mostrarTimePicker = false }
                )
            },
            text = {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hora de la sesión",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    if (mostrarTimePickerApertura) {
        val aperturaActual = aperturaReserva ?: "18:00"
        val partes = aperturaActual.split(":")
        val initialHour = partes.getOrNull(0)?.toIntOrNull() ?: 18
        val initialMinute = partes.getOrNull(1)?.toIntOrNull() ?: 0
        val timePickerState = TimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { mostrarTimePickerApertura = false },
            confirmButton = {
                AppDialogTextButton(
                    text = "Aceptar",
                    onClick = {
                        val h = timePickerState.hour.toString().padStart(2, '0')
                        val m = timePickerState.minute.toString().padStart(2, '0')
                        aperturaReserva = "$h:$m"
                        mostrarTimePickerApertura = false
                    }
                )
            },
            dismissButton = {
                AppDialogTextButton(
                    text = "Cancelar",
                    onClick = { mostrarTimePickerApertura = false }
                )
            },
            title = {
                Text(
                    text = "Apertura de reservas",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hora desde la que los clientes pueden reservar esta sesión.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(8.dp))
                    AppDialogTextButton(
                        text = "Abrir desde el inicio",
                        onClick = {
                            // Abrir desde el inicio del día (null).
                            aperturaReserva = null
                            mostrarTimePickerApertura = false
                        }
                    )
                }
            }
        )
    }
}
