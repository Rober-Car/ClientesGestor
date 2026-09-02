package com.roberto.gestorpro.ui.economia

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.data.entity.GastoEntity
import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.model.EstadoMovimiento
import com.roberto.gestorpro.ui.components.AppDialogConfirmButton
import com.roberto.gestorpro.ui.components.AppDialogDangerConfirmButton
import com.roberto.gestorpro.ui.components.AppDialogTextButton
import com.roberto.gestorpro.ui.components.AppIconDangerButton
import com.roberto.gestorpro.ui.components.AppIconPrimaryButton
import com.roberto.gestorpro.ui.components.AppNavigationBackButton
import com.roberto.gestorpro.ui.viewmodel.EconomiaViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/* ============================================================
 * ============ FILTRO DE ECONOMÍA ===========================
 * ============================================================ */
enum class FiltroEconomia {
    TODOS,
    INGRESOS,
    GASTOS
}

/* ============================================================
 * ============ ELEMENTO UNIFICADO DE LISTA ==================
 * ============================================================ */
sealed class ItemEconomia {
    data class Ingreso(val movimiento: MovimientoEntity) : ItemEconomia()
    data class Gasto(val gasto: GastoEntity) : ItemEconomia()

    fun fecha(): Long = when (this) {
        is Ingreso -> movimiento.fechaInicio
        is Gasto -> gasto.fecha
    }

    fun importe(): Double = when (this) {
        is Ingreso -> movimiento.precioFinal
        is Gasto -> -gasto.importe
    }
}

