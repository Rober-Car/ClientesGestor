package com.roberto.gestorpro.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.roberto.gestorpro.ui.auth.LoginScreen
import com.roberto.gestorpro.ui.auth.RecuperarPasswordScreen
import com.roberto.gestorpro.ui.auth.RegistroScreen
import com.roberto.gestorpro.ui.clases.ClasesScreen
import com.roberto.gestorpro.ui.clases.CrearClaseScreen
import com.roberto.gestorpro.ui.clases.DetalleClaseScreen
import com.roberto.gestorpro.ui.clases.DetalleSesionReservasScreen
import com.roberto.gestorpro.ui.clientes.AñadirClienteScreen
import com.roberto.gestorpro.ui.clientes.ClientesScreen
import com.roberto.gestorpro.ui.clientes.PerfilClienteScreen
import com.roberto.gestorpro.ui.configuracion.ConfiguracionScreen
import com.roberto.gestorpro.ui.configuracion.CrearNegocioScreen
import com.roberto.gestorpro.ui.configuracion.CuentaScreen
import com.roberto.gestorpro.ui.configuracion.DatosScreen
import com.roberto.gestorpro.ui.configuracion.MiNegocioScreen
import com.roberto.gestorpro.ui.configuracion.PreferenciasScreen
import com.roberto.gestorpro.ui.economia.EconomiaScreen
import com.roberto.gestorpro.ui.home.HomeScreen
import com.roberto.gestorpro.ui.viewmodel.MainViewModel

/**
 * AppNavigation.kt
 * ----------------
 * ✔ TIPO: archivo de código fuente Kotlin (navegación)
 * Es el NavHost de GestorPro Admin. Centraliza rutas y pantallas del
 * administrador. (Las pantallas de CLIENTE y la Vía B quedaron fuera.)
 */

/**
 * AppNavigation
 * -------------
 * ✔ TIPO: función @Composable
 * Prepara la navegación de GestorPro Admin usando Jetpack Compose.
 * Arranca en Login (sin sesión) o en Home (sesión restaurada).
 */
@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val mainViewModel: MainViewModel = hiltViewModel()

    var destinoInicial by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        destinoInicial = mainViewModel.destinoInicialSegunSesion()
    }

    val destino = destinoInicial
    if (destino == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = destino
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }

        composable(Routes.REGISTRO) {
            RegistroScreen(navController)
        }

        composable(Routes.RECUPERAR_PASSWORD) {
            RecuperarPasswordScreen(navController)
        }

        composable(Routes.HOME) {
            HomeScreen(navController)
        }

        composable(Routes.CLIENTES) {
            ClientesScreen(navController)
        }

        composable(
            route = "${Routes.PERFILCLIENTE}/{idCliente}"
        ) { backStackEntry ->

            val idCliente = backStackEntry.arguments
                ?.getString("idCliente")
                ?.toIntOrNull()

            if (idCliente != null) {
                PerfilClienteScreen(
                    navController = navController,
                    idCliente = idCliente
                )
            }
        }

        composable(Routes.AÑADIRCLIENTE) {

            AñadirClienteScreen(navController)
        }

        composable(
            route = "${Routes.MODIFICARCLIENTE}/{idCliente}"
        ) { backStackEntry ->

            val idCliente = backStackEntry.arguments
                ?.getString("idCliente")
                ?.toIntOrNull()

            if (idCliente != null) {
                AñadirClienteScreen(
                    navController = navController,
                    idCliente = idCliente
                )
            }
        }

        composable(Routes.ECONOMIA) {
            EconomiaScreen(navController)
        }

        composable(Routes.CONFIGURACION) {
            ConfiguracionScreen(navController)
        }

        composable(Routes.MINEGOCIO) {
            MiNegocioScreen(navController)
        }

        composable(Routes.CREAR_NEGOCIO) {
            CrearNegocioScreen(navController)
        }

        composable(Routes.PREFERENCIAS) {
            PreferenciasScreen(navController)
        }

        composable(Routes.DATOS) {
            DatosScreen(navController)
        }

        composable(Routes.CUENTA) {
            CuentaScreen(navController)
        }

        composable(Routes.CLASES) {
            ClasesScreen(navController)
        }

        composable(Routes.CREAR_CLASE) {
            CrearClaseScreen(navController)
        }

        composable(
            route = "${Routes.DETALLE_CLASE}/{idClase}"
        ) { backStackEntry ->
            val idClase = backStackEntry.arguments
                ?.getString("idClase")
                ?.toIntOrNull()
            if (idClase != null) {
                DetalleClaseScreen(
                    navController = navController,
                    idClase = idClase
                )
            }
        }

        composable(
            route = "${Routes.DETALLE_SESION_RESERVAS}/{idSesion}"
        ) { backStackEntry ->
            val idSesion = backStackEntry.arguments
                ?.getString("idSesion")
                ?.toIntOrNull()
            if (idSesion != null) {
                DetalleSesionReservasScreen(
                    navController = navController,
                    idSesion = idSesion
                )
            }
        }

    }
}
