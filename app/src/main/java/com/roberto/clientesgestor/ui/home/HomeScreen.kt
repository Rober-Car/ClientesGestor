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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.roberto.clientesgestor.navigation.Routes
import com.roberto.clientesgestor.ui.components.MenuCard

@Composable
fun HomeScreen(
    navController: NavHostController
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "ClientesGestor",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bienvenido",
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            MenuCard(
                titulo = "Clientes",
                descripcion = "Gestión de clientes",
                icono = Icons.Default.Person,
                iconColor = Color(0xFF1E88E5),
                onClick = { navController.navigate(Routes.CLIENTES) }
            )

            MenuCard(
                titulo = "Economia",
                descripcion = "Datos económicos",
                icono = Icons.Default.AccountBalance,
                iconColor = Color(0xFF1E88E5),
                onClick = { navController.navigate(Routes.ECONOMIA) }
            )

            MenuCard(
                titulo = "Configuración",
                descripcion = "Ajustes de la aplicación",
                icono = Icons.Default.Settings,
                iconColor = Color(0xFF1E88E5),
                onClick = { navController.navigate(Routes.CONFIGURACION) }
            )
        }
    }
}
