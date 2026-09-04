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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.roberto.gestorpro.ui.components.AppPrimaryButton
import com.roberto.gestorpro.ui.components.AppTextLinkButton
import com.roberto.gestorpro.ui.viewmodel.MainViewModel
import java.io.File
import kotlinx.coroutines.launch

/**
 * Mensajes de credenciales inválidas que ya devuelve el repositorio de
 * autenticación. No se revela si el email está registrado: ambos se
 * presentan con el mismo mensaje genérico en la pantalla de login.
 */
private val erroresCredencialesLogin = setOf(
    "Email o contraseña incorrectos",
    "No existe una cuenta con este email"
)

private const val MENSAJE_CREDENCIALES_INCORRECTAS =
    "El correo o la contraseña no son correctos."

/**
 * textoErrorLogin
 * ---------------
 * Normaliza el error de credenciales devuelto por la autenticación a un
 * mensaje genérico. El resto de errores (cuenta desactivada, sin conexión,
 * perfil ilegible…) se muestran tal cual.
 */
private fun textoErrorLogin(error: String): String =
    if (error in erroresCredencialesLogin) {
        MENSAJE_CREDENCIALES_INCORRECTAS
    } else {
        error
    }

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
    var contrasenaVisible by rememberSaveable { mutableStateOf(false) }

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
                model = modeloLogo(logoNegocio),
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
                    onValueChange = {
                        email = it
                        mensajeError = ""
                    },
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
                    onValueChange = {
                        password = it
                        mensajeError = ""
                    },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = azulPrincipal
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { contrasenaVisible = !contrasenaVisible }
                        ) {
                            Icon(
                                imageVector = if (contrasenaVisible) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = if (contrasenaVisible) {
                                    "Ocultar contraseña"
                                } else {
                                    "Mostrar contraseña"
                                },
                                tint = azulPrincipal
                            )
                        }
                    },
                    visualTransformation = if (contrasenaVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
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

                Spacer(modifier = Modifier.height(24.dp))

                AppPrimaryButton(
                    text = "Entrar",
                    onClick = {
                        mensajeError = ""
                        scope.launch {
                            val error = mainViewModel.iniciarSesion(email.trim(), password)
                            if (error == null) {
                                val destino = mainViewModel.destinoSegunTipo()
                                navController.navigate(destino) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            } else {
                                mensajeError = textoErrorLogin(error)
                            }
                        }
                    },
                    enabled = formularioValido && !autenticando
                )

                if (autenticando) {
                    Spacer(modifier = Modifier.height(12.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = azulPrincipal
                    )
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

                AppTextLinkButton(
                    text = "¿No tienes cuenta? Crear una",
                    onClick = {
                        if (!autenticando) navController.navigate(Routes.REGISTRO)
                    }
                )

                AppTextLinkButton(
                    text = "¿Has olvidado tu contraseña?",
                    onClick = {
                        if (!autenticando) navController.navigate(Routes.RECUPERAR_PASSWORD)
                    }
                )
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

/**
 * modeloLogo
 * ----------
 * Modelo de Coil para el logo: URL remota (futuro Firebase Storage) → String;
 * ruta local → File. Nunca File(...) sobre una URL.
 */
private fun modeloLogo(valor: String): Any {
    return if (valor.startsWith("http://") || valor.startsWith("https://")) {
        valor
    } else {
        File(valor)
    }
}
