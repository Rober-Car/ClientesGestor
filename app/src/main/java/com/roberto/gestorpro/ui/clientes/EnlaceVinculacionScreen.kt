package com.roberto.gestorpro.ui.clientes

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.roberto.gestorpro.data.firebase.ClienteRemotoRepository
import com.roberto.gestorpro.data.firebase.ConsultaEnlace
import com.roberto.gestorpro.data.firebase.VinculacionRepository
import com.roberto.gestorpro.data.repository.ClienteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * EnlaceVinculacionViewModel
 * --------------------------
 * ✔ TIPO: clase @HiltViewModel (ViewModel)
 * Es el ViewModel de la pantalla de enlace de vinculación de un cliente.
 * Sirve para comprobar la réplica remota de la ficha y generar, regenerar o
 * revocar su token individual bloqueando todo mientras no esté sincronizada.
 */
@HiltViewModel
class EnlaceVinculacionViewModel @Inject constructor(
    private val clienteRepository: ClienteRepository,
    private val clienteRemotoRepository: ClienteRemotoRepository,
    private val vinculacionRepository: VinculacionRepository
) : ViewModel() {

    /**
     * Estados observables de la pantalla.
     * Sirven para pintar el estado de sincronización, el enlace activo,
     * los mensajes y la carga sin bloquear el hilo principal.
     */
    private val _fichaSincronizada = MutableStateFlow<Boolean?>(null)
    val fichaSincronizada = _fichaSincronizada.asStateFlow()

    private val _enlace = MutableStateFlow(ConsultaEnlace(null, false))
    val enlace = _enlace.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje = _mensaje.asStateFlow()

    private val _exito = MutableStateFlow<String?>(null)
    val exito = _exito.asStateFlow()

    private val _operando = MutableStateFlow(false)
    val operando = _operando.asStateFlow()

    /**
     * cargar
     * ------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Comprueba si la ficha existe en Firestore y cuál es su enlace activo.
     * Sirve para decidir entre permitir generar el enlace o pedir sincronizar.
     */
    suspend fun cargar(idCliente: Int) {
        _operando.value = true
        try {
            val existe = clienteRepository.obtenerClientePorIdRepo(idCliente) != null &&
                clienteRemotoRepository.existeClienteRemoto(idCliente)
            _fichaSincronizada.value = existe
            if (existe) {
                _enlace.value = vinculacionRepository.consultarEnlace(idCliente)
            }
        } finally {
            _operando.value = false
        }
    }

    /**
     * reintentarComprobacion
     * ----------------------
     * ✔ TIPO: método (fun) de Kotlin (lanza corrutina)
     * Vuelve a comprobar la sincronización tras un reintento del usuario.
     */
    fun reintentarComprobacion(idCliente: Int) {
        viewModelScope.launch { cargar(idCliente) }
    }

    /**
     * generar / regenerar / revocar
     * -----------------------------
     * ✔ TIPO: métodos (fun) de Kotlin (lanzan corrutinas)
     * Ejecutan las operaciones de enlace sobre la ficha remota ya
     * sincronizada y refrescan el estado al terminar.
     */
    fun generar(idCliente: Int) {
        viewModelScope.launch {
            _operando.value = true
            try {
                val resultado = vinculacionRepository.generarEnlaceParaCliente(idCliente)
                if (resultado.exito) {
                    _exito.value = "Enlace generado. Ya puedes compartirlo."
                    _mensaje.value = null
                } else {
                    _mensaje.value = resultado.mensaje
                    _exito.value = null
                }
                _enlace.value = vinculacionRepository.consultarEnlace(idCliente)
            } finally {
                _operando.value = false
            }
        }
    }

    fun regenerar(idCliente: Int) {
        val tokenAnterior = _enlace.value.token ?: return
        viewModelScope.launch {
            _operando.value = true
            try {
                val resultado =
                    vinculacionRepository.regenerarEnlaceDeCliente(idCliente, tokenAnterior)
                if (resultado.exito) {
                    _exito.value = "Enlace regenerado. El anterior ya no sirve."
                    _mensaje.value = null
                } else {
                    _mensaje.value = resultado.mensaje
                    _exito.value = null
                }
                _enlace.value = vinculacionRepository.consultarEnlace(idCliente)
            } finally {
                _operando.value = false
            }
        }
    }

    fun revocar(idCliente: Int) {
        val token = _enlace.value.token ?: return
        viewModelScope.launch {
            _operando.value = true
            try {
                val resultado = vinculacionRepository.revocarEnlaceDeCliente(idCliente, token)
                if (resultado.exito) {
                    _exito.value = "Enlace revocado."
                    _mensaje.value = null
                } else {
                    _mensaje.value = resultado.mensaje
                    _exito.value = null
                }
                _enlace.value = vinculacionRepository.consultarEnlace(idCliente)
            } finally {
                _operando.value = false
            }
        }
    }

    fun limpiarMensajes() {
        _mensaje.value = null
        _exito.value = null
    }
}

