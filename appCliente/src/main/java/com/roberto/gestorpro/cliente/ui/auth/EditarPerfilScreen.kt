package com.roberto.gestorpro.cliente.ui.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.roberto.gestorpro.cliente.ui.utils.guardaFotoEnInterna
import com.roberto.gestorpro.cliente.ui.viewmodel.MainViewModel
import java.io.File
import kotlinx.coroutines.launch

/**
 * EditarPerfilScreen
 * ------------------
 * Formulario de edición de los datos personales de la propia ficha.
 * NO permite modificar DNI ni datos administrativos.
 */
@Composable
fun EditarPerfilScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val operandoRemoto by mainViewModel.operandoRemoto.collectAsStateWithLifecycle()
    val cliente by mainViewModel.cliente.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var nombre by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var foto by rememberSaveable { mutableStateOf("") }
    var fechaNacimiento by rememberSaveable { mutableStateOf("") }
    var mensajeError by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(cliente) {
        val c = cliente ?: return@LaunchedEffect
        if (nombre.isBlank()) nombre = c.nombre
        if (apellidos.isBlank()) apellidos = c.apellidos
        if (telefono.isBlank()) telefono = c.telefono
        if (email.isBlank()) email = c.email ?: ""
        if (foto.isBlank()) foto = c.foto
        if (fechaNacimiento.isBlank()) {
            fechaNacimiento = if (c.fechaNacimiento > 0L) {
                try {
                    java.time.Instant.ofEpochMilli(c.fechaNacimiento)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                } catch (_: Exception) {
                    ""
                }
            } else ""
        }
    }

    val launcherFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val ruta = guardaFotoEnInterna(context, uri)
            if (ruta != null) foto = ruta
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
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
                    text = "Modificar mis datos",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(
                text = "Solo puedes modificar tus datos personales. El DNI y los " +
                    "datos de tu gimnasio no se pueden cambiar desde aquí.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (foto.isNotBlank()) {
                    AsyncImage(
                        model = File(foto),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF1E88E5), CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(
                    onClick = {
                        launcherFoto.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) {
                    Text(if (foto.isNotBlank()) "Cambiar foto" else "Elegir foto")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = fechaNacimiento,
                onValueChange = { fechaNacimiento = it },
                label = { Text("Fecha de nacimiento (dd/MM/aaaa)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (mensajeError.isNotBlank()) {
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    mensajeError = ""
                    scope.launch {
                        val fecha = fechaNacimiento
                            .split("/")
                            .filter { it.isNotBlank() }
                            .mapNotNull { it.toLongOrNull() }
                        val fechaMillis = if (fecha.size == 3) {
                            try {
                                java.time.LocalDate.of(
                                    fecha[2].toInt(),
                                    fecha[1].toInt(),
                                    fecha[0].toInt()
                                )
                                    .atStartOfDay(java.time.ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                            } catch (_: Exception) {
                                0L
                            }
                        } else {
                            0L
                        }

                        val error = mainViewModel.actualizarMisDatosPersonales(
                            nombre = nombre.trim(),
                            apellidos = apellidos.trim(),
                            telefono = telefono.trim(),
                            email = email.trim().ifBlank { null },
                            foto = foto,
                            fechaNacimiento = fechaMillis
                        )
                        if (error != null) {
                            mensajeError = error
                        } else {
                            navController.popBackStack()
                        }
                    }
                },
                enabled = !operandoRemoto && nombre.isNotBlank() && apellidos.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
            ) {
                Text("Guardar cambios", color = Color.White)
            }

            if (operandoRemoto) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
    }
}
