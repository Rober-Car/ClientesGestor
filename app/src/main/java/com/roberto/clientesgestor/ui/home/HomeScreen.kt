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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.roberto.clientesgestor.navigation.Routes
import com.roberto.clientesgestor.ui.components.MenuCard

/**
 * HomeScreen.kt
 * -------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla principal)
 * Es el archivo que define la pantalla de inicio o menú principal.
 * Sirve como punto central desde el que el usuario accede a todas las secciones del gestor.
 */

/**
 * HomeScreen
 * ----------
 * ✔ TIPO: función @Composable
 * Es la pantalla principal de la aplicación con el título y las tarjetas del menú.
 * Sirve para navegar a las secciones de ClientesGestor (Clientes, Cuotas, Pagos, Economía, Configuración).
 */
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
                descripcion = "Gestión de clientes ",
                Icons.Default.Person,
                onClick = {
                    navController.navigate(Routes.CLIENTES)
                }
            )

            MenuCard(
                titulo = "Cuotas",
                descripcion = "Gestiona las cuotas",
                Icons.Default.CardMembership,
                onClick = {
                }
            )

            MenuCard(
                titulo = "Pagos",
                descripcion = "Valida los pagos",
                Icons.Default.AttachMoney,
                onClick = {
                }
            )

            MenuCard(
                titulo = "Economia",
                descripcion = "Datos economicos",
                Icons.Default.AccountBalance,
                onClick = {
                }
            )

            MenuCard(
                titulo = "Configuración",
                descripcion = "Ajustes de la aplicación",
                Icons.Default.Settings,
                onClick = {
                }
            )
        }
    }
}
