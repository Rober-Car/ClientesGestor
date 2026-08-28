package com.roberto.gestorpro.cliente.ui.configuracion

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.cliente.data.repository.PreferencesRepository
import com.roberto.gestorpro.cliente.ui.viewmodel.MainViewModel

/**
 * ConfiguracionScreen
 * -------------------
 * Pantalla de ajustes de GestorPro Cliente.
 * Permite cambiar el tema de la aplicación (claro, oscuro o sistema),
 * reutilizando el tema y el estilo visual de GestorPro Admin.
 */
@Composable
fun ConfiguracionScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()

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
                    text = "Configuración",
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
                        onClick = { mainViewModel.setThemeMode(PreferencesRepository.THEME_CLARO) }
                    )
                    TemaOption(
                        label = "Oscuro",
                        selected = themeMode == PreferencesRepository.THEME_OSCURO,
                        onClick = { mainViewModel.setThemeMode(PreferencesRepository.THEME_OSCURO) }
                    )
                    TemaOption(
                        label = "Seguir configuración del sistema",
                        selected = themeMode == PreferencesRepository.THEME_SISTEMA,
                        onClick = { mainViewModel.setThemeMode(PreferencesRepository.THEME_SISTEMA) }
                    )
                }
            }
        }
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
