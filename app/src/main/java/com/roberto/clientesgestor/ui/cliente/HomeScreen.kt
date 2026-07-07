package com.roberto.clientesgestor.ui.cliente

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * HomeScreen
 * ----------
 * ✔ TIPO: función @Composable
 * ¿Qué es?
 * Una pantalla de la app que se muestra al navegar a HOME.
 *
 * ¿Qué hace?
 * Dibuja la interfaz usando Scaffold y Column.
 *
 * Parámetro navController
 * -----------------------
 * ✔ TIPO: NavHostController
 * ¿Para qué sirve?
 * Permite navegar a otras pantallas desde HomeScreen.
 */
@Composable
fun HomeScreen (

    /**
     * navController
     * -------------
     * El controlador de navegación que gestiona las rutas.
     *
     * ¿Para qué sirve?
     *  permite navegar a otras pantallas  desde esta pantalla si fuese necesario.
     */
    navController: NavHostController

) {

    /**
     * Scaffold
     * --------
     * ✔ TIPO: Composable de Material Design
     *
     * ¿Qué es?
     * Un contenedor que estructura la pantalla.
     *
     * ¿Qué hace?
     * Proporciona espacios como barras superiores, inferiores,
     * FAB, y un área de contenido con padding automático.
     *
     * innerPadding
     * ------------
     * ✔ TIPO: PaddingValues
     * ¿Qué hace?
     * Indica el padding interno que Scaffold aplica al contenido.
     */
    Scaffold { innerPadding ->

        /**
         * Column
         * ------
         * ✔ TIPO: layout vertical
         *
         * ¿Qué es?
         * Un contenedor que coloca elementos uno debajo del otro.
         *
         * modifier
         * --------
         * ✔ fillMaxSize() → ocupa toda la pantalla.
         * ✔ padding(innerPadding) → aplica el padding del Scaffold.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            /**
             * Text("Home Screen")
             * ------------------
             * ✔ TIPO: Composable de texto
             *
             * ¿Qué hace?
             * Muestra el texto “Home Screen” en pantalla.
             *
             * ¿Para qué sirve?
             * Indica al usuario que está en la pantalla principal.
             */
            Text(
                text = "ClientesGestor",
                modifier = Modifier.padding(16.dp)
            )

            Text(
                text = "Bienvenido",
                modifier = Modifier.padding(start = 16.dp)
            )

        }

    }

}
