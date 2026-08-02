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
 * HomeScreen
 * ----------
 * ✔ TIPO: función @Composable
 * Es una función declarativa que Jetpack Compose usa para dibujar interfaz.
 * Sirve para mostrar la pantalla principal de la aplicación.
 *
 * Sintaxis:
 * @Composable fun NombrePantalla(parámetros) { ... }
 *
 * Uso:
 * Se llama desde NavHost para mostrar esta pantalla.
 */
@Composable
fun HomeScreen (

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
     * Sirve para estructurar pantallas complejas con un layout coherente.
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
                .verticalScroll(rememberScrollState())
        ) {

            /**
             * Text("ClientesGestor")
             * ----------------------
             * ✔ TIPO: Text (Composable)
             * Es un componente que muestra texto en pantalla.
             * Sirve para mostrar títulos, etiquetas o contenido textual.
             *
             * Sintaxis:
             * Text(text = "...", style = ..., modifier = ...)
             *
             * Uso:
             * Se usa para mostrar texto con estilos de MaterialTheme.
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
             * Sirve para separar visualmente componentes.
             *
             * Sintaxis:
             * Spacer(modifier = Modifier.height(XX.dp))
             */
            Spacer(modifier = Modifier.height(24.dp))

            /**
             * Text("Bienvenido")
             * ------------------
             * ✔ TIPO: Text
             * Es un texto secundario.
             * Sirve para mostrar un mensaje complementario.
             *
             * (Detalles completos ya explicados en la primera aparición de Text)
             */
            Text(
                text = "Bienvenido",
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            /**
             * MenuCard
             * --------
             * ✔ TIPO: Composable personalizado
             * Es un componente reutilizable que encapsula una Card con título y descripción.
             * Sirve para evitar repetir código y mantener la interfaz consistente.
             *
             * Sintaxis:
             * MenuCard(titulo = "...", descripcion = "...")
             *
             * Uso:
             * Se usa para representar opciones del menú principal.
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
             * MenuCard (segunda aparición)
             * ----------------------------
             */
            MenuCard(
                titulo = "Cuotas",
                descripcion = "Gestiona las cuotas",
                Icons.Default.CardMembership,
                onClick = {


                }
            )

            /**
             * MenuCard (tercera aparición)
             * ----------------------------
             */
            MenuCard(
                titulo = "Pagos",
                descripcion="Valida los pagos",
                Icons.Default.AttachMoney,
                onClick = {


                }
            )


            MenuCard(
                titulo = "Economia",
                descripcion = "Datos economicos",
                Icons.Default.AccountBalance,
                onClick = {


                }
            )

            /**
             * MenuCard (cuarta aparición)
             * ---------------------------
             */
            MenuCard(
                titulo="Configuración",
                descripcion="Ajustes de la aplicación",
                Icons.Default.Settings,
                onClick = {


                }
            )

        }
    }
}
