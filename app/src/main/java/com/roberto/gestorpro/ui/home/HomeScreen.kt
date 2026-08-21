package com.roberto.gestorpro.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.roberto.gestorpro.navigation.Routes
import com.roberto.gestorpro.ui.components.MenuCard
import com.roberto.gestorpro.ui.viewmodel.MainViewModel
import java.io.File

@Composable
fun HomeScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {

    /**
     * nombreNegocio / logoNegocio
     * ---------------------------
     * ✔ TIPO: variables observables (val by collectAsStateWithLifecycle) → String
     * Son el nombre y la ruta del logo del negocio configurados por el administrador.
     * Sirven para personalizar la cabecera; si están vacíos se muestra "GestorPro"
     * con su icono por defecto.
     */
    val nombreNegocio by mainViewModel.nombreNegocio.collectAsStateWithLifecycle()
    val logoNegocio by mainViewModel.logoNegocio.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            /**
             * Cabecera con identidad del negocio
             * ----------------------------------
             * ✔ TIPO: bloque Row con logo y nombre
             * Es la cabecera principal de la app.
             * Sirve para mostrar el logo y nombre configurados en MiNegocioScreen;
             * sin configuración muestra el icono y el nombre "GestorPro".
             */
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (logoNegocio.isNotBlank()) {

                    /**
                     * logoDelNegocio
                     * --------------
                     * ✔ TIPO: bloque condicional + Composable (coil3.compose.AsyncImage)
                     * Es la imagen del logo cargada desde memoria interna con Coil.
                     * Sirve para identificar visualmente el negocio en la pantalla principal.
                     */
                    AsyncImage(
                        model = File(logoNegocio),
                        contentDescription = "Logo del negocio",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = null,
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = nombreNegocio.ifBlank { "GestorPro" },
                    style = MaterialTheme.typography.headlineMedium
                )
            }

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
                titulo = "Clases",
                descripcion = "Gestión de clases y horarios",
                icono = Icons.Default.Groups,
                iconColor = Color(0xFF1E88E5),
                onClick = { navController.navigate(Routes.CLASES) }
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