/* ============================================================
 * ============ PANTALLA ECONOMÍA ============================
 * ============================================================ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EconomiaScreen(
    navController: NavHostController,
    viewModel: EconomiaViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    val movimientos by viewModel.movimientos.collectAsStateWithLifecycle()
    val gastos by viewModel.gastos.collectAsStateWithLifecycle()
    val clientesMap by viewModel.clientesMap.collectAsStateWithLifecycle()
    val serviciosMap by viewModel.serviciosMap.collectAsStateWithLifecycle()

    var filtroSeleccionado by rememberSaveable(
        stateSaver = Saver<FiltroEconomia, String>(
            save = { it.name },
            restore = { FiltroEconomia.valueOf(it) }
        )
    ) {
        mutableStateOf(FiltroEconomia.TODOS)
    }

    var textoBusqueda by rememberSaveable { mutableStateOf("") }
    var ordenarDescendente by rememberSaveable { mutableStateOf(true) }

    val totalIngresos = movimientos.sumOf { it.precioFinal }
    val totalGastos = gastos.sumOf { it.importe }
    val balance = totalIngresos - totalGastos

    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }

    val items = remember(movimientos, gastos) {
        val lista = mutableListOf<ItemEconomia>()
        movimientos.forEach { lista.add(ItemEconomia.Ingreso(it)) }
        gastos.forEach { lista.add(ItemEconomia.Gasto(it)) }
        lista
    }

    val itemsFiltrados = remember(items, filtroSeleccionado, textoBusqueda, ordenarDescendente, clientesMap, serviciosMap) {
        val filtrados = when (filtroSeleccionado) {
            FiltroEconomia.TODOS -> items
            FiltroEconomia.INGRESOS -> items.filterIsInstance<ItemEconomia.Ingreso>()
            FiltroEconomia.GASTOS -> items.filterIsInstance<ItemEconomia.Gasto>()
        }

        val busqueda = textoBusqueda.trim().lowercase()
        val resultados = if (busqueda.isBlank()) {
            filtrados
        } else {
            filtrados.filter { item ->
                when (item) {
                    is ItemEconomia.Ingreso -> {
                        val nombreCliente = clientesMap[item.movimiento.idCliente].orEmpty().lowercase()
                        val nombreServicios = item.movimiento.servicios
                            .mapNotNull { serviciosMap[it] }
                            .joinToString(" ")
                            .lowercase()
                        nombreCliente.contains(busqueda) ||
                                nombreServicios.contains(busqueda) ||
                                item.movimiento.servicios.joinToString(" ").contains(busqueda)
                    }
                    is ItemEconomia.Gasto -> {
                        item.gasto.concepto.lowercase().contains(busqueda) ||
                                item.gasto.observaciones.orEmpty().lowercase().contains(busqueda)
                    }
                }
            }
        }

        if (ordenarDescendente) {
            resultados.sortedByDescending { it.fecha() }
        } else {
            resultados.sortedBy { it.fecha() }
        }
    }

    var mostrarDialogNuevoGasto by remember { mutableStateOf(false) }
    var gastoSeleccionado by remember { mutableStateOf<GastoEntity?>(null) }
    var movimientoSeleccionado by remember { mutableStateOf<MovimientoEntity?>(null) }
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogNuevoGasto = true },
                containerColor = Color(0xFF1E88E5)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir gasto",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppNavigationBackButton(onClick = { navController.popBackStack() })
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Economía",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResumenEconomiaCard(
                    titulo = "Ingresos",
                    cantidad = formatter.format(totalIngresos),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                ResumenEconomiaCard(
                    titulo = "Gastos",
                    cantidad = formatter.format(totalGastos),
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
                ResumenEconomiaCard(
                    titulo = "Balance",
                    cantidad = formatter.format(balance),
                    color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipEconomia(
                    texto = "Todos",
                    seleccionado = filtroSeleccionado == FiltroEconomia.TODOS,
                    color = Color(0xFF1E88E5),
                    onClick = { filtroSeleccionado = FiltroEconomia.TODOS },
                    modifier = Modifier.weight(1f)
                )
                FilterChipEconomia(
                    texto = "Ingresos",
                    seleccionado = filtroSeleccionado == FiltroEconomia.INGRESOS,
                    color = Color(0xFF4CAF50),
                    onClick = { filtroSeleccionado = FiltroEconomia.INGRESOS },
                    modifier = Modifier.weight(1f)
                )
                FilterChipEconomia(
                    texto = "Gastos",
                    seleccionado = filtroSeleccionado == FiltroEconomia.GASTOS,
                    color = Color(0xFFF44336),
                    onClick = { filtroSeleccionado = FiltroEconomia.GASTOS },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar por nombre, servicio...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color(0xFF1E88E5)
                    )
                )
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { ordenarDescendente = !ordenarDescendente },
                    color = Color(0xFF1E88E5).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (ordenarDescendente) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = if (ordenarDescendente) "Más recientes primero" else "Más antiguos primero",
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                items(itemsFiltrados) { item ->
                    when (item) {
                        is ItemEconomia.Ingreso -> ItemMovimiento(
                            movimiento = item.movimiento,
                            nombreCliente = clientesMap[item.movimiento.idCliente].orEmpty(),
                            nombreServicios = item.movimiento.servicios
                                .mapNotNull { serviciosMap[it] }
                                .joinToString(" + ")
                                .ifBlank { "Sin servicio asociado" },
                            onClick = { movimientoSeleccionado = item.movimiento }
                        )
                        is ItemEconomia.Gasto -> ItemGasto(
                            gasto = item.gasto,
                            onClick = {
                                gastoSeleccionado = item.gasto
                            }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogNuevoGasto) {
        DialogNuevoGasto(
            onDismiss = { mostrarDialogNuevoGasto = false },
            onGuardar = { concepto, importe, fecha, observaciones ->
                viewModel.insertarGasto(
                    GastoEntity(
                        concepto = concepto,
                        importe = importe,
                        fecha = fecha,
                        observaciones = observaciones
                    )
                )
                mostrarDialogNuevoGasto = false
            }
        )
    }

    if (gastoSeleccionado != null) {
        DialogDetalleGasto(
            gasto = gastoSeleccionado!!,
            onDismiss = { gastoSeleccionado = null },
            onEditar = { gastoModificado ->
                viewModel.actualizarGasto(gastoModificado)
                gastoSeleccionado = null
            },
            onEliminar = {
                mostrarConfirmarEliminar = true
            }
        )
    }

    if (movimientoSeleccionado != null) {
        DialogDetalleMovimiento(
            movimiento = movimientoSeleccionado!!,
            nombreServicios = movimientoSeleccionado!!.servicios
                .mapNotNull { serviciosMap[it] }
                .joinToString(" + ")
                .ifBlank { "Sin servicio asociado" },
            onDismiss = { movimientoSeleccionado = null }
        )
    }

    if (mostrarConfirmarEliminar && gastoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminar = false },
            title = {
                Text(
                    text = "Eliminar gasto",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text("¿Seguro que quieres eliminar este gasto? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                AppDialogDangerConfirmButton(
                    text = "Eliminar",
                    onClick = {
                        viewModel.eliminarGasto(gastoSeleccionado!!)
                        mostrarConfirmarEliminar = false
                        gastoSeleccionado = null
                    }
                )
            },
            dismissButton = {
                AppDialogTextButton(
                    text = "Cancelar",
                    onClick = { mostrarConfirmarEliminar = false }
                )
            }
        )
    }
}

/* ============================================================
 * ============ COMPONENTE: Tarjeta de resumen ===============
 * ============================================================ */
@Composable
fun ResumenEconomiaCard(
    titulo: String,
    cantidad: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = cantidad,
                style = MaterialTheme.typography.titleSmall,
                color = color
            )
        }
    }
}

/* ============================================================
 * ============ COMPONENTE: Chip de filtro ===================
 * ============================================================ */
