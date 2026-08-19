package com.roberto.gestorpro.ui.clientes

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.model.EstadoCliente
import com.roberto.gestorpro.model.FiltroClientes
import com.roberto.gestorpro.navigation.Routes
import com.roberto.gestorpro.ui.components.ClienteItem
import com.roberto.gestorpro.ui.viewmodel.ClienteViewModel

@Composable
fun ClientesScreen(
    navController: NavHostController
) {

    var filtroSeleccionado by rememberSaveable(
        stateSaver = Saver<FiltroClientes, String>(
            save = { it.name },
            restore = { FiltroClientes.valueOf(it) }
        )
    ) {
        mutableStateOf(FiltroClientes.TODOS)
    }

    val viewModel: ClienteViewModel = hiltViewModel()

    val clientes by viewModel.clientes.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val morososIds by viewModel.morososIds.collectAsStateWithLifecycle()

    val clientesFiltrados = when (filtroSeleccionado) {
        FiltroClientes.TODOS -> clientes
        FiltroClientes.ACTIVO -> clientes.filter { it.estado == EstadoCliente.ACTIVO }
        FiltroClientes.MOROSO -> clientes.filter { it.idCliente in morososIds }
        FiltroClientes.BAJA -> clientes.filter { it.estado == EstadoCliente.BAJA }
    }

    val totalClientes = clientes.size
    val totalActivos = clientes.count { it.estado == EstadoCliente.ACTIVO }
    val totalMorosos = morososIds.size
    val totalBajas = clientes.count { it.estado == EstadoCliente.BAJA }

    Scaffold { innerPadding ->

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
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Clientes",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { navController.navigate(Routes.AÑADIRCLIENTE) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E88E5),
                        contentColor = Color.White
                    )
                ) {
                    Text("Añadir cliente")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResumenClienteCard(
                    titulo = "Todos",
                    cantidad = totalClientes,
                    color = Color(0xFF1E88E5),
                    seleccionado = filtroSeleccionado == FiltroClientes.TODOS,
                    onClick = { filtroSeleccionado = FiltroClientes.TODOS },
                    modifier = Modifier.weight(1f)
                )
                ResumenClienteCard(
                    titulo = "Activos",
                    cantidad = totalActivos,
                    color = Color(0xFF4CAF50),
                    seleccionado = filtroSeleccionado == FiltroClientes.ACTIVO,
                    onClick = { filtroSeleccionado = FiltroClientes.ACTIVO },
                    modifier = Modifier.weight(1f)
                )
                ResumenClienteCard(
                    titulo = "Bajas",
                    cantidad = totalBajas,
                    color = Color.Gray,
                    seleccionado = filtroSeleccionado == FiltroClientes.BAJA,
                    onClick = { filtroSeleccionado = FiltroClientes.BAJA },
                    modifier = Modifier.weight(1f)
                )
                ResumenClienteCard(
                    titulo = "Morosos",
                    cantidad = totalMorosos,
                    color = Color.Red,
                    seleccionado = filtroSeleccionado == FiltroClientes.MOROSO,
                    onClick = { filtroSeleccionado = FiltroClientes.MOROSO },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(clientesFiltrados) { cliente ->
                    ClienteItem(
                        nombre = cliente.nombre,
                        telefono = cliente.telefono,
                        estado = cliente.estado,
                        foto = cliente.foto,
                        esMoroso = cliente.idCliente in morososIds,
                        onClick = {
                            navController.navigate(
                                Routes.perfilCliente(cliente.idCliente)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ResumenClienteCard(
    titulo: String,
    cantidad: Int,
    color: Color,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) color else color.copy(alpha = 0.1f)
        )
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
                color = if (seleccionado) Color.White else color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = cantidad.toString(),
                style = MaterialTheme.typography.titleSmall,
                color = if (seleccionado) Color.White else color
            )
        }
    }
}
