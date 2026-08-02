package com.roberto.clientesgestor.ui.clientes

import android.R.attr.onClick
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.roberto.clientesgestor.navigation.Routes
import com.roberto.clientesgestor.ui.components.ResumenCard


/**
 * ClientesScreen.kt
 * -----------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de gestión de clientes)
 *
 * ¿Qué es?
 * El archivo que define la pantalla dedicada a la gestión de clientes.
 *
 * ¿Qué hace?
 * - Declara ClientesScreen, el composable de la pantalla de clientes.
 * - Muestra una cabecera con el título y un botón para volver atrás.
 * - Muestra las tarjetas de resumen: Activos, Bajas y Morosos.
 *
 * ¿Para qué sirve?
 * Para mostrar al usuario un resumen del estado de los clientes
 * y dar acceso a su gestión dentro de la aplicación.
 */

/**
 * ClientesScreen
 * --------------
 * ✔ TIPO: función @Composable
 * Es una función declarativa que Jetpack Compose usa para dibujar interfaz.
 * Sirve para mostrar la pantalla dedicada a la gestión de clientes.
 *
 * Sintaxis:
 * @Composable fun NombrePantalla(parámetros) { ... }
 *
 * Uso:
 * Se llama desde NavHost para navegar a esta pantalla.
 */
@Composable
fun ClientesScreen(

    /**
     * navController
     * -------------
     * ✔ TIPO: NavHostController
     * Es el controlador de navegación que administra rutas y destinos.
     * Sirve para navegar entre pantallas mediante navigate("ruta").
     */
    navController: NavHostController

) {

    /**
     * Scaffold
     * --------
     * ✔ TIPO: Scaffold (estructura de Material Design)
     * Es un contenedor de alto nivel que organiza la pantalla en zonas:
     * barra superior, inferior, FAB y contenido principal.
     * Sirve para estructurar pantallas con un layout coherente.
     *
     * Sintaxis:
     * Scaffold { innerPadding -> ... }
     *
     * Uso:
     * innerPadding debe aplicarse al contenido para evitar solapamientos.
     */
    Scaffold { innerPadding ->

        /**
         * Column
         * ------
         * ✔ TIPO: Column (layout vertical)
         * Es un contenedor que coloca elementos uno debajo del otro.
         * Sirve para organizar contenido en estructura vertical.
         *
         * Sintaxis:
         * Column(modifier = Modifier) { elementos }
         *
         * Uso:
         * Se usa cuando la pantalla debe apilar elementos verticalmente.
         */
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
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(
                    modifier = Modifier.width(16.dp)
                )

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
                    "Activos",
                    10,
                    Color.Green,
                    onClick = {
                        },
                    modifier = Modifier
                        .weight(1f)
                        .padding( 6.dp)
                )

                ResumenCard(
                    "Bajas",
                    10,
                    color = Color.Gray,
                    onClick = {
                        },
                    modifier = Modifier
                        .weight(1f)
                        .padding(6.dp)
                )


                ResumenCard(
                    "Morosos",
                    10,
                    color = Color.Red,
                    onClick = {
                       },
                    modifier = Modifier
                        .weight(1f)
                        .padding(  6.dp)
                )
            }

       }

        }

    }