@Composable
fun FilterChipEconomia(
    texto: String,
    seleccionado: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = if (seleccionado) color else color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = texto,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            textAlign = TextAlign.Center,
            color = if (seleccionado) Color.White else color,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/* ============================================================
 * ============ COMPONENTE: Fila de movimiento ==============
 * ============================================================ */
@Composable
fun ItemMovimiento(
    movimiento: MovimientoEntity,
    nombreCliente: String,
    nombreServicios: String,
    onClick: () -> Unit = {}
) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }
    val fechaFinFormateada = remember(movimiento.fechaFin) {
        Instant.ofEpochMilli(movimiento.fechaFin)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

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
                    text = nombreCliente,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$nombreServicios · $fechaFinFormateada",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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

/* ============================================================
 * ============ COMPONENTE: Fila de gasto ====================
 * ============================================================ */
@Composable
fun ItemGasto(
    gasto: GastoEntity,
    onClick: () -> Unit
) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }
    val fechaFormateada = remember(gasto.fecha) {
        Instant.ofEpochMilli(gasto.fecha)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336).copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                tint = Color(0xFFF44336),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gasto.concepto,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = fechaFormateada,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                text = "-${formatter.format(gasto.importe)}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFF44336)
            )
        }
    }
}

/* ============================================================
 * ============ DIALOG: Nuevo gasto ==========================
 * ============================================================ */
