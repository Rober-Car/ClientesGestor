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

import androidx.navigation.NavHostController
import com.roberto.clientesgestor.model.Cliente
import com.roberto.clientesgestor.model.Estado
import com.roberto.clientesgestor.model.FiltroClientes
import com.roberto.clientesgestor.navigation.Routes
import com.roberto.clientesgestor.ui.components.ClienteItem

import com.roberto.clientesgestor.ui.components.ResumenCard

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
     * Lista de prueba con datos mockeados de clientes.
     */
    val listaClientes = listOf(
        Cliente(
            idCliente = 1,
            nombre = "Roberto Pérez",
            telefono = "600123123",
            estado = Estado.ACTIVO
        ),
        Cliente(
            idCliente = 2,
            nombre = "Ana García",
            telefono = "611456789",
            estado = Estado.MOROSO
        ),
        Cliente(
            idCliente = 3,
            nombre = "Juan López",
            telefono = "622987654",
            estado = Estado.BAJA
        ),
        Cliente(
            idCliente = 4,
            nombre = "María Fernández",
            telefono = "633112233",
            estado = Estado.ACTIVO
        ),
        Cliente(
            idCliente = 5,
            nombre = "Luis Ramírez",
            telefono = "644998877",
            estado = Estado.MOROSO
        ),
        Cliente(
            idCliente = 6,
            nombre = "Carmen Soto",
            telefono = "655443322",
            estado = Estado.BAJA
        ),
        Cliente(
            idCliente = 7,
            nombre = "Pedro Martín",
            telefono = "677221100",
            estado = Estado.ACTIVO
        )
    )

    /**
     * clientesFiltrados
     * -----------------
     * ✔ TIPO: variable inmutable (val) → List<Cliente>
     * Es la lista de clientes que cumple el filtro seleccionado.
     * Sirve para mostrar solo los clientes del estado elegido o todos.
     */
    val clientesFiltrados = when (filtroSeleccionado) {

        FiltroClientes.TODOS -> listaClientes

        FiltroClientes.ACTIVO ->  listaClientes.filter { cliente ->
                cliente.estado == Estado.ACTIVO}

        FiltroClientes.MOROSO -> listaClientes.filter { cliente ->
                cliente.estado == Estado.MOROSO }

        FiltroClientes.BAJA -> listaClientes.filter { cliente ->

            cliente.estado == Estado.BAJA
        }
    }


    /**
     * totalClientes
     * -------------
     * ✔ TIPO: variable inmutable (val) → Int
     * Es el número total de clientes de la lista.
     * Sirve para mostrar la cantidad en la tarjeta "Todos".
     */
    val totalClientes = listaClientes.size

    /**
     * totalActivos
     * ------------
     * ✔ TIPO: variable inmutable (val) → Int
     * Es el número de clientes activos de la lista.
     * Sirve para mostrar la cantidad en la tarjeta "Activos".
     */
    val totalActivos = listaClientes.count { cliente ->
        cliente.estado == Estado.ACTIVO
    }

    /**
     * totalMorosos
     * ------------
     * ✔ TIPO: variable inmutable (val) → Int
     * Es el número de clientes morosos de la lista.
     * Sirve para mostrar la cantidad en la tarjeta "Morosos".
     */
    val totalMorosos = listaClientes.count { cliente ->
        cliente.estado == Estado.MOROSO
    }

    /**
     * totalBajas
     * ----------
     * ✔ TIPO: variable inmutable (val) → Int
     * Es el número de clientes dados de baja de la lista.
     * Sirve para mostrar la cantidad en la tarjeta "Bajas".
     */
    val totalBajas = listaClientes.count { cliente ->
        cliente.estado == Estado.BAJA
    }

    /**
     * Scaffold
     * --------
     * ✔ TIPO: Scaffold Estructura base de Material Design para la pantalla de Clientes.
     * Es un contenedor de alto nivel que organiza la pantalla en zonas.
     * Sirve para estructurar la pantalla de clientes con un layout coherente.
     */
    Scaffold { innerPadding ->

        /**
         * Column
         * ------
         * ✔ TIPO: Column (layout vertical)
         * Es un contenedor que coloca elementos uno debajo del otro.
         * Sirve para apilar verticalmente el contenido de la pantalla.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            /**
             * Row (cabecera)
             * --------------
             * ✔ TIPO: Row (layout horizontal)
             * Es una fila que contiene el botón de volver y el título.
             * Sirve para mostrar la cabecera de la pantalla.
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                /**
                 * IconButton (volver)
                 * ------------------
                 * ✔ TIPO: IconButton
                 * Es un botón que muestra la flecha de retroceso.
                 * Sirve para volver a la pantalla anterior con popBackStack().
                 */
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

                /**
                 * Spacer
                 * ------
                 * ✔ TIPO: Spacer
                 * Es un elemento invisible que ocupa espacio.
                 * Sirve para separar el botón de volver del título.
                 */
                Spacer(
                    modifier = Modifier.width(16.dp)
                )

                /**
                 * Text("Clientes")
                 * ---------------
                 * ✔ TIPO: Text
                 * Es el título de la pantalla.
                 * Sirve para identificar la sección de clientes.
                 */
                Text(
                    text = "Clientes",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            /**
             * Row (resúmenes)
             * ---------------
             * ✔ TIPO: Row (layout horizontal)
             * Es una fila que contiene las tres tarjetas de resumen.
             * Sirve para repartir el ancho entre Activos, Bajas y Morosos.
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {

                /**
                 * ResumenCard (Todos)
                 * -------------------
                 * ✔ TIPO: Composable personalizado (ResumenCard)
                 * Es una tarjeta que muestra el número total de clientes.
                 * Sirve para ver el total y volver a mostrar todos los clientes.
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
                 * ResumenCard (Activos)
                 * --------------------
                 * Tarjeta que muestra el número de clientes activos.
                 * Tipo ResumenCard detallado más arriba con "Todos".
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
                 * ResumenCard (Bajas)
                 * -------------------
                 * Tarjeta que muestra el número de clientes dados de baja.
                 * Tipo ResumenCard detallado más arriba con "Todos".
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
                 * ResumenCard (Morosos)
                 * --------------------
                 * Tarjeta que muestra el número de clientes morosos.
                 * Tipo ResumenCard detallado más arriba con "Todos".
                 */
                ResumenCard(
                    "Morosos",
                    totalMorosos ,
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
             * ✔ TIPO: LazyColumn (lista perezosa vertical)
             * Es una lista que solo dibuja los elementos visibles en pantalla.
             * Sirve para mostrar los clientes filtrados uno debajo del otro.
             */
            LazyColumn(

                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(16.dp)

            ){

                /**
                 * items(clientesFiltrados)
                 * ------------------------
                 * ✔ TIPO: función (androidx.compose.foundation.lazy.items)
                 * Es la función que recorre la lista de clientes filtrados.
                 * Sirve para crear un ClienteItem por cada cliente de la lista.
                 */
                items (clientesFiltrados){ cliente ->
                    /**
                     * ClienteItem
                     * -----------
                     * ✔ TIPO: Composable personalizado (ClienteItem)
                     * Es la tarjeta que muestra los datos de un cliente.
                     * Sirve para representar cada cliente dentro de la lista.
                     */
                    ClienteItem(
                        nombre = cliente.nombre,
                        telefono = cliente.telefono,
                        estado = cliente.estado,
                        onClick = {

                            navController.navigate(Routes.PERFILCLIENTE)
                        },
                    )


                }
            }

        }

    }

}

