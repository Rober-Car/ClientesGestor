package com.roberto.gestorpro.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.roberto.gestorpro.navigation.Routes
import com.roberto.gestorpro.ui.viewmodel.MainViewModel
import java.io.File
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {

    /**
     * nombreNegocio / logoNegocio
     * ---------------------------
     * ✔ TIPO: variables observables (val by collectAsStateWithLifecycle) → String
     * Son el nombre y la ruta del logo del negocio configurados por el administrador.
     * Sirven para personalizar la pantalla de acceso; si están vacíos se muestra
     * el icono de persona y el nombre "GestorPro" de siempre.
     */
    val nombreNegocio by mainViewModel.nombreNegocio.collectAsStateWithLifecycle()
    val logoNegocio by mainViewModel.logoNegocio.collectAsStateWithLifecycle()

    /**
     * autenticando
     * ------------
     * ✔ TIPO: variable observable (val by collectAsStateWithLifecycle) → Boolean
     * Es el estado que indica si el inicio de sesión está en curso.
     * Sirve para desactivar el botón y mostrar carga mientras Firebase responde.
     */
    val autenticando by mainViewModel.autenticando.collectAsStateWithLifecycle()

    /**
     * mensajeError
     * ------------
     * ✔ TIPO: variable de estado (var by rememberSaveable) → String
     * Guarda el error devuelto por la autenticación real (credenciales,
     * cuenta desactivada, sin conexión...).
     * Sirve para mostrarlo bajo el formulario sin romper la pantalla.
     */
    var mensajeError by rememberSaveable { mutableStateOf("") }

    /**
     * scope
     * -----
     * ✔ TIPO: variable inmutable (val) → CoroutineScope
     * Es el ámbito de corrutinas ligado a la composición de esta pantalla.
     * Sirve para leer el tipo de usuario guardado antes de navegar al Home correcto.
     */
    val scope = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val formularioValido = email.isNotBlank() && password.isNotBlank()
    val azulPrincipal = Color(0xFF1E88E5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        /**
         * Logo o icono de la cabecera
         * ---------------------------
         * ✔ TIPO: bloque condicional con Composables
         * Es la imagen superior de la pantalla de acceso.
         * Sirve para mostrar el logo del negocio si está configurado;
         * si no, mantiene el icono de persona clásico de GestorPro.
         */
        if (logoNegocio.isNotBlank()) {

            /**
             * logoDelNegocio
             * --------------
             * ✔ TIPO: Composable (coil3.compose.AsyncImage)
             * Es la imagen del logo cargada desde memoria interna con Coil.
             * Sirve para identificar visualmente el negocio al iniciar sesión.
             */
            AsyncImage(
                model = File(logoNegocio),
                contentDescription = "Logo del negocio",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = azulPrincipal,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = nombreNegocio.ifBlank { "GestorPro" },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Gestión de clientes y cuotas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Iniciar sesión",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = azulPrincipal
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = azulPrincipal
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = azulPrincipal,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = azulPrincipal,
                        cursorColor = azulPrincipal
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = azulPrincipal
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = azulPrincipal,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = azulPrincipal,
                        cursorColor = azulPrincipal
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {

                        /**
                         * Inicio de sesión real con Firebase
                         * ----------------------------------
                         * ✔ TIPO: bloque de corrutina (scope.launch)
                         * Llama al MainViewModel para autenticar contra Firebase
                         * Authentication y comprobar el perfil usuarios/{uid}.
                         * Si hay error se muestra en pantalla; si no, navega al
                         * Home correcto según el tipo de usuario guardado.
                         */
                        scope.launch {
                            val error = mainViewModel.iniciarSesion(email.trim(), password)
                            if (error == null) {
                                mensajeError = ""
                                val destino = mainViewModel.destinoSegunTipo()
                                navController.navigate(destino) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            } else {
                                mensajeError = error
                            }
                        }
                    },
                    enabled = formularioValido && !autenticando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = azulPrincipal,
                        contentColor = Color.White,
                        disabledContainerColor = azulPrincipal.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    if (autenticando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Entrar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (mensajeError.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = mensajeError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { navController.navigate(Routes.REGISTRO) },
                    enabled = !autenticando
                ) {
                    Text(
                        text = "¿No tienes cuenta? Crear una",
                        color = azulPrincipal,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "© 2026 GestorPro",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}
