package com.roberto.clientesgestor.ui.clientes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.roberto.clientesgestor.ui.components.ServicioItem

/**
 * PerfilClienteAdministradorScreen.kt
 * ------------------------------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de perfil de cliente)
 * Es el archivo que define la pantalla de perfil de un cliente.
 * Sirve para mostrar los datos y la gestión detallada de cada cliente.
 */

/**
 * PerfilClienteScreen
 * -------------------
 * ✔ TIPO: función @Composable
 * Es la pantalla de perfil del cliente con su estructura base.
 * Sirve para mostrar la información del cliente y su gestión.
 */
@Composable
fun PerfilClienteScreen(
    navController: NavHostController
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Icono de persona",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nombre del Cliente",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Estado",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Icono de teléfono",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Teléfono",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Icono de email",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pollinox@hotmail.com",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = "Icono de DNI",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "4891856456D",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 18.dp
                ),
                thickness = 1.dp,
                color = Color(0xFF64B5F6)
            )

            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Servicios contratados",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                ServicioItem(
                    nombreServicio = "Sala de máquinas",
                    iconoServicio = Icons.Default.FitnessCenter
                )

                ServicioItem(
                    nombreServicio = "CrossFit",
                    iconoServicio = Icons.Default.Bolt
                )
            }
        }
    }
}
