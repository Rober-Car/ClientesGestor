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
import com.roberto.gestorpro.ui.notificaciones.ConfigNotificacionesScreen
import com.roberto.gestorpro.ui.notificaciones.CrearNotificacionScreen
import com.roberto.gestorpro.ui.notificaciones.GestionNotificacionesScreen
import com.roberto.gestorpro.ui.notificaciones.SeleccionarClientesScreen
import com.roberto.gestorpro.ui.servicios.DetalleServicioScreen
import com.roberto.gestorpro.ui.servicios.EditarServicioScreen
import com.roberto.gestorpro.ui.servicios.EditarSesionScreen
import com.roberto.gestorpro.ui.servicios.ProgramarSesionesScreen
import com.roberto.gestorpro.ui.servicios.ServiciosScreen
import com.roberto.gestorpro.ui.servicios.SesionReservasScreen
import com.roberto.gestorpro.ui.viewmodel.MainViewModel
import com.roberto.gestorpro.ui.viewmodel.NotificacionesViewModel

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

    // ViewModel compartido por las pantallas de notificaciones (lista, crear,
    // configuración): al obtenerlo a nivel de AppNavigation se mantiene una
    // única instancia durante toda la navegación de notificaciones.
    val notificacionesViewModel: NotificacionesViewModel = hiltViewModel()

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

        composable(Routes.SERVICIOS) {
            ServiciosScreen(navController)
        }

        composable(Routes.CREAR_SERVICIO) {
            EditarServicioScreen(navController)
        }

        composable(
            route = "${Routes.EDITAR_SERVICIO}/{idServicio}"
        ) { backStackEntry ->
            val idServicio = backStackEntry.arguments
                ?.getString("idServicio")
                ?.toIntOrNull()
            if (idServicio != null) {
                EditarServicioScreen(
                    navController = navController,
                    idServicio = idServicio
                )
            }
        }

        composable(
            route = "${Routes.DETALLE_SERVICIO}/{idServicio}"
        ) { backStackEntry ->
            val idServicio = backStackEntry.arguments
                ?.getString("idServicio")
                ?.toIntOrNull()
            if (idServicio != null) {
                DetalleServicioScreen(
                    navController = navController,
                    idServicio = idServicio
                )
            }
        }

        composable(
            route = "${Routes.PROGRAMAR_SESIONES}/{idServicio}"
        ) { backStackEntry ->
            val idServicio = backStackEntry.arguments
                ?.getString("idServicio")
                ?.toIntOrNull()
            if (idServicio != null) {
                ProgramarSesionesScreen(
                    navController = navController,
                    idServicio = idServicio
                )
            }
        }

        composable(
            route = "${Routes.SESION_RESERVAS}/{idSesion}"
        ) { backStackEntry ->
            val idSesion = backStackEntry.arguments
                ?.getString("idSesion")
                ?.toIntOrNull()
            if (idSesion != null) {
                SesionReservasScreen(
                    navController = navController,
                    idSesion = idSesion
                )
            }
        }

        composable(
            route = "${Routes.EDITAR_SESION}/{idSesion}"
        ) { backStackEntry ->
            val idSesion = backStackEntry.arguments
                ?.getString("idSesion")
                ?.toIntOrNull()
            if (idSesion != null) {
                EditarSesionScreen(
                    navController = navController,
                    idSesion = idSesion
                )
            }
        }

        composable(Routes.NOTIFICACIONES) {
            GestionNotificacionesScreen(
                navController = navController,
                viewModel = notificacionesViewModel
            )
        }

        composable(Routes.CREAR_NOTIFICACION) {
            CrearNotificacionScreen(
                navController = navController,
                viewModel = notificacionesViewModel
            )
        }

        composable(Routes.CONFIG_NOTIFICACIONES) {
            ConfigNotificacionesScreen(
                navController = navController,
                viewModel = notificacionesViewModel
            )
        }

        composable(Routes.SELECCIONAR_CLIENTES) {
            SeleccionarClientesScreen(
                navController = navController,
                viewModel = notificacionesViewModel
            )
        }

    }
}
