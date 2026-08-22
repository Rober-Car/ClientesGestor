package com.roberto.gestorpro.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.roberto.gestorpro.model.TipoUsuario
import com.roberto.gestorpro.navigation.Routes
import com.roberto.gestorpro.ui.components.MenuCard
import com.roberto.gestorpro.ui.viewmodel.MainViewModel

/**
 * SeleccionTipoUsuarioScreen.kt
 * -----------------------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de selección de perfil)
 * Es el archivo que define la pantalla "¿Cómo vas a utilizar GestorPro?".
 * Sirve para que el usuario elija su perfil la primera vez que abre la app;
 * la elección se guarda en DataStore y no se vuelve a preguntar.
 */

/**
 * SeleccionTipoUsuarioScreen
 * --------------------------
 * ✔ TIPO: función @Composable
 * Es la pantalla inicial de elección entre Administrador y Cliente.
 * Sirve para guardar el tipo elegido con MainViewModel y navegar al Login,
 * borrando esta pantalla del historial para que atrás no vuelva aquí.
 */
@Composable
fun SeleccionTipoUsuarioScreen(
    /**
     * navController
     * -------------
     * ✔ TIPO: parámetro (param) → NavHostController
     * Es el controlador de navegación que recibe la pantalla.
     * Sirve para navegar al Login una vez guardado el tipo de usuario.
     */
    navController: NavHostController,
    /**
     * mainViewModel
     * -------------
     * ✔ TIPO: parámetro (param) → MainViewModel (inyectado por Hilt)
     * Es el ViewModel de preferencias de la app.
     * Sirve para persistir la elección del perfil en DataStore.
     */
    mainViewModel: MainViewModel = hiltViewModel()
) {

    /**
     * Scaffold
     * --------
     * ✔ TIPO: función @Composable (androidx.compose.material3.Scaffold)
     * Es el contenedor base de la pantalla de selección.
     * Sirve como estructura general y respeta las barras del sistema.
     */
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->

        /**
         * Column principal
         * ----------------
         * ✔ TIPO: función @Composable (Column)
         * Es el contenedor vertical que centra título y tarjetas.
         * Sirve para presentar las dos opciones una debajo de la otra.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            /**
             * Text del título
             * ---------------
             * ✔ TIPO: función @Composable (Text)
             * Es la pregunta que encabeza la pantalla.
             * Sirve para indicar al usuario qué debe elegir antes de entrar.
             */
            Text(
                text = "¿Cómo vas a utilizar GestorPro?",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            /**
             * Text del subtítulo
             * ------------------
             * ✔ TIPO: función @Composable (Text)
             * Es el texto explicativo bajo el título.
             * Sirve para aclarar que la elección se puede cambiar luego desde Configuración > Cuenta.
             */
            Text(
                text = "Podrás cambiarlo más tarde en Cuenta",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            /**
             * MenuCard de Administrador
             * -------------------------
             * ✔ TIPO: componente @Composable (MenuCard)
             * Es la tarjeta del perfil Administrador/Negocio.
             * Sirve para guardar ADMINISTRADOR y continuar al Login mostrando el menú completo.
             */
            MenuCard(
                titulo = "Administrador",
                descripcion = "Gestionar mi negocio",
                icono = Icons.Default.ManageAccounts,
                iconColor = Color(0xFF1E88E5),
                onClick = {
                    mainViewModel.guardarTipoUsuario(TipoUsuario.ADMINISTRADOR)
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SELECCION_TIPO_USUARIO) { inclusive = true }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            /**
             * MenuCard de Cliente
             * -------------------
             * ✔ TIPO: componente @Composable (MenuCard)
             * Es la tarjeta del perfil Cliente.
             * Sirve para guardar CLIENTE y continuar al Login mostrando el menú de cliente.
             */
            MenuCard(
                titulo = "Cliente",
                descripcion = "Soy cliente del negocio",
                icono = Icons.Default.Person,
                iconColor = Color(0xFF1E88E5),
                onClick = {
                    mainViewModel.guardarTipoUsuario(TipoUsuario.CLIENTE)
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SELECCION_TIPO_USUARIO) { inclusive = true }
                    }
                }
            )
        }
    }
}