/**
 * EnlaceVinculacionScreen
 * -----------------------
 * ✔ TIPO: función @Composable
 * Es la pantalla donde el ADMIN gestiona el enlace individual de un cliente:
 * comprueba que la ficha está en la nube y genera, copia, comparte,
 * regenera o revoca su token de vinculación.
 */
@Composable
fun EnlaceVinculacionScreen(
    /**
     * navController
     * -------------
     * ✔ TIPO: parámetro (param) → NavHostController
     * Es el controlador de navegación que recibe la pantalla.
     * Sirve para volver atrás hacia el perfil del cliente.
     */
    navController: NavHostController,
    /**
     * idCliente
     * ---------
     * ✔ TIPO: parámetro (param) → Int
     * Es el identificador Room/Firestore compartido del cliente.
     * Sirve para operar sobre la misma ficha en ambas bases.
     */
    idCliente: Int,
    /**
     * viewModel
     * ---------
     * ✔ TIPO: parámetro (param) → EnlaceVinculacionViewModel (inyectado por Hilt)
     * Es el ViewModel de esta pantalla.
     * Sirve para ejecutar las operaciones remotas y observar sus estados.
     */
    viewModel: EnlaceVinculacionViewModel = hiltViewModel()
) {

    val fichaSincronizada by viewModel.fichaSincronizada.collectAsStateWithLifecycle()
    val enlace by viewModel.enlace.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val exito by viewModel.exito.collectAsStateWithLifecycle()
    val operando by viewModel.operando.collectAsStateWithLifecycle()

    var tokenVisible by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(idCliente) {
        viewModel.cargar(idCliente)
    }

    val context = LocalContext.current

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
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
                    text = "Vinculación en la nube",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            when (fichaSincronizada) {
                null -> {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }

                false -> {
                    Text(
                        text = "Esta ficha todavía no está sincronizada con la nube. " +
                            "Guarda de nuevo el cliente (o reintenta la sincronización " +
                            "desde su formulario) antes de generar un enlace.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    OutlinedButton(
                        onClick = { viewModel.reintentarComprobacion(idCliente) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reintentar comprobación")
                    }
                }

                else -> {
                    Text(
                        text = if (enlace.token == null) {
                            "Este cliente aún no tiene enlace de vinculación. " +
                                "Genéralo y compártelo con él."
                        } else if (enlace.caducado) {
                            "El enlace actual ha caducado. Regenéralo para poder compartirlo."
                        } else {
                            "Enlace activo listo para compartir."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (enlace.token != null && !enlace.caducado) {
                        OutlinedTextField(
                            value = tokenVisible.ifBlank { enlace.token ?: "" },
                            onValueChange = { tokenVisible = it },
                            label = { Text("Token de vinculación") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                val portapapeles = context.getSystemService(
                                    Context.CLIPBOARD_SERVICE
                                ) as ClipboardManager
                                portapapeles.setPrimaryClip(
                                    ClipData.newPlainText(
                                        "Enlace GestorPro",
                                        enlaceGestorPro(enlace.token)
                                    )
                                )
                            }) {
                                Text("Copiar")
                            }

                            OutlinedButton(onClick = {
                                val envio = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Regístrate en GestorPro y vincula tu ficha con este enlace: " +
                                            enlaceGestorPro(enlace.token)
                                    )
                                }
                                context.startActivity(
                                    Intent.createChooser(envio, "Compartir enlace")
                                )
                            }) {
                                Text("Compartir")
                            }
                        }

                        Text(
                            text = "El enlace caduca a los 7 días y solo puede usarse una vez.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (mensaje != null) {
                        Text(
                            text = mensaje!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (exito != null) {
                        Text(
                            text = exito!!,
                            color = Color(0xFF4CAF50),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.limpiarMensajes()
                            if (enlace.token == null || enlace.caducado) {
                                viewModel.generar(idCliente)
                            } else {
                                viewModel.regenerar(idCliente)
                            }
                        },
                        enabled = !operando,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5),
                            contentColor = Color.White
                        )
                    ) {
                        if (operando) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                if (enlace.token == null || enlace.caducado) {
                                    "Generar enlace"
                                } else {
                                    "Regenerar enlace"
                                }
                            )
                        }
                    }

                    if (enlace.token != null && !enlace.caducado) {
                        OutlinedButton(
                            onClick = { viewModel.revocar(idCliente) },
                            enabled = !operando,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Revocar enlace")
                        }
                    }
                }
            }
        }
    }
}

/**
 * enlaceGestorPro
 * ---------------
 * ✔ TIPO: función (fun) privada de Kotlin → String
 * Construye el deep link completo a partir del token. Centraliza el formato
 * para poder evolucionar después a App Links HTTPS sin tocar las pantallas.
 */
private fun enlaceGestorPro(token: String?): String {
    return "gestorpro://vincular/$token"
}
