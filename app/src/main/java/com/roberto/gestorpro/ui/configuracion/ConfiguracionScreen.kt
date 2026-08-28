package com.roberto.gestorpro.ui.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.roberto.gestorpro.navigation.Routes
import com.roberto.gestorpro.ui.components.MenuCard

@Composable
fun ConfiguracionScreen(
    navController: NavHostController
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ajustes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    MenuCard(
                        titulo = "Mi negocio",
                        descripcion = "Nombre y logo de tu negocio",
                        icono = Icons.Default.AccountBox,
                        containerColor = Color(0xFF1E88E5).copy(alpha = 0.12f),
                        iconContainerColor = Color(0xFF1E88E5),
                        iconTint = Color.White,
                        onClick = { navController.navigate(Routes.MINEGOCIO) }
                    )
                }
                item {
                    MenuCard(
                        titulo = "Preferencias",
                        descripcion = "Ajustes de la aplicación",
                        icono = Icons.Default.Settings,
                        containerColor = Color(0xFF42A5F5).copy(alpha = 0.12f),
                        iconContainerColor = Color(0xFF42A5F5),
                        iconTint = Color.White,
                        onClick = { navController.navigate(Routes.PREFERENCIAS) }
                    )
                }
                item {
                    MenuCard(
                        titulo = "Datos",
                        descripcion = "Gestión de los datos",
                        icono = Icons.Default.Info,
                        containerColor = Color(0xFF607D8B).copy(alpha = 0.12f),
                        iconContainerColor = Color(0xFF607D8B),
                        iconTint = Color.White,
                        onClick = { navController.navigate(Routes.DATOS) }
                    )
                }
                item {
                    MenuCard(
                        titulo = "Cuenta",
                        descripcion = "Seguridad y sesión",
                        icono = Icons.Default.Lock,
                        containerColor = Color(0xFF90A4AE).copy(alpha = 0.12f),
                        iconContainerColor = Color(0xFF90A4AE),
                        iconTint = Color.White,
                        onClick = { navController.navigate(Routes.CUENTA) }
                    )
                }
            }
        }
    }
}
