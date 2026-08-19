package com.roberto.clientesgestor.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.roberto.clientesgestor.navigation.Routes
import com.roberto.clientesgestor.ui.components.MenuCard

/**
 * HomeScreen.kt
 * -------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla principal)
 * Es el archivo que define la pantalla de inicio o menú principal.
 * Sirve como punto central desde el que el usuario accede a todas las secciones del gestor.
 */

/**
 * HomeScreen
 * ----------
 * ✔ TIPO: función @Composable
 * Es la pantalla principal de la aplicación con el título y las tarjetas del menú.
 * Sirve para navegar a las secciones de ClientesGestor (Clientes, Cuotas, Pagos, Economía, Configuración).
 */
@Composable
fun HomeScreen(
    /**
     * navController
     * -------------
     * ✔ TIPO: parámetro (param) → NavHostController
     * Es el controlador de navegación que recibe la pantalla de Home.
     * Sirve para poder navegar desde el menú principal a las demás secciones.
     */
    navController: NavHostController
) {

    /**
     * Scaffold
     * --------
     * ✔ TIPO: función @Composable (androidx.compose.material3.Scaffold)
     * Es el contenedor base de la pantalla que aplica los rellenos del sistema.
     * Sirve como estructura general y proporciona el innerPadding para el contenido.
     */
    Scaffold { innerPadding ->

        /**
         * Column
         * ------
         * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
         * Es el contenedor vertical del contenido del menú.
         * Sirve para apilar el título y las tarjetas de MenúCard en orden,
         * permitiendo hacer scroll si el contenido no cabe en pantalla.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            /**
             * Text del título
             * ---------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
             * Es el título principal de la aplicación en la pantalla de inicio.
             * Sirve para mostrar el nombre "ClientesGestor" con un estilo destacado.
             */
            Text(
                text = "ClientesGestor",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            /**
             * Text de bienvenida
             * ------------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
             * Es el saludo de bienvenida que se muestra al usuario.
             * Sirve para dar la bienvenida al menú principal de la aplicación.
             */
            Text(
                text = "Bienvenido",
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            /**
             * MenuCard de Clientes
             * --------------------
             * ✔ TIPO: componente @Composable (MenuCard)
             * Es la tarjeta de acceso a la gestión de clientes.
             * Sirve para navegar a la pantalla de Clientes al pulsarla.
             */
            MenuCard(
                titulo = "Clientes",
                descripcion = "Gestión de clientes ",
                icono = Icons.Default.Person,
                onClick = {
                    navController.navigate(Routes.CLIENTES)
                }
            )

            /**
             * MenuCard de Cuotas
             * ------------------
             * ✔ TIPO: componente @Composable (MenuCard)
             * Es la tarjeta de acceso a la gestión de cuotas.
             * Sirve como entrada a la futura sección de cuotas de la aplicación.
             */
            MenuCard(
                titulo = "Cuotas",
                descripcion = "Gestiona las cuotas",
                icono = Icons.Default.CardMembership,
                onClick = {
                }
            )

            /**
             * MenuCard de Pagos
             * -----------------
             * ✔ TIPO: componente @Composable (MenuCard)
             * Es la tarjeta de acceso a la validación de pagos.
             * Sirve como entrada a la futura sección de pagos de la aplicación.
             */
            MenuCard(
                titulo = "Pagos",
                descripcion = "Valida los pagos",
                icono = Icons.Default.AttachMoney,
                onClick = {
                }
            )

            /**
             * MenuCard de Economía
             * --------------------
             * ✔ TIPO: componente @Composable (MenuCard)
             * Es la tarjeta de acceso a los datos económicos.
             * Sirve como entrada a la futura sección de economía de la aplicación.
             */
            MenuCard(
                titulo = "Economia",
                descripcion = "Datos economicos",
                icono = Icons.Default.AccountBalance,
                onClick = {
                    navController.navigate(Routes.ECONOMIA)
                }
            )

            /**
             * MenuCard de Configuración
             * -------------------------
             * ✔ TIPO: componente @Composable (MenuCard)
             * Es la tarjeta de acceso a los ajustes de la aplicación.
             * Sirve como entrada a la futura sección de configuración.
             */
            MenuCard(
                titulo = "Configuración",
                descripcion = "Ajustes de la aplicación",
                icono = Icons.Default.Settings,
                onClick = {
                }
            )
        }
    }
}
