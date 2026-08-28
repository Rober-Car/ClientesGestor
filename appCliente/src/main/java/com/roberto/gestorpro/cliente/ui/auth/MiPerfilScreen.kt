package com.roberto.gestorpro.cliente.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.roberto.gestorpro.cliente.data.firebase.PerfilPendiente
import com.roberto.gestorpro.cliente.model.Cliente
import com.roberto.gestorpro.cliente.navigation.Routes
import com.roberto.gestorpro.cliente.ui.viewmodel.MainViewModel
import java.io.File

/**
 * MiPerfilScreen
 * --------------
 * Muestra el perfil del CLIENTE:
 *   - vinculado: la ficha clientes/{idCliente} (sin observaciones ni datos admin);
 *   - sin vincular: el perfil pendiente de perfiles_pendientes/{uid}.
 */
@Composable
fun MiPerfilScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val idCliente by mainViewModel.idCliente.collectAsStateWithLifecycle()
    val cliente by mainViewModel.cliente.collectAsStateWithLifecycle()
    val perfilPendiente by mainViewModel.perfilPendiente.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        mainViewModel.cargarPerfilVista()
    }

    val datos = if (idCliente != null) {
        cliente?.let { DatosPerfilMostrar.from(it) }
    } else {
        perfilPendiente?.let { DatosPerfilMostrar.from(it) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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
                    text = "Mi perfil",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (datos == null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (datos.foto.isNotBlank()) {
                    AsyncImage(
                        model = File(datos.foto),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color(0xFF1E88E5), CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${datos.nombre} ${datos.apellidos}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                if (datos.estadoTexto != null) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF1E88E5).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = datos.estadoTexto,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF1E88E5),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Datos personales",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1E88E5)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilaDatoPerfilCliente(
                icono = Icons.Default.Badge,
                etiqueta = "DNI",
                valor = datos.dni
            )
            FilaDatoPerfilCliente(
                icono = Icons.Default.Phone,
                etiqueta = "Teléfono",
                valor = datos.telefono
            )
            FilaDatoPerfilCliente(
                icono = Icons.Default.Email,
                etiqueta = "Email",
                valor = datos.email ?: "Sin email"
            )
            FilaDatoPerfilCliente(
                icono = Icons.Default.DateRange,
                etiqueta = "Fecha de nacimiento",
                valor = datos.fechaNacimiento.let { formatearFecha(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate(Routes.EDITAR_PERFIL) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text("Modificar mis datos", color = Color.White)
            }
        }
    }
}

/**
 * DatosPerfilMostrar
 * ------------------
 * Datos mínimos del perfil para mostrar tanto desde clientes/{idCliente}
 * (vinculado) como desde perfiles_pendientes/{uid} (sin vincular).
 */
private data class DatosPerfilMostrar(
    val nombre: String,
    val apellidos: String,
    val dni: String,
    val telefono: String,
    val email: String?,
    val foto: String,
    val fechaNacimiento: Long,
    val estadoTexto: String?
) {
    companion object {
        fun from(c: Cliente) = DatosPerfilMostrar(
            nombre = c.nombre,
            apellidos = c.apellidos,
            dni = c.dni,
            telefono = c.telefono,
            email = c.email,
            foto = c.foto,
            fechaNacimiento = c.fechaNacimiento,
            estadoTexto = estadoTexto(c.estado.name)
        )

        fun from(p: PerfilPendiente) = DatosPerfilMostrar(
            nombre = p.nombre,
            apellidos = p.apellidos,
            dni = p.dni,
            telefono = p.telefono,
            email = p.email,
            foto = p.foto,
            fechaNacimiento = p.fechaNacimiento,
            estadoTexto = null
        )

        private fun estadoTexto(estado: String): String {
            return when (estado) {
                "ACTIVO" -> "Activo"
                "MOROSO" -> "Moroso"
                "BAJA" -> "Baja"
                "ARCHIVADO" -> "Archivado"
                else -> "Registrado"
            }
        }
    }
}

@Composable
private fun FilaDatoPerfilCliente(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    etiqueta: String,
    valor: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = Color(0xFF1E88E5), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun formatearFecha(millis: Long): String {
    if (millis <= 0L) return "Sin especificar"
    return try {
        java.time.Instant.ofEpochMilli(millis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (_: Exception) {
        "Sin especificar"
    }
}
