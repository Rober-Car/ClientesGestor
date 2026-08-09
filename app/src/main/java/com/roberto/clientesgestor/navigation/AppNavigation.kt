package com.roberto.clientesgestor.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.roberto.clientesgestor.ui.auth.LoginScreen
import com.roberto.clientesgestor.ui.clientes.ClientesScreen
import com.roberto.clientesgestor.ui.clientes.PerfilClienteScreen
import com.roberto.clientesgestor.ui.home.HomeScreen

/**
 * AppNavigation.kt
 * ----------------
 * ✔ TIPO: archivo de código fuente Kotlin (navegación)
 * Es el archivo encargado de configurar la navegación entre pantallas.
 * Sirve para tener un punto central donde se definen rutas, pantallas y el flujo de navegación de la app.
 */

/**
 * AppNavigation
 * -------------
 * ✔ TIPO: función @Composable
 * Es la función que prepara la navegación de la aplicación usando Jetpack Compose.
 * Sirve para crear el NavHost y mover al usuario entre las pantallas del proyecto.
 */
@Composable
fun AppNavigation() {

    /**
     * navController
     * -------------
     * ✔ TIPO: variable inmutable (val) → NavController
     * Es el objeto que controla la navegación entre pantallas.
     * Sirve para guardar el historial, navegar a otras rutas y volver atrás.
     */
    val navController = rememberNavController()

    /**
     * NavHost
     * -------
     * ✔ TIPO: función @Composable (androidx.navigation.compose.NavHost)
     * Es el contenedor principal donde se registran todas las rutas de la aplicación.
     * Sirve como el "mapa" que indica qué pantalla se muestra según la ruta actual.
     */
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {

        /**
         * Ruta LOGIN
         * ----------
         * ✔ TIPO: ruta de navegación (composable)
         * Es la ruta de inicio que muestra la pantalla de Login.
         * Sirve para que al abrir la aplicación se muestre el inicio de sesión
         * y se pase el navController para poder navegar al Home.
         */
        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }

        /**
         * Ruta HOME
         * ---------
         * ✔ TIPO: ruta de navegación (composable)
         * Es la ruta que muestra la pantalla de inicio o menú principal.
         * Sirve para que desde Login se llegue al menú principal
         * y se pase el navController para poder navegar al resto de pantallas.
         */
        composable(Routes.HOME) {
            HomeScreen(navController)
        }

        /**
         * Ruta CLIENTES
         * -------------
         * ✔ TIPO: ruta de navegación (composable)
         * Es la ruta que muestra la pantalla de la lista de clientes.
         * Sirve para que desde el menú principal se acceda a la gestión de clientes
         * y se pase el navController para poder navegar al perfil de un cliente.
         */
        composable(Routes.CLIENTES) {
            ClientesScreen(navController)
        }

        /**
         * Ruta PERFILCLIENTE
         * ------------------
         * ✔ TIPO: ruta de navegación (composable)
         * Es la ruta que muestra la pantalla de perfil de un cliente.
         * Sirve para que desde la lista de clientes se acceda a los detalles
         * y se pase el navController para poder volver atrás.
         */
        composable(Routes.PERFILCLIENTE) {
            PerfilClienteScreen(navController)
        }
    }
}
