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
     * error
     * -----
     * ✔ TIPO: variable inmutable (val) → String?
     * Es el mensaje de error observado desde el ViewModel.
     * Sirve para mostrar al usuario avisos como "El DNI ya está registrado"
     * cuando ocurre un error al guardar datos.
     */
    val error by viewModel.error.collectAsStateWithLifecycle()

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

        /**
         * Filtro TODOS
         * ------------
         * ✔ TIPO: rama (when) del filtro seleccionado
         * Es el caso en que el filtro elegido es TODOS.
         * Sirve para mostrar la lista completa de clientes sin filtrar.
         */
        FiltroClientes.TODOS -> clientes

        /**
         * Filtro ACTIVO
         * -------------
         * ✔ TIPO: rama (when) del filtro seleccionado
         * Es el caso en que el filtro elegido es ACTIVO.
         * Sirve para mostrar solo los clientes cuyo estado es ACTIVO.
         */
        FiltroClientes.ACTIVO -> clientes.filter { cliente ->
            cliente.estado == EstadoCliente.ACTIVO
        }

        /**
         * Filtro MOROSO
         * -------------
         * ✔ TIPO: rama (when) del filtro seleccionado
         * Es el caso en que el filtro elegido es MOROSO.
         * Sirve para mostrar solo los clientes cuyo estado es MOROSO.
         */
        FiltroClientes.MOROSO -> clientes.filter { cliente ->
            cliente.estado == EstadoCliente.MOROSO
        }

        /**
         * Filtro BAJA
         * -----------
         * ✔ TIPO: rama (when) del filtro seleccionado
         * Es el caso en que el filtro elegido es BAJA.
         * Sirve para mostrar solo los clientes cuyo estado es BAJA.
         */
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

    /**
     * Scaffold
     * --------
     * ✔ TIPO: función @Composable (androidx.compose.material3.Scaffold)
     * Es el contenedor base de la pantalla de clientes.
     * Sirve como estructura general y proporciona el innerPadding para el contenido.
     */
    Scaffold { innerPadding ->

        /**
         * Column
         * ------
         * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
         * Es el contenedor vertical principal de la pantalla de clientes.
         * Sirve para apilar la cabecera, las tarjetas de resumen y la lista de clientes.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            /**
             * Row de la cabecera
             * ------------------
             * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Row)
             * Es la fila superior de la pantalla con el botón de volver y el título.
             * Sirve para situar el botón "Volver" a la izquierda y el título "Clientes" a su lado.
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                /**
                 * IconButton de volver
                 * --------------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.IconButton)
                 * Es el botón con forma de icono que permite retroceder.
                 * Sirve para volver a la pantalla anterior pulsando la flecha.
                 */
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {

                    /**
                     * Icon de flecha
                     * --------------
                     * ✔ TIPO: función @Composable (androidx.compose.material3.Icon)
                     * Es el icono de flecha hacia atrás del botón.
                     * Sirve para indicar visualmente la acción de volver.
                     */
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                /**
                 * Text del título
                 * ---------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
                 * Es el título de la pantalla de clientes.
                 * Sirve para indicar al usuario en qué sección se encuentra.
                 */
                Text(
                    text = "Clientes",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            /**
             * Row de resumen
             * --------------
             * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Row)
             * Es la fila que contiene las tarjetas de resumen de clientes.
             * Sirve para mostrar y seleccionar los filtros Todos, Activos, Bajas y Morosos.
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {

                /**
                 * ResumenCard de Todos
                 * --------------------
                 * ✔ TIPO: componente @Composable (ResumenCard)
                 * Es la tarjeta que muestra el total de clientes y selecciona el filtro TODOS.
                 * Sirve para consultar la lista completa al pulsarla.
                 */
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

                /**
                 * ResumenCard de Activos
                 * ----------------------
                 * ✔ TIPO: componente @Composable (ResumenCard)
                 * Es la tarjeta verde que muestra los clientes activos y selecciona el filtro ACTIVO.
                 * Sirve para consultar solo los clientes activos al pulsarla.
                 */
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

                /**
                 * ResumenCard de Bajas
                 * --------------------
                 * ✔ TIPO: componente @Composable (ResumenCard)
                 * Es la tarjeta gris que muestra los clientes de baja y selecciona el filtro BAJA.
                 * Sirve para consultar solo los clientes dados de baja al pulsarla.
                 */
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

                /**
                 * ResumenCard de Morosos
                 * ----------------------
                 * ✔ TIPO: componente @Composable (ResumenCard)
                 * Es la tarjeta roja que muestra los clientes morosos y selecciona el filtro MOROSO.
                 * Sirve para consultar solo los clientes morosos al pulsarla.
                 */
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

            /**
             * LazyColumn
             * ----------
             * ✔ TIPO: función @Composable (androidx.compose.foundation.lazy.LazyColumn)
             * Es la lista vertical de clientes con carga perezosa.
             * Sirve para mostrar los clientes filtrados de forma eficiente,
             * creando solo los elementos visibles y separándolos con 8.dp.
             */
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)
            ) {

                /**
                 * items(clientesFiltrados)
                 * -----------------------
                 * ✔ TIPO: extensión de LazyListScope (items)
                 * Es el bloque que recorre la lista de clientes filtrados.
                 * Sirve para crear un ClienteItem por cada cliente de la lista.
                 */
                items(clientesFiltrados) { cliente ->

                    /**
                     * ClienteItem
                     * -----------
                     * ✔ TIPO: componente @Composable (ClienteItem)
                     * Es el elemento de lista que muestra los datos de un cliente.
                     * Sirve para mostrar nombre, teléfono y estado del cliente
                     * y navegar a su perfil al pulsarlo.
                     */
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
