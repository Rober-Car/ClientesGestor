package com.roberto.gestorpro.ui.clases

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.roberto.gestorpro.data.entity.ClaseEntity
import com.roberto.gestorpro.ui.viewmodel.ClaseViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearClaseScreen(
    navController: NavHostController,
    viewModel: ClaseViewModel = hiltViewModel()
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var horaInicio by rememberSaveable { mutableStateOf("20:00") }
    var duracionMinutos by rememberSaveable { mutableStateOf("60") }
    var capacidadMaxima by rememberSaveable { mutableStateOf("20") }
    var reservaDesdeHorasAntes by rememberSaveable { mutableStateOf("2") }

    var diasSeleccionados by rememberSaveable { mutableStateOf(setOf<DayOfWeek>()) }
    var fechaInicio by rememberSaveable { mutableStateOf<Long?>(null) }
    var fechaFin by rememberSaveable { mutableStateOf<Long?>(null) }

    var errorNombre by rememberSaveable { mutableStateOf(false) }
    var errorHora by rememberSaveable { mutableStateOf(false) }
    var errorDuracion by rememberSaveable { mutableStateOf(false) }
    var errorCapacidad by rememberSaveable { mutableStateOf(false) }
    var errorDias by rememberSaveable { mutableStateOf(false) }
    var errorFechaInicio by rememberSaveable { mutableStateOf(false) }
    var errorFechaFin by rememberSaveable { mutableStateOf(false) }

    var mostrarDatePickerInicio by rememberSaveable { mutableStateOf(false) }
    var mostrarDatePickerFin by rememberSaveable { mutableStateOf(false) }

    val diasSemana = listOf(
        DayOfWeek.MONDAY to "L",
        DayOfWeek.TUESDAY to "M",
        DayOfWeek.WEDNESDAY to "X",
        DayOfWeek.THURSDAY to "J",
        DayOfWeek.FRIDAY to "V",
        DayOfWeek.SATURDAY to "S",
        DayOfWeek.SUNDAY to "D"
    )

    val diasNombres = listOf(
        DayOfWeek.MONDAY to "Lun",
        DayOfWeek.TUESDAY to "Mar",
        DayOfWeek.WEDNESDAY to "Mié",
        DayOfWeek.THURSDAY to "Jue",
        DayOfWeek.FRIDAY to "Vie",
        DayOfWeek.SATURDAY to "Sáb",
        DayOfWeek.SUNDAY to "Dom"
    )

    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    val fechaInicioFormateada = fechaInicio?.let {
        Instant.ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(formatter)
    } ?: ""

    val fechaFinFormateada = fechaFin?.let {
        Instant.ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(formatter)
    } ?: ""

    Scaffold(
        floatingActionButton = {
            Button(
                onClick = {
                    errorNombre = nombre.isBlank()
                    errorHora = !horaInicio.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d$"))
                    errorDuracion = duracionMinutos.toIntOrNull() == null || duracionMinutos.toInt() <= 0
                    errorCapacidad = capacidadMaxima.toIntOrNull() == null || capacidadMaxima.toInt() <= 0
                    errorDias = diasSeleccionados.isEmpty()
                    errorFechaInicio = fechaInicio == null
                    errorFechaFin = fechaFin == null

                    if (!errorNombre && !errorHora && !errorDuracion && !errorCapacidad && !errorDias && !errorFechaInicio && !errorFechaFin) {
                        val clase = ClaseEntity(
                            nombre = nombre.trim(),
                            diasSemana = ClaseViewModel.diasSemanaToString(diasSeleccionados),
                            horaInicio = horaInicio,
                            duracionMinutos = duracionMinutos.toInt(),
                            capacidadMaxima = capacidadMaxima.toInt(),
                            reservaDesdeHorasAntes = reservaDesdeHorasAntes.toIntOrNull() ?: 2,
                            fechaInicio = fechaInicio!!,
                            fechaFin = fechaFin!!
                        )
                        viewModel.crearClase(clase)
                        navController.popBackStack()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Crear clase")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Crear clase",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(
                text = "Información básica",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    errorNombre = false
                },
                label = { Text("Nombre de la clase") },
                placeholder = { Text("Ej: CrossFit, Yoga, Spinning...") },
                isError = errorNombre,
                supportingText = {
                    if (errorNombre) Text("El nombre es obligatorio")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = horaInicio,
                onValueChange = {
                    horaInicio = it
                    errorHora = false
                },
                label = { Text("Hora de inicio") },
                placeholder = { Text("HH:mm") },
                isError = errorHora,
                supportingText = {
                    if (errorHora) Text("Formato: HH:mm (ej: 20:00)")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = duracionMinutos,
                    onValueChange = {
                        duracionMinutos = it
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
                    value = capacidadMaxima,
                    onValueChange = {
                        capacidadMaxima = it
                        errorCapacidad = false
                    },
                    label = { Text("Plazas máximas") },
                    isError = errorCapacidad,
                    supportingText = {
                        if (errorCapacidad) Text("Inválido")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = reservaDesdeHorasAntes,
                onValueChange = { reservaDesdeHorasAntes = it },
                label = { Text("Reserva desde (h antes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = "Horario semanal",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "Selecciona los días",
                style = MaterialTheme.typography.bodyMedium,
                color = if (errorDias) MaterialTheme.colorScheme.error else Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                diasSemana.forEachIndexed { index, (dia, letra) ->
                    val seleccionado = diasSeleccionados.contains(dia)
                    val nombreDia = diasNombres[index].second

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .clickable {
                                    diasSeleccionados = if (seleccionado) {
                                        diasSeleccionados - dia
                                    } else {
                                        diasSeleccionados + dia
                                    }
                                    errorDias = false
                                },
                            shape = CircleShape,
                            color = if (seleccionado) Color(0xFF1E88E5) else Color.Transparent,
                            border = if (seleccionado) null else androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                Color.LightGray
                            )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = letra,
                                    color = if (seleccionado) Color.White else Color.DarkGray,
                                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nombreDia,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (seleccionado) Color(0xFF1E88E5) else Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Text(
                text = "Fechas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )

            OutlinedTextField(
                value = fechaInicioFormateada,
                onValueChange = { },
                readOnly = true,
                enabled = false,
                label = { Text("Fecha de inicio") },
                placeholder = { Text("dd/MM/aaaa") },
                isError = errorFechaInicio,
                supportingText = {
                    if (errorFechaInicio) Text("La fecha de inicio es obligatoria")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = Color.Transparent,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Seleccionar fecha"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarDatePickerInicio = true }
            )

            OutlinedTextField(
                value = fechaFinFormateada,
                onValueChange = { },
                readOnly = true,
                enabled = false,
                label = { Text("Fecha de fin") },
                placeholder = { Text("dd/MM/aaaa") },
                isError = errorFechaFin,
                supportingText = {
                    if (errorFechaFin) Text("La fecha de fin es obligatoria")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = Color.Transparent,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Seleccionar fecha"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarDatePickerFin = true }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (mostrarDatePickerInicio) {
        val hoy = LocalDate.now()
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
                        fechaInicio = datePickerState.selectedDateMillis
                        mostrarDatePickerInicio = false
                        errorFechaInicio = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerInicio = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (mostrarDatePickerFin) {
        val hoy = LocalDate.now()
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
                        fechaFin = datePickerState.selectedDateMillis
                        mostrarDatePickerFin = false
                        errorFechaFin = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerFin = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