@Composable
fun DialogNuevoGasto(
    onDismiss: () -> Unit,
    onGuardar: (concepto: String, importe: Double, fecha: Long, observaciones: String?) -> Unit
) {
    var concepto by remember { mutableStateOf("") }
    var importe by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf<Long?>(null) }
    var observaciones by remember { mutableStateOf("") }
    var errorConcepto by remember { mutableStateOf(false) }
    var errorImporte by remember { mutableStateOf(false) }
    var errorFecha by remember { mutableStateOf(false) }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Nuevo gasto",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = concepto,
                    onValueChange = {
                        concepto = it
                        errorConcepto = false
                    },
                    label = { Text("Concepto") },
                    isError = errorConcepto,
                    supportingText = {
                        if (errorConcepto) Text("El concepto es obligatorio")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = importe,
                    onValueChange = {
                        importe = it
                        errorImporte = false
                    },
                    label = { Text("Importe") },
                    isError = errorImporte,
                    supportingText = {
                        if (errorImporte) Text("Introduce un importe válido")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                val fechaFormateada = fecha?.let {
                    Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                } ?: ""

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
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Seleccionar fecha"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { mostrarDatePicker = true }
                )

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppDialogTextButton(
                        text = "Cancelar",
                        onClick = onDismiss
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AppDialogConfirmButton(
                        text = "Guardar",
                        onClick = {
                            errorConcepto = concepto.isBlank()
                            errorImporte = importe.toDoubleOrNull() == null
                            errorFecha = fecha == null

                            if (!errorConcepto && !errorImporte && !errorFecha) {
                                onGuardar(
                                    concepto.trim(),
                                    importe.toDouble(),
                                    fecha!!,
                                    observaciones.trim().ifBlank { null }
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    if (mostrarDatePicker) {
        val hoy = LocalDate.now()
        val selectableDates = remember {
            object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = true
                override fun isSelectableYear(year: Int): Boolean =
                    year >= hoy.minusYears(120).year
            }
        }
        val datePickerState = rememberDatePickerState(selectableDates = selectableDates)

        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        fecha = datePickerState.selectedDateMillis
                        mostrarDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/* ============================================================
 * ============ DIALOG: Detalle gasto ========================
 * ============================================================ */
@Composable
fun DialogDetalleGasto(
    gasto: GastoEntity,
    onDismiss: () -> Unit,
    onEditar: (GastoEntity) -> Unit,
    onEliminar: () -> Unit
) {
    var editando by remember { mutableStateOf(false) }
    var conceptoEditado by remember(gasto) { mutableStateOf(gasto.concepto) }
    var importeEditado by remember(gasto) { mutableStateOf(gasto.importe.toString()) }
    var fechaEditada by remember(gasto) { mutableStateOf(gasto.fecha) }
    var observacionesEditadas by remember(gasto) { mutableStateOf(gasto.observaciones.orEmpty()) }
    var errorConcepto by remember { mutableStateOf(false) }
    var errorImporte by remember { mutableStateOf(false) }
    var mostrarDatePickerDetalle by remember { mutableStateOf(false) }

    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }
    val fechaFormateada = remember(fechaEditada) {
        Instant.ofEpochMilli(fechaEditada)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    Dialog(
        onDismissRequest = {
            onDismiss()
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (editando) "Editar gasto" else "Detalle",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                if (editando) {
                    OutlinedTextField(
                        value = conceptoEditado,
                        onValueChange = {
                            conceptoEditado = it
                            errorConcepto = false
                        },
                        label = { Text("Concepto") },
                        isError = errorConcepto,
                        supportingText = {
                            if (errorConcepto) Text("El concepto es obligatorio")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = importeEditado,
                        onValueChange = {
                            importeEditado = it
                            errorImporte = false
                        },
                        label = { Text("Importe") },
                        isError = errorImporte,
                        supportingText = {
                            if (errorImporte) Text("Introduce un importe válido")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = fechaFormateada,
                        onValueChange = { },
                        readOnly = true,
                        enabled = false,
                        label = { Text("Fecha") },
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
                            .clickable { mostrarDatePickerDetalle = true }
                    )

                    OutlinedTextField(
                        value = observacionesEditadas,
                        onValueChange = { observacionesEditadas = it },
                        label = { Text("Observaciones (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    DetalleCampo("Concepto", gasto.concepto)
                    DetalleCampo("Importe", formatter.format(gasto.importe))
                    DetalleCampo("Fecha", fechaFormateada)
                    if (!gasto.observaciones.isNullOrBlank()) {
                        DetalleCampo("Observaciones", gasto.observaciones)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (editando) {
                        AppDialogTextButton(
                            text = "Cancelar",
                            onClick = {
                                editando = false
                                conceptoEditado = gasto.concepto
                                importeEditado = gasto.importe.toString()
                                fechaEditada = gasto.fecha
                                observacionesEditadas = gasto.observaciones.orEmpty()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AppDialogConfirmButton(
                            text = "Guardar",
                            onClick = {
                                errorConcepto = conceptoEditado.isBlank()
                                errorImporte = importeEditado.toDoubleOrNull() == null

                                if (!errorConcepto && !errorImporte) {
                                    onEditar(
                                        gasto.copy(
                                            concepto = conceptoEditado.trim(),
                                            importe = importeEditado.toDouble(),
                                            fecha = fechaEditada,
                                            observaciones = observacionesEditadas.trim()
                                                .ifBlank { null }
                                        )
                                    )
                                }
                            }
                        )
                    } else {
                        AppIconPrimaryButton(
                            icon = Icons.Default.Edit,
                            onClick = { editando = true },
                            contentDescription = "Editar"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AppIconDangerButton(
                            icon = Icons.Default.Delete,
                            onClick = onEliminar,
                            contentDescription = "Eliminar"
                        )
                    }
                }
            }
        }
    }

    if (mostrarDatePickerDetalle) {
        val hoy = LocalDate.now()
        val selectableDates = remember {
            object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = true
                override fun isSelectableYear(year: Int): Boolean =
                    year >= hoy.minusYears(120).year
            }
        }
        val datePickerState = rememberDatePickerState(selectableDates = selectableDates)

        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerDetalle = false },
            confirmButton = {
                TextButton(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        fechaEditada = datePickerState.selectedDateMillis!!
                        mostrarDatePickerDetalle = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerDetalle = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/* ============================================================
 * ============ COMPONENTE: Campo de detalle =================
 * ============================================================ */
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

/* ============================================================
 * ============ DIALOG: Detalle movimiento ===================
 * ============================================================ */
@Composable
fun DialogDetalleMovimiento(
    movimiento: MovimientoEntity,
    nombreServicios: String,
    onDismiss: () -> Unit
) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "ES")) }
    val fechaInicioFormateada = remember(movimiento.fechaInicio) {
        Instant.ofEpochMilli(movimiento.fechaInicio)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
    val fechaFinFormateada = remember(movimiento.fechaFin) {
        Instant.ofEpochMilli(movimiento.fechaFin)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
    val fechaPagoFormateada = remember(movimiento.fechaPago) {
        movimiento.fechaPago?.let {
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
    }

    Dialog(
        onDismissRequest = onDismiss
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Detalle",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                DetalleCampo("Servicio", nombreServicios)
                DetalleCampo("Precio", formatter.format(movimiento.precioFinal))
                DetalleCampo("Fecha inicio", fechaInicioFormateada)
                DetalleCampo("Fecha fin", fechaFinFormateada)
                DetalleCampo("Estado", movimiento.estado.name)
                if (movimiento.estado == EstadoMovimiento.PAGADO) {
                    if (fechaPagoFormateada != null) {
                        DetalleCampo("Fecha de pago", fechaPagoFormateada)
                    }
                    DetalleCampo(
                        "Método de pago",
                        com.roberto.gestorpro.util.MovimientoPago.metodoPagoLabel(
                            movimiento.metodoPago
                        )
                    )
                }
                if (!movimiento.observaciones.isNullOrBlank()) {
                    DetalleCampo("Observaciones", movimiento.observaciones)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppDialogTextButton(
                        text = "Cerrar",
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}
