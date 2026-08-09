package com.roberto.clientesgestor.ui.clientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.clientesgestor.data.entity.ClienteEntity
import com.roberto.clientesgestor.model.Cliente
import com.roberto.clientesgestor.model.EstadoCliente
import com.roberto.clientesgestor.model.FiltroClientes
import com.roberto.clientesgestor.navigation.Routes
import com.roberto.clientesgestor.ui.components.ClienteItem
import com.roberto.clientesgestor.ui.components.ResumenCard
import com.roberto.clientesgestor.ui.viewmodel.ClienteViewModel
import kotlin.random.Random

/**
 * ClientesScreen.kt
 * -----------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de gestión de clientes)
 * Es el archivo que define la pantalla dedicada a la gestión de clientes.
 * Sirve para mostrar un resumen del estado de los clientes y dar acceso a su gestión.
 */

/**
 * ClientesScreen
 * --------------
 * ✔ TIPO: función @Composable
 * Es la pantalla de clientes con cabecera, botón de volver y tarjetas de resumen.
 * Sirve para mostrar los resúmenes Activos, Bajas y Morosos de los clientes.
 */
@Composable
fun ClientesScreen(
    navController: NavHostController
) {

    /**
     * filtroSeleccionado
     * ------------------
     * ✔ TIPO: variable con estado (var) mediante delegación `by`
     * Es el filtro que está activo en la pantalla de clientes.
     * Sirve para decidir qué clientes se muestran al pulsar cada tarjeta de resumen.
     */
    var filtroSeleccionado by remember {
        mutableStateOf(FiltroClientes.TODOS)
    }

    /**
     * viewModel
     * ---------
     * ✔ TIPO: variable inmutable (val) → ClienteViewModel (inyectado por Hilt)
     * Es el ViewModel de la pantalla de clientes.
     * Sirve para obtener los datos de los clientes desde la base de datos a través del repositorio.
     */
    val viewModel: ClienteViewModel = hiltViewModel()

    /**
     * clientes
     * --------
     * ✔ TIPO: variable inmutable (val) → List<Cliente>
     * Es la lista de clientes observada desde el ViewModel.
     * Sirve para que la pantalla se actualice automáticamente cuando cambian los datos de la base de datos.
     */
    val clientes by viewModel.clientes.collectAsStateWithLifecycle()

    /**
     * listaClientes
     * -------------
     * ✔ TIPO: variable inmutable (val) → List<Cliente>
     * Es una lista de prueba con datos mockeados de clientes.
     * Sirve para mostrar la pantalla de clientes con datos de ejemplo.
     */
    /*val listaClientes = listOf(
        Cliente(
            idCliente = 1,
            nombre = "Roberto Pérez",
            telefono = "600123123",
            estado = EstadoCliente.ACTIVO
        ),
        Cliente(
            idCliente = 2,
            nombre = "Ana García",
            telefono = "611456789",
            estado = EstadoCliente.MOROSO
        ),
        Cliente(
            idCliente = 3,
            nombre = "Juan López",
            telefono = "622987654",
            estado = EstadoCliente.BAJA
        ),
        Cliente(
            idCliente = 4,
            nombre = "María Fernández",
            telefono = "633112233",
            estado = EstadoCliente.ACTIVO
        ),
        Cliente(
            idCliente = 5,
            nombre = "Luis Ramírez",
            telefono = "644998877",
            estado = EstadoCliente.MOROSO
        ),
        Cliente(
            idCliente = 6,
            nombre = "Carmen Soto",
            telefono = "655443322",
            estado = EstadoCliente.BAJA
        ),
        Cliente(
            idCliente = 7,
            nombre = "Pedro Martín",
            telefono = "677221100",
            estado = EstadoCliente.ACTIVO
        )
    )
*/
    /**
     * clientesFiltrados
     * -----------------
     * ✔ TIPO: variable inmutable (val) → List<Cliente>
     * Es la lista de clientes que cumple el filtro seleccionado.
     * Sirve para mostrar solo los clientes del estado elegido o todos.
     */
    val clientesFiltrados = when (filtroSeleccionado) {

        FiltroClientes.TODOS -> clientes

        FiltroClientes.ACTIVO -> clientes.filter { cliente ->
            cliente.estado == EstadoCliente.ACTIVO
        }
        FiltroClientes.MOROSO -> clientes.filter { cliente ->
            cliente.estado == EstadoCliente.MOROSO
        }
        FiltroClientes.BAJA -> clientes.filter { cliente ->
            cliente.estado == EstadoCliente.BAJA
        }
    }

    /**
     * totalClientes
     * -------------
     * ✔ TIPO: variable inmutable (val) → Int
     * Es el número total de clientes registrados.
     * Sirve para mostrarlo en la tarjeta de resumen "Todos".
     */
    val totalClientes = clientes.size

    /**
     * totalActivos
     * ------------
     * ✔ TIPO: variable inmutable (val) → Int
     * Es el número de clientes con estado ACTIVO.
     * Sirve para mostrarlo en la tarjeta de resumen "Activos".
     */
    val totalActivos = clientes.count { cliente ->
        cliente.estado == EstadoCliente.ACTIVO
    }

    /**
     * totalMorosos
     * ------------
     * ✔ TIPO: variable inmutable (val) → Int
     * Es el número de clientes con estado MOROSO.
     * Sirve para mostrarlo en la tarjeta de resumen "Morosos".
     */
    val totalMorosos = clientes.count { cliente ->
        cliente.estado == EstadoCliente.MOROSO
    }

    /**
     * totalBajas
     * ----------
     * ✔ TIPO: variable inmutable (val) → Int
     * Es el número de clientes con estado BAJA.
     * Sirve para mostrarlo en la tarjeta de resumen "Bajas".
     */
    val totalBajas = clientes.count { cliente ->
        cliente.estado == EstadoCliente.BAJA
    }

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
                    text = "Clientes",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                ResumenCard(
                    titulo = "Todos",
                    cantidad = totalClientes,
                    estaSeleccionada = filtroSeleccionado == FiltroClientes.TODOS,
                    Color.Transparent,
                    onClick = {
                        filtroSeleccionado = FiltroClientes.TODOS
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(6.dp)
                )

                ResumenCard(
                    titulo = "Activos",
                    cantidad = totalActivos,
                    estaSeleccionada = filtroSeleccionado == FiltroClientes.ACTIVO,
                    Color(0xFF4CAF50),
                    onClick = {
                        filtroSeleccionado = FiltroClientes.ACTIVO
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(6.dp)
                )

                ResumenCard(
                    "Bajas",
                    totalBajas,
                    estaSeleccionada = filtroSeleccionado == FiltroClientes.BAJA,
                    color = Color.Gray,
                    onClick = {
                        filtroSeleccionado = FiltroClientes.BAJA
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(6.dp)
                )

                ResumenCard(
                    "Morosos",
                    totalMorosos,
                    estaSeleccionada = filtroSeleccionado == FiltroClientes.MOROSO,
                    color = Color.Red,
                    onClick = {
                        filtroSeleccionado = FiltroClientes.MOROSO
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(6.dp)
                )
            }


            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(clientesFiltrados) { cliente ->
                    ClienteItem(
                        nombre = cliente.nombre,
                        telefono = cliente.telefono,
                        estado = cliente.estado,
                        onClick = {
                            navController.navigate(Routes.PERFILCLIENTE)
                        }
                    )
                }
            }
        }
    }
}
