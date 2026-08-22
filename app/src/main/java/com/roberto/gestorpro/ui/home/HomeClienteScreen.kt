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

/**
 * HomeClienteScreen.kt
 * --------------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de inicio del cliente)
 * Es el archivo que define el menú principal exclusivo del perfil Cliente.
 * Sirve para que los clientes tengan su propio inicio, distinto al del administrador.
 */

/**
 * HomeClienteScreen
 * -----------------
 * ✔ TIPO: función @Composable
 * Es la pantalla de inicio con las tarjetas disponibles para el cliente.
 * Sirve para dar acceso a "Mi perfil" (ficha propia y registro) y a las clases;
 * la pantalla Mi perfil decide por sí sola si pide el registro o muestra los datos.
 */
@Composable
fun HomeClienteScreen(
    /**
     * navController
     * -------------
     * ✔ TIPO: parámetro (param) → NavHostController
     * Es el controlador de navegación que recibe la pantalla.
     * Sirve para navegar a Mi perfil o a las clases desde las tarjetas del menú.
     */
    navController: NavHostController,
    /**
     * mainViewModel
     * -------------
     * ✔ TIPO: parámetro (param) → MainViewModel (inyectado por Hilt)
     * Es el ViewModel de preferencias de la app.
     * Sirve para leer el nombre y el logo del negocio configurados por el administrador.
     */
    mainViewModel: MainViewModel = hiltViewModel()
) {

    /**
     * nombreNegocio / logoNegocio
     * ---------------------------
     * ✔ TIPO: variables observables (val by collectAsStateWithLifecycle) → String
     * Son el nombre y la ruta del logo del negocio configurados por el administrador.
     * Sirven para personalizar la cabecera; si están vacíos se muestra "GestorPro"
     * con su icono por defecto, igual que en la Home del administrador.
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
             * Es la cabecera principal de la pantalla.
             * Sirve para mostrar el mismo branding que la Home del administrador,
             * de modo que el cliente reconozca el negocio al entrar.
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
                     * Sirve para identificar visualmente el negocio en la pantalla del cliente.
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

            /**
             * MenuCard de Mi perfil
             * ---------------------
             * ✔ TIPO: componente @Composable (MenuCard)
             * Es la tarjeta que abre la ficha del propio cliente.
             * Sirve para navegar a MIPERFIL; esa pantalla decide si mostrar el formulario
             * de registro (primera vez o si el admin borró al cliente) o los datos guardados.
             */
            MenuCard(
                titulo = "Mi perfil",
                descripcion = "Tus datos y tu registro",
                icono = Icons.Default.Person,
                iconColor = Color(0xFF1E88E5),
                onClick = { navController.navigate(Routes.MIPERFIL) }
            )

            /**
             * MenuCard de Clases
             * ------------------
             * ✔ TIPO: componente @Composable (MenuCard)
             * Es la tarjeta que abre la consulta de clases en modo cliente.
             * Sirve para que el cliente consulte sus sesiones (solo lectura);
             * navega a la ruta CLIENTE_CLASES, sin opciones de crear ni configurar.
             */
            MenuCard(
                titulo = "Clases",
                descripcion = "Consulta clases y horarios",
                icono = Icons.Default.Groups,
                iconColor = Color(0xFF1E88E5),
                onClick = { navController.navigate(Routes.CLIENTE_CLASES) }
            )

            /**
             * MenuCard de Configuración
             * -------------------------
             * ✔ TIPO: componente @Composable (MenuCard)
             * Es la tarjeta de ajustes del cliente.
             * Sirve para acceder a Preferencias, donde de momento puede cambiar
             * el tema de la app (claro/oscuro/sistema) y cerrar sesión.
             */
            MenuCard(
                titulo = "Configuración",
                descripcion = "Tema de la app y más",
                icono = Icons.Default.Settings,
                iconColor = Color(0xFF1E88E5),
                onClick = { navController.navigate(Routes.PREFERENCIAS) }
            )
        }
    }
}
