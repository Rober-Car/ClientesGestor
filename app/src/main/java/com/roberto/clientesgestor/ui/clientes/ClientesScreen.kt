package com.roberto.clientesgestor.ui.clientes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController


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

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    modifier = Modifier.size(24.dp)
                )

        }


       }

        }

    }

