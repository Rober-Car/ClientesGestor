package com.roberto.gestorpro.ui.configuracion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.roberto.gestorpro.navigation.Routes

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
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            /**
             * Opción Mi negocio
             * -----------------
             * ✔ TIPO: llamada a Composable (ConfiguracionItem)
             * Es la tarjeta de personalización de la identidad del negocio.
             * Sirve para que el administrador configure el nombre y el logo
             * que se muestran en las cabeceras de Home y Login.
             */
            ConfiguracionItem(
                titulo = "Mi negocio",
                descripcion = "Nombre y logo de tu negocio",
                icono = Icons.Default.AccountBox,
                iconColor = Color(0xFF1E88E5),
                onClick = { navController.navigate(Routes.MINEGOCIO) }
            )

            ConfiguracionItem(
                titulo = "Preferencias",
                descripcion = "Ajustes de la aplicación",
                icono = Icons.Default.Settings,
                iconColor = Color(0xFF1E88E5),
                onClick = { navController.navigate(Routes.PREFERENCIAS) }
            )

            ConfiguracionItem(
                titulo = "Datos",
                descripcion = "Gestión de los datos",
                icono = Icons.Default.Info,
                iconColor = Color(0xFF1E88E5),
                onClick = { navController.navigate(Routes.DATOS) }
            )

            ConfiguracionItem(
                titulo = "Cuenta",
                descripcion = "Seguridad y sesión",
                icono = Icons.Default.Lock,
                iconColor = Color(0xFF1E88E5),
                onClick = { navController.navigate(Routes.CUENTA) }
            )
        }
    }
}

@Composable
private fun ConfiguracionItem(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
