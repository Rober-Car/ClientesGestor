package com.roberto.gestorpro.cliente.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.roberto.gestorpro.cliente.navigation.Routes
import com.roberto.gestorpro.cliente.ui.components.MenuCard
import com.roberto.gestorpro.cliente.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val idCliente by mainViewModel.idCliente.collectAsStateWithLifecycle()
    val nombreNegocio by mainViewModel.nombreNegocio.collectAsStateWithLifecycle()
    val logoNegocio by mainViewModel.logoNegocio.collectAsStateWithLifecycle()
    val vinculado = idCliente != null

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            if (logoNegocio.isNotBlank()) {
                AsyncImage(
                    model = logoNegocio,
                    contentDescription = "Logo del gimnasio",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "GestorPro Cliente",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (vinculado) {
                    nombreNegocio.ifBlank { "Tu gimnasio" }
                } else {
                    "Todavía no estás vinculado a un gimnasio"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!vinculado) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "No estás vinculado con tu gimnasio.\n" +
                            "Debes vincularte para ver las clases.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            MenuCard(
                titulo = "Mi perfil",
                descripcion = "Ver y editar mis datos",
                icono = Icons.Default.Person,
                onClick = { navController.navigate(Routes.MI_PERFIL) }
            )

            MenuCard(
                titulo = "Clases y sesiones",
                descripcion = "Clases disponibles",
                icono = Icons.Default.FitnessCenter,
                onClick = { navController.navigate(Routes.CLASES) }
            )

            if (!vinculado) {
                MenuCard(
                    titulo = "Vinculación",
                    descripcion = "Qué has hecho",
                    icono = Icons.Default.Badge,
                    onClick = { navController.navigate(Routes.INICIO) }
                )
            }

            MenuCard(
                titulo = "Mi cuenta",
                descripcion = "Cómo está",
                icono = Icons.Default.AccountCircle,
                onClick = { navController.navigate(Routes.CUENTA) }
            )

            MenuCard(
                titulo = "Configuración",
                descripcion = "Tema claro u oscuro",
                icono = Icons.Default.Settings,
                onClick = { navController.navigate(Routes.CONFIGURACION) }
            )
        }
    }
}
