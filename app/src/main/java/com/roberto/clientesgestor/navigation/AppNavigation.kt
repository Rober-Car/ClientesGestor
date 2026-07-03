package com.roberto.clientesgestor.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.roberto.clientesgestor.ui.auth.LoginScreen
import com.roberto.clientesgestor.ui.cliente.HomeScreen


/**
 * AppNavigation
 * -------------
 * ✔ TIPO: función @Composable
 *
 * ¿Qué es?
 * Una función que prepara la navegación de la aplicación usando Jetpack Compose.
 *
 * ¿Qué hace?
 * Crea y recuerda un controlador de navegación (NavController),
 * que será el encargado de mover al usuario entre pantallas.
 *
 * ¿Para qué sirve?
 * Para tener un punto central donde configurar rutas, pantallas
 * y el flujo de navegación de la app.
 */
@Composable
fun AppNavigation() {

    /**
     * navController
     * -------------
     * ✔ TIPO: variable (val → inmutable)
     * ✔ TIPO REAL: NavController
     * ✔ CLASE: androidx.navigation.NavController
     *
     * ¿Qué es?
     * El objeto que controla la navegación entre pantallas.
     *
     * ¿Qué hace?
     * - Guarda el historial de pantallas.
     * - Permite navegar a otras rutas.
     * - Permite volver atrás.
     *
     * rememberNavController()
     * -----------------------
     * ✔ TIPO: función @Composable
     * ✔ CLASE: androidx.navigation.compose.rememberNavController
     *
     * ¿Qué hace?
     * Crea un NavController y lo recuerda entre recomposiciones.
     *
     * ¿Para qué sirve?
     * Para que el controlador de navegación no se reinicie
     * cada vez que Compose redibuja la UI.
     */
    val navController = rememberNavController()

    /**
     * NavHost
     * -------
     * ✔ TIPO: función @Composable
     * ✔ CLASE: androidx.navigation.compose.NavHost
     *
     * ¿Qué es?
     * El contenedor principal donde se registran todas las rutas de navegación
     * de la aplicación. Es el “mapa” que indica qué pantalla se muestra según la ruta.
     *
     * ¿Qué hace?
     * - Recibe un NavController para gestionar la navegación.
     * - Define la pantalla inicial mediante startDestination.
     * - Dentro de sus llaves se añaden las rutas con composable().
     *
     * navController = navController
     * -----------------------------
     * ✔ TIPO: parámetro
     * ✔ VALOR: instancia de NavController creada previamente
     *
     * ¿Qué hace?
     * Indica qué controlador de navegación debe usar este NavHost.
     *
     * startDestination = Routes.LOGIN
     * -------------------------------
     * ✔ TIPO: parámetro
     * ✔ VALOR: constante que representa la ruta inicial
     *
     * ¿Qué hace?
     * Define la primera pantalla que se mostrará cuando la app arranca.
     */
    NavHost(

        navController = navController,
        startDestination = Routes.LOGIN
    ){

        composable(Routes.LOGIN){
            LoginScreen(navController)

            }

        composable(Routes.HOME){

            HomeScreen(navController)
    }

}
}