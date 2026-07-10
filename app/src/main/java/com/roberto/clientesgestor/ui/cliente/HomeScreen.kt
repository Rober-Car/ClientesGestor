package com.roberto.clientesgestor.ui.cliente

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
             * Text("ClientesGestor")
             * ----------------------
             * ✔ TIPO: Composable de texto (Material 3)
             *
             * ¿Qué es?
             * Un componente que muestra texto en pantalla.
             *
             * Parámetros:
             * - text → contenido textual que se va a mostrar.
             * - style → estilo tipográfico aplicado (headlineMedium en este caso).
             * - modifier → ajustes visuales como padding, tamaño, alineación, etc.
             */
            Text(
                text = "ClientesGestor",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )


            Spacer(modifier = Modifier.height(24.dp))

            /**
             * Text("Bienvenido")
             * ------------------
             * ✔ TIPO: Composable de texto
             *
             * ¿Qué es?
             * Un texto secundario que complementa el título principal.
             *
             * Parámetros:
             * - text → contenido textual.
             * - modifier → en este caso, padding horizontal desde la izquierda.
             */
            Text(
                text = "Bienvenido",
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier= Modifier.height(24.dp))




                Card (

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)

                ){

                    Text(
                        text = "👥 Clientes",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )

                    Text(
                        text = "Gestiona los clientes del gimnasio",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                    )

                }

                Card(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)

                ){

                    Text(
                        text = " \uD83D\uDCB6 Cuotas" ,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )

                    Text(

                        text = "Gestiona las cuotas",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                    )
                }

                Card(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)

                ){

                    Text(
                        text = "\uD83D\uDCB3 Pagos " ,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )

                    Text(

                        text = "Valida los pagos ",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                    )
                }

                Card(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)

                ){

                    Text(
                        text = "⚙ Configuración" ,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )

                    Text(

                        text = "Ajustes de la aplicación",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                    )
                }


            }

        }

    }


