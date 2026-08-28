package com.roberto.gestorpro.cliente.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.cliente.navigation.Routes
import com.roberto.gestorpro.cliente.ui.components.MenuCard
import com.roberto.gestorpro.cliente.ui.viewmodel.MainViewModel

/**
 * HomeScreen
 * ----------
 * Menú principal del CLIENTE ya vinculado.
 */
@Composable
fun HomeScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val nombreNegocio by mainViewModel.nombreNegocio.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "GestorPro Cliente",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = nombreNegocio.ifBlank { "Tu gimnasio" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            MenuCard(
                titulo = "Mi perfil",
                descripcion = "Ver y editar mis datos personales",
                icono = Icons.Default.Person,
                onClick = { navController.navigate(Routes.MI_PERFIL) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCard(
                titulo = "Mis reservas",
                descripcion = "Próximamente",
                icono = Icons.Default.Badge,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCard(
                titulo = "Mi cuenta",
                descripcion = "Cerrar sesión",
                icono = Icons.Default.Settings,
                onClick = { navController.navigate(Routes.CUENTA) }
            )
        }
    }
}
