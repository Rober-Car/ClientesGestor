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
     * Scaffold
     * --------
     * ✔ TIPO: Scaffold (estructura de Material Design)
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
                        modifier = Modifier.size(24.dp)
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
                 * ResumenCard (Activos)
                 * --------------------
                 * ✔ TIPO: Composable personalizado (ResumenCard)
                 * Es una tarjeta que muestra el número de clientes activos.
                 * Sirve para resumir de un vistazo los clientes activos.
                 */
                ResumenCard(
                    "Activos",
                    10,
                    Color.Green,
                    onClick = {
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(6.dp)
                )

                /**
                 * ResumenCard (Bajas)
                 * -------------------
                 * ✔ TIPO: Composable personalizado (ResumenCard)
                 * Es una tarjeta que muestra el número de clientes dados de baja.
                 * Sirve para resumir de un vistazo los clientes de baja.
                 */
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

                /**
                 * ResumenCard (Morosos)
                 * --------------------
                 * ✔ TIPO: Composable personalizado (ResumenCard)
                 * Es una tarjeta que muestra el número de clientes morosos.
                 * Sirve para resumir de un vistazo los clientes morosos.
                 */
                ResumenCard(
                    "Morosos",
                    10,
                    color = Color.Red,
                    onClick = {
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(6.dp)
                )
            }
        }
    }
}
