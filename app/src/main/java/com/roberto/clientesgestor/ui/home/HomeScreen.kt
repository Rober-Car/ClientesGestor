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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    navController: NavHostController
) {

    /**
     * Scaffold
     * --------
     * ✔ TIPO: Scaffold (estructura de Material Design)
     * Es un contenedor de alto nivel que organiza la pantalla en zonas.
     * Sirve para estructurar la pantalla principal con un layout coherente.
     */
    Scaffold { innerPadding ->

        /**
         * Column
         * ------
         * ✔ TIPO: Column (layout vertical)
         * Es un contenedor que coloca elementos uno debajo del otro.
         * Sirve para apilar verticalmente el contenido de la pantalla principal.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            /**
             * Text("ClientesGestor")
             * ----------------------
             * ✔ TIPO: Text
             * Es el título principal de la aplicación.
             * Sirve para identificar la pantalla de inicio.
             */
            Text(
                text = "ClientesGestor",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            /**
             * Spacer
             * ------
             * ✔ TIPO: Spacer
             * Es un elemento invisible que ocupa espacio.
             * Sirve para separar visualmente los componentes.
             */
            Spacer(modifier = Modifier.height(24.dp))

            /**
             * Text("Bienvenido")
             * ------------------
             * ✔ TIPO: Text
             * Es un texto secundario de bienvenida.
             * Sirve para mostrar un mensaje complementario.
             */
            Text(
                text = "Bienvenido",
                modifier = Modifier.padding(start = 16.dp)
            )

            /**
             * Segundo Spacer, igual que el detallado más arriba.
             */
            Spacer(modifier = Modifier.height(24.dp))

            /**
             * MenuCard (Clientes)
             * -------------------
             * ✔ TIPO: Composable personalizado (MenuCard)
             * Es una tarjeta del menú que representa la sección de clientes.
             * Sirve para navegar a la pantalla de gestión de clientes.
             */
            MenuCard(
                titulo = "Clientes",
                descripcion = "Gestión de clientes ",
                Icons.Default.Person,
                onClick = {
                    navController.navigate(Routes.CLIENTES)
                }
            )

            /**
             * MenuCard (Cuotas)
             * -----------------
             * Tarjeta que representa la sección de cuotas.
             * Tipo MenuCard detallado más arriba con "Clientes".
             */
            MenuCard(
                titulo = "Cuotas",
                descripcion = "Gestiona las cuotas",
                Icons.Default.CardMembership,
                onClick = {
                }
            )

            /**
             * MenuCard (Pagos)
             * ----------------
             * Tarjeta que representa la sección de pagos.
             * Tipo MenuCard detallado más arriba con "Clientes".
             */
            MenuCard(
                titulo = "Pagos",
                descripcion = "Valida los pagos",
                Icons.Default.AttachMoney,
                onClick = {
                }
            )

            /**
             * MenuCard (Economía)
             * ------------------
             * Tarjeta que representa la sección de economía.
             * Tipo MenuCard detallado más arriba con "Clientes".
             */
            MenuCard(
                titulo = "Economia",
                descripcion = "Datos economicos",
                Icons.Default.AccountBalance,
                onClick = {
                }
            )

            /**
             * MenuCard (Configuración)
             * ------------------------
             * Tarjeta que representa la sección de configuración.
             * Tipo MenuCard detallado más arriba con "Clientes".
             */
            MenuCard(
                titulo = "Configuración",
                descripcion = "Ajustes de la aplicación",
                Icons.Default.Settings,
                onClick = {
                }
            )
        }
    }
}
