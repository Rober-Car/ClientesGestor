package com.roberto.gestorpro.cliente.navigation

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
import com.roberto.gestorpro.cliente.ui.auth.CuentaScreen
import com.roberto.gestorpro.cliente.ui.auth.CompletarPerfilScreen
import com.roberto.gestorpro.cliente.ui.auth.EditarPerfilScreen
import com.roberto.gestorpro.cliente.ui.auth.InicioScreen
import com.roberto.gestorpro.cliente.ui.auth.LoginScreen
import com.roberto.gestorpro.cliente.ui.auth.MiPerfilScreen
import com.roberto.gestorpro.cliente.ui.auth.RecuperarPasswordScreen
import com.roberto.gestorpro.cliente.ui.auth.RegistroScreen
import com.roberto.gestorpro.cliente.ui.home.HomeScreen
import com.roberto.gestorpro.cliente.ui.viewmodel.MainViewModel

/**
 * AppNavigation
 * -------------
 * ✔ TIPO: función @Composable
 * NavHost de GestorPro Cliente.
 */
@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()

    var destinoInicial by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        destinoInicial = mainViewModel.destinoInicial()
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

        composable(Routes.INICIO) {
            InicioScreen(navController)
        }

        composable(Routes.COMPLETAR_PERFIL) {
            CompletarPerfilScreen(navController)
        }

        composable(Routes.HOME) {
            HomeScreen(navController)
        }

        composable(Routes.MI_PERFIL) {
            MiPerfilScreen(navController)
        }

        composable(Routes.EDITAR_PERFIL) {
            EditarPerfilScreen(navController)
        }

        composable(Routes.CUENTA) {
            CuentaScreen(navController)
        }
    }
}
