package com.roberto.gestorpro.ui.configuracion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.data.repository.PreferencesRepository
import com.roberto.gestorpro.navigation.Routes
import com.roberto.gestorpro.ui.viewmodel.MainViewModel
import com.roberto.gestorpro.ui.viewmodel.PreferenciasViewModel

@Composable
fun PreferenciasScreen(
    navController: NavHostController,
    viewModel: PreferenciasViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    /**
     * mostrarDialogoCerrarSesion
     * --------------------------
     * ✔ TIPO: variable observable (var by mutableStateOf) → Boolean
     * Es el estado que controla si se muestra el diálogo de confirmación.
     * Sirve para que cerrar sesión no ocurra por un toque accidental.
     */
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

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
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Preferencias",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(
                text = "Apariencia",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF1E88E5),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    TemaOption(
                        label = "Claro",
                        selected = themeMode == PreferencesRepository.THEME_CLARO,
                        onClick = { viewModel.setThemeMode(PreferencesRepository.THEME_CLARO) }
                    )
                    TemaOption(
                        label = "Oscuro",
                        selected = themeMode == PreferencesRepository.THEME_OSCURO,
                        onClick = { viewModel.setThemeMode(PreferencesRepository.THEME_OSCURO) }
                    )
                    TemaOption(
                        label = "Seguir configuración del sistema",
                        selected = themeMode == PreferencesRepository.THEME_SISTEMA,
                        onClick = { viewModel.setThemeMode(PreferencesRepository.THEME_SISTEMA) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            /**
             * Botón Cerrar sesión
             * -------------------
             * ✔ TIPO: Composable (TextButton)
             * Es la salida de sesión disponible para cualquier perfil (admin o cliente).
             * Sirve para volver a la pantalla de Login limpiando todo el historial
             * de navegación; pide confirmación antes de actuar.
             */
            TextButton(
                onClick = { mostrarDialogoCerrarSesion = true },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Cerrar sesión",
                    color = Color(0xFFF44336),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    /**
     * Diálogo Cerrar sesión
     * ---------------------
     * ✔ TIPO: condición + Composable (AlertDialog)
     * Es el diálogo de confirmación antes de cerrar la sesión.
     * Sirve para evitar cierres accidentales; al confirmar, navega al Login
     * vaciando por completo la pila de navegación.
     */
    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            title = {
                Text(
                    text = "Cerrar sesión",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF1E88E5),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text("¿Seguro que quieres cerrar sesión?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoCerrarSesion = false
                        mainViewModel.cerrarSesion()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text("Cerrar sesión", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TemaOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF1E88E5)
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
