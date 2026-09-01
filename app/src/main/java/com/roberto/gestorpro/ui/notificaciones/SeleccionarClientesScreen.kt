package com.roberto.gestorpro.ui.notificaciones

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.model.EstadoCliente
import com.roberto.gestorpro.model.FiltroClientes
import com.roberto.gestorpro.ui.clientes.FilterChipItem
import com.roberto.gestorpro.ui.components.ClienteItem
import com.roberto.gestorpro.ui.viewmodel.ClienteViewModel
import com.roberto.gestorpro.ui.viewmodel.NotificacionesViewModel

/**
 * SeleccionarClientesScreen
 * -------------------------
 * Pantalla de selección de clientes para una notificación GRUPAL.
 *
 * Reutiliza la lista de clientes del ADMIN (ClienteViewModel), la tarjeta
 * ClienteItem y los filtros de ClientesScreen (buscador + chips por estado).
 * La tarjeta completa es pulsable para seleccionar/deseleccionar y muestra un
 * checkbox a la derecha que refleja el estado visualmente.
 *
 * La selección vive en el ViewModel compartido (seleccionGrupo), por lo que se
 * conserva al cambiar de filtro o búsqueda y al volver al formulario con
 * "Continuar". Esta pantalla NO envía la notificación: solo elige destinatarios.
 */
@Composable
fun SeleccionarClientesScreen(
    navController: NavHostController,
    viewModel: NotificacionesViewModel,
    clienteViewModel: ClienteViewModel = hiltViewModel()
) {
    val clientes by clienteViewModel.clientes.collectAsStateWithLifecycle()
    val morososIds by clienteViewModel.morososIds.collectAsStateWithLifecycle()

    var filtroSeleccionado by rememberSaveable(
        stateSaver = Saver<FiltroClientes, String>(
            save = { it.name },
            restore = { FiltroClientes.valueOf(it) }
        )
    ) {
        mutableStateOf(FiltroClientes.TODOS)
    }
    var textoBusqueda by rememberSaveable { mutableStateOf("") }

    // La selección se inicializa con lo que ya hubiera (persistida en el
    // ViewModel compartido) y se sincroniza con cada cambio.
    var seleccionados by remember { mutableStateOf(viewModel.seleccionGrupo.value) }

    fun alternarSeleccion(idCliente: Int) {
        seleccionados = if (idCliente in seleccionados) {
            seleccionados - idCliente
        } else {
            seleccionados + idCliente
        }
        viewModel.actualizarSeleccionGrupo(seleccionados)
    }

    val clientesFiltrados = clientes
        .filter { cliente ->
            when (filtroSeleccionado) {
                FiltroClientes.TODOS -> cliente.estado != EstadoCliente.ARCHIVADO
                FiltroClientes.ACTIVO -> cliente.estado == EstadoCliente.ACTIVO
                FiltroClientes.MOROSO -> cliente.idCliente in morososIds
                FiltroClientes.BAJA -> cliente.estado == EstadoCliente.BAJA
                FiltroClientes.ARCHIVADO -> cliente.estado == EstadoCliente.ARCHIVADO
            }
        }
        .filter { cliente ->
            textoBusqueda.isBlank() || listOf(
                cliente.nombre,
                cliente.telefono,
                cliente.dni,
                cliente.email.orEmpty()
            ).any { it.contains(textoBusqueda, ignoreCase = true) }
        }

    val totalClientes = clientes.count { it.estado != EstadoCliente.ARCHIVADO }
    val totalActivos = clientes.count { it.estado == EstadoCliente.ACTIVO }
    val totalMorosos = morososIds.size
    val totalBajas = clientes.count { it.estado == EstadoCliente.BAJA }
    val totalArchivados = clientes.count { it.estado == EstadoCliente.ARCHIVADO }

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${seleccionados.size} clientes seleccionados",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Continuar")
                    }
                }
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    text = "Seleccionar clientes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Buscar por nombre, DNI, teléfono...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { textoBusqueda = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar búsqueda"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipItem(
                    titulo = "Todos",
                    cantidad = totalClientes,
                    color = Color(0xFF1E88E5),
                    seleccionado = filtroSeleccionado == FiltroClientes.TODOS,
                    onClick = { filtroSeleccionado = FiltroClientes.TODOS }
                )
                FilterChipItem(
                    titulo = "Activos",
                    cantidad = totalActivos,
                    color = Color(0xFF4CAF50),
                    seleccionado = filtroSeleccionado == FiltroClientes.ACTIVO,
                    onClick = { filtroSeleccionado = FiltroClientes.ACTIVO }
                )
                FilterChipItem(
                    titulo = "Bajas",
                    cantidad = totalBajas,
                    color = Color.Gray,
                    seleccionado = filtroSeleccionado == FiltroClientes.BAJA,
                    onClick = { filtroSeleccionado = FiltroClientes.BAJA }
                )
                FilterChipItem(
                    titulo = "Morosos",
                    cantidad = totalMorosos,
                    color = Color.Red,
                    seleccionado = filtroSeleccionado == FiltroClientes.MOROSO,
                    onClick = { filtroSeleccionado = FiltroClientes.MOROSO }
                )
                FilterChipItem(
                    titulo = "Archivados",
                    cantidad = totalArchivados,
                    color = Color(0xFFFF9800),
                    seleccionado = filtroSeleccionado == FiltroClientes.ARCHIVADO,
                    onClick = { filtroSeleccionado = FiltroClientes.ARCHIVADO }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (clientesFiltrados.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (textoBusqueda.isNotBlank()) {
                            "No se encontraron resultados"
                        } else {
                            "No hay clientes en esta categoría"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(clientesFiltrados, key = { it.idCliente }) { cliente ->
                        ClienteItem(
                            nombre = cliente.nombre,
                            telefono = cliente.telefono,
                            estado = cliente.estado,
                            foto = cliente.foto,
                            esMoroso = cliente.idCliente in morososIds,
                            seleccionable = true,
                            seleccionado = cliente.idCliente in seleccionados,
                            onClick = { alternarSeleccion(cliente.idCliente) }
                        )
                    }
                }
            }
        }
    }
}
