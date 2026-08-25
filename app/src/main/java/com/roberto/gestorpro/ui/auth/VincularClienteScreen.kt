package com.roberto.gestorpro.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.roberto.gestorpro.navigation.EnlacePendiente
import com.roberto.gestorpro.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * VincularClienteScreen.kt
 * ------------------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de vinculación del cliente)
 * Es el archivo que define la pantalla donde el CLIENTE se vincula a un negocio.
 * Sirve para las dos vías acordadas: código maestro (crea su propia ficha) o
 * enlace individual del administrador (reclama una ficha ya creada).
 */

/**
 * VincularClienteScreen
 * ---------------------
 * ✔ TIPO: función @Composable
 * Es la pantalla de vinculación con un campo de código y dos acciones.
 * Sirve para que el cliente se vincule una única vez; si ya lo está, muestra
 * el estado y deshabilita los botones.
 */
@Composable
fun VincularClienteScreen(
    /**
     * navController
     * -------------
     * ✔ TIPO: parámetro (param) → NavHostController
     * Es el controlador de navegación que recibe la pantalla.
     * Sirve para volver atrás hacia la Home del cliente.
     */
    navController: NavHostController,
    /**
     * codigoPrecargado
     * ----------------
     * ✔ TIPO: parámetro (param) → String? (nullable)
     * Es el token llegado por deep link (Vía B) ya extraído del enlace.
     * Sirve para abrir la pantalla en modo reclamación directa con el campo
     * precargado y un solo botón; null cuando el cliente entra a mano.
     */
    codigoPrecargado: String? = null,
    /**
     * mainViewModel
     * -------------
     * ✔ TIPO: parámetro (param) → MainViewModel (inyectado por Hilt)
     * Es el ViewModel principal de la app.
     * Sirve para lanzar las dos vías de vinculación y conocer su estado.
     */
    mainViewModel: MainViewModel = hiltViewModel()
) {

    /**
     * codigo / mensajeError / yaVinculado
     * -----------------------------------
     * ✔ TIPO: variables con estado (var) → String / Boolean
     * Son el campo de código, el error bajo los botones y la marca de que la
     * cuenta ya está vinculada. Sirven para validar y bloquear re-vinculaciones.
     */
    var codigo by rememberSaveable {
        mutableStateOf(codigoPrecargado ?: "")
    }
    var mensajeError by rememberSaveable { mutableStateOf("") }
    var yaVinculado by rememberSaveable { mutableStateOf(false) }

    /**
     * operandoRemoto
     * --------------
     * ✔ TIPO: variable observable (val by collectAsStateWithLifecycle) → Boolean
     * Es el estado de carga compartido de las operaciones remotas.
     * Sirve para deshabilitar los botones durante la vinculación.
     */
    val operandoRemoto by mainViewModel.operandoRemoto.collectAsStateWithLifecycle()

    /**
     * alcance
     * -------
     * ✔ TIPO: variable (val) → CoroutineScope
     * Es el scope ligado a la composición para lanzar la vinculación.
     * Sirve para no bloquear el hilo principal durante la operación.
     */
    val alcance = rememberCoroutineScope()

    /**
     * LaunchedEffect(comprobación inicial)
     * ------------------------------------
     * ✔ TIPO: efecto de composición (LaunchedEffect)
     * Comprueba al abrir la pantalla si el usuario ya está vinculado.
     * Sirve para mostrar el estado correcto desde el primer frame.
     */
    LaunchedEffect(Unit) {
        yaVinculado = mainViewModel.clienteYaVinculado()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    text = "Vincularme a un negocio",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (yaVinculado) {
                Text(
                    text = "Tu cuenta ya está vinculada a un negocio. " +
                        "No puedes volver a vincularte.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                if (codigoPrecargado == null) {
                    Text(
                        text = "Introduce el código maestro del negocio o el enlace " +
                            "individual que te haya enviado tu administrador.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it },
                        label = { Text("Código") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Has abierto un enlace de vinculación. Reclama tu ficha " +
                            "para unirte al negocio de tu administrador.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (mensajeError.isNotBlank()) {
                    Text(
                        text = mensajeError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (codigoPrecargado == null) {
                    Button(
                        onClick = {
                            alcance.launch {
                                val error =
                                    mainViewModel.vincularConCodigoMaestro(codigo)
                                if (error == null) {
                                    navController.popBackStack()
                                } else {
                                    mensajeError = error
                                }
                            }
                        },
                        enabled = !operandoRemoto && codigo.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Vincularme con código maestro")
                    }
                }

                OutlinedButton(
                    onClick = {
                        alcance.launch {
                            val error = mainViewModel.reclamarFichaConEnlace(codigo)
                            if (error == null) {
                                EnlacePendiente.limpiar()
                                navController.popBackStack()
                            } else {
                                mensajeError = error
                            }
                        }
                    },
                    enabled = !operandoRemoto && codigo.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reclamar mi ficha con enlace")
                }

                if (operandoRemoto) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
