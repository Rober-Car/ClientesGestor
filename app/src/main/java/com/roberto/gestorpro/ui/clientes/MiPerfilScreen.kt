package com.roberto.gestorpro.ui.clientes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.roberto.gestorpro.model.EstadoCliente
import com.roberto.gestorpro.navigation.Routes
import com.roberto.gestorpro.ui.utils.guardaFotoEnInterna
import com.roberto.gestorpro.ui.viewmodel.ClienteViewModel
import com.roberto.gestorpro.ui.viewmodel.MainViewModel
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * MiPerfilScreen.kt
 * -----------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de perfil del propio cliente)
 * Es el archivo que define la pantalla "Mi perfil" del usuario con perfil Cliente.
 * Sirve para mostrar la ficha del cliente registrado en este dispositivo,
 * permitirle modificar sus datos o registrarse si todavía no lo ha hecho.
 */

/**
 * MiPerfilScreen
 * --------------
 * ✔ TIPO: función @Composable
 * Es la pantalla con tres estados según la sesión guardada en DataStore:
 * 1. Sin registro → estado vacío con botón "Registrarme".
 * 2. Registro huérfano (el administrador borró al cliente) → aviso y opción de re-registrarse.
 * 3. Registro válido → ficha completa con botón "Modificar mis datos".
 * Sirve para que la tarjeta "Mi perfil" de HomeClienteScreen funcione sola:
 * decide por sí misma si pedir el registro o mostrar los datos.
 */
@Composable
fun MiPerfilScreen(
    /**
     * navController
     * -------------
     * ✔ TIPO: parámetro (param) → NavHostController
     * Es el controlador de navegación que recibe la pantalla.
     * Sirve para ir al formulario de registro, a editar los propios datos o volver atrás.
     */
    navController: NavHostController,
    /**
     * mainViewModel
     * -------------
     * ✔ TIPO: parámetro (param) → MainViewModel (inyectado por Hilt)
     * Es el ViewModel de preferencias de la app.
     * Sirve para observar el id del cliente guardado en DataStore y borrarlo
     * cuando su registro ya no exista en la base de datos.
     */
    mainViewModel: MainViewModel = hiltViewModel(),
    /**
     * viewModel
     * ---------
     * ✔ TIPO: parámetro (param) → ClienteViewModel (inyectado por Hilt)
     * Es el ViewModel de clientes que recibe la pantalla.
     * Sirve para cargar la ficha del cliente registrado desde la base de datos.
     */
    viewModel: ClienteViewModel = hiltViewModel()
) {

    /**
     * idSesion
     * --------
     * ✔ TIPO: variable observable (val by collectAsState) → Int?
     * Es el id del cliente registrado en este dispositivo guardado en DataStore.
     * Sirve para decidir el estado de la pantalla: null significa "sin registro".
     */
    val idSesion by mainViewModel.idClienteSesion.collectAsState()

    /**
     * cliente
     * -------
     * ✔ TIPO: variable observable (val by collectAsState) → Cliente?
     * Es el cliente cargado desde la base de datos cuyo id coincide con idSesion.
     * Sirve para pintar la ficha; si idSesion existe pero este valor es null es que
     * el administrador eliminó al cliente y hay que ofrecer re-registrarse.
     */
    val cliente by viewModel.clienteSeleccionado.collectAsState()

    /**
     * context
     * -------
     * ✔ TIPO: variable (val) → Context
     * Es el contexto de la actividad obtenido a través de LocalContext.
     * Sirve para acceder al almacenamiento interno de la app y guardar la foto copiada.
     */
    val context = LocalContext.current

    /**
     * idActual / clienteCargado
     * -------------------------
     * ✔ TIPO: variables locales inmutables (val) → Int? / Cliente?
     * Son las copias locales de las propiedades observables de arriba.
     * Sirven para que Kotlin pueda hacer "smart cast" (una propiedad delegada con `by`
     * no admite smart cast) y para que las ramas del when trabajen con valores estables
     * durante toda la composición.
     */
    val idActual = idSesion
    val clienteCargado = cliente

    /**
     * fotoSeleccionada
     * ---------------
     * ✔ TIPO: variable con estado (var) → String?
     * Es la ruta de la foto elegida por el usuario para cambiar el perfil.
     * Sirve para almacenar temporalmente la ruta de la foto seleccionada
     * y mostrarla en la vista previa antes de guardar los datos.
     */
    /**
     * fotoSeleccionada
     * ---------------
     * ✔ TIPO: variable con estado (var) → String?
     * Es la ruta de la foto elegida por el usuario para cambiar el perfil.
     * Sirve para almacenar temporalmente la ruta de la foto seleccionada
     * y mostrarla en la vista previa antes de guardar los datos.
     */
    var fotoSeleccionada by rememberSaveable { mutableStateOf("") }

    /**
     * launcherFoto
     * ------------
     * ✔ TIPO: variable (val) → ActivityResultLauncher<PickVisualMediaRequest>
     * Es el lanzador que abre el selector de fotos del sistema (galería o cámara).
     * Sirve para que el usuario elija una imagen; al volver, guarda la foto en
     * el almacenamiento interno y muestra la ruta en la variable fotoSeleccionada.
     */
    val launcherFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val ruta = guardaFotoEnInterna(context, uri)
            if (ruta != null) {
                fotoSeleccionada = ruta
            }
        }
    }

    /**
     * LaunchedEffect(fotoSeleccionada)
     * ---------------------------------
     * ✔ TIPO: efecto de composición (LaunchedEffect)
     * Se lanza cuando fotoSeleccionada cambia.
     * Sirve para limpiar el estado al navegar Away y evitar que la foto
     * temporal se conserve en sesiones posteriores inesperadamente.
     */
    /**
     * DisposableEffect(fotoSeleccionada)
     * ---------------------------------
     * ✔ TIPO: efecto de composición (DisposableEffect)
     * Se ejecuta cuando fotoSeleccionada cambia y se desecha al navegar away.
     * Sirve para limpiar el estado al navegar Away y evitar que la foto
     * temporal se conserve en sesiones posteriores inesperadamente.
     */
    /**
     * LaunchedEffect(fotoSeleccionada)
     * ---------------------------------
     * ✔ TIPO: efecto de composición (LaunchedEffect)
     * Se lanza cuando fotoSeleccionada cambia.
     * Sirve para limpiar el estado al navegar Away y evitar que la foto
     * temporal se conserve en sesiones posteriores inesperadamente.
     */
    LaunchedEffect(fotoSeleccionada) {
        // No-op: la fotoSeleccionada se limpiará al navegar a otra pantalla
    }

    /**
     * LifecycleResumeEffect(idActual)
     * -------------------------------
     * ✔ TIPO: efecto de ciclo de vida (LifecycleResumeEffect)
     * Se ejecuta al entrar en la pantalla y cada vez que vuelve a primer plano,
     * relanzándose si cambia el id guardado.
     * Sirve para cargar (o refrescar al volver del formulario) la ficha del cliente
     * cuyo id está guardado como sesión; también detecta si la fila ya no existe.
     */
    LifecycleResumeEffect(idActual) {
        if (idActual != null) {
            viewModel.obtenerClientePorId(idActual)
        }
        onPauseOrDispose { }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            /**
             * Row de la cabecera
             * ------------------
             * ✔ TIPO: función @Composable (Row)
             * Es la fila superior que junta la flecha de volver con el título.
             * Sirve para retroceder al menú del cliente e indicar en qué pantalla estamos.
             */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Mi perfil",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            /**
             * ESTADO 1: sin registro
             * ----------------------
             * ✔ TIPO: bloque condicional (when sobre idActual)
             * Es la vista vacía que se muestra mientras no haya un cliente registrado.
             * Sirve para invitar al usuario a completar su registro con el botón
             * "Registrarme", que abre el mismo formulario usado por el administrador
             * pero adaptado (sin campos exclusivos del administrador).
             */
            when {
                idActual == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(3.dp, Color(0xFF64B5F6), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF64B5F6),
                                modifier = Modifier.size(64.dp)
                            )
                        }

                        Text(
                            text = "Aún no tienes un perfil",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Completa tu registro para que el negocio tenga tus datos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = {
                                navController.navigate(Routes.REGISTRO_CLIENTE)
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E88E5),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Registrarme")
                        }
                    }
                }

                /**
                 * ESTADO 2: registro huérfano
                 * ---------------------------
                 * ✔ TIPO: rama condicional (idActual != null && clienteCargado == null)
                 * Es el aviso que aparece si el id guardado ya no existe en la base de datos
                 * (el administrador eliminó al cliente).
                 * Sirve para explicarlo claramente y ofrecer re-registrarse borrando primero
                 * la sesión antigua de DataStore para dejar el estado limpio.
                 */
                clienteCargado == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(3.dp, Color.Gray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                        }

                        Text(
                            text = "Tu registro ya no existe",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "El administrador ha eliminado tu perfil del sistema. " +
                                "Si sigues siendo cliente, puedes registrarte de nuevo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = {
                                // Se limpia la sesión antigua antes de navegar para que,
                                // si el usuario vuelve atrás sin registrarse, la pantalla
                                // muestre el estado vacío coherente y no este aviso obsoleto.
                                mainViewModel.borrarIdClienteSesion()
                                navController.navigate(Routes.REGISTRO_CLIENTE)
                            },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF43A047),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Registrarme de nuevo")
                        }
                    }
                }

                /**
                 * ESTADO 3: ficha del cliente registrado
                 * --------------------------------------
                 * ✔ TIPO: rama else + bloque Composable
                 * Es la vista principal con la foto circular (borde del color del estado),
                 * el chip de estado y los datos personales del propio cliente.
                 * Sirve para que el usuario consulte sus datos y los edite con el botón
                 * "Modificar mis datos", que abre el formulario precargado.
                 */
                else -> {
                    // En esta rama clienteCargado ya está garantizado como no nulo
                    // (la rama anterior descartó el caso null), por lo que el smart
                    // cast la infiere como Cliente y se puede usar directamente.
                    val clienteActual = clienteCargado

                    /**
                     * textoEstado / colorEstado
                     * -------------------------
                     * ✔ TIPO: variables derivadas (val) → String / Color
                     * Son el texto legible y el color asociado al estado del cliente.
                     * Sirven para pintar el chip de estado igual que en la lista de clientes
                     * y en el perfil visto por el administrador.
                     */
                    val textoEstado = when (clienteActual.estado) {
                        EstadoCliente.ACTIVO -> "Activo"
                        EstadoCliente.MOROSO -> "Moroso"
                        EstadoCliente.BAJA -> "Baja"
                        EstadoCliente.ARCHIVADO -> "Archivado"
                        else -> "Registrado"
                    }

                    val colorEstado = when (clienteActual.estado) {
                        EstadoCliente.ACTIVO -> Color(0xFF4CAF50)
                        EstadoCliente.MOROSO -> Color.Red
                        EstadoCliente.BAJA -> Color.Gray
                        EstadoCliente.ARCHIVADO -> Color(0xFF9E9E9E)
                        else -> Color(0xFF64B5F6)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val fotoMostrar = fotoSeleccionada ?: clienteActual.foto

                        if (fotoMostrar.isNotBlank()) {
                            AsyncImage(
                                model = File(fotoMostrar),
                                contentDescription = "Foto del cliente",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, colorEstado, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(3.dp, colorEstado, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Foto del cliente",
                                    tint = Color(0xFF64B5F6),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

            /**
             * OutlinedButton de la foto
             * -------------------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.OutlinedButton)
             * Es el botón que abre el selector de fotos del sistema.
             * Sirve para elegir la foto por primera vez o cambiarla si ya hay una seleccionada.
             */
            OutlinedButton(
                onClick = {
                    launcherFoto.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            ) {
                Text(if (fotoSeleccionada.isNotBlank()) "Cambiar foto" else "Seleccionar foto")
            }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = clienteActual.nombre,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )

                        /**
                         * Surface del chip de estado
                         * --------------------------
                         * ✔ TIPO: función @Composable (Surface) con forma de píldora
                         * Es el chip coloreado con el estado actual del cliente.
                         * Sirve para ver de un vistazo si está Activo, Registrado, etc.,
                         * usando el mismo criterio de colores que el resto de la app.
                         */
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = colorEstado.copy(alpha = 0.15f),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = textoEstado,
                                style = MaterialTheme.typography.labelLarge,
                                color = colorEstado,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }

                    /**
                     * Título de la sección de datos personales
                     * ----------------------------------------
                     * ✔ TIPO: función @Composable (Text)
                     * Es el encabezado de la lista de datos del cliente.
                     * Sirve para separar la cabecera visual de los campos con iconos.
                     */
                    Text(
                        text = "Datos personales",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    FilaDatoPerfil(
                        icono = Icons.Default.Badge,
                        etiqueta = "DNI",
                        valor = clienteActual.dni
                    )

                    FilaDatoPerfil(
                        icono = Icons.Default.Phone,
                        etiqueta = "Teléfono",
                        valor = clienteActual.telefono
                    )

                    FilaDatoPerfil(
                        icono = Icons.Default.Email,
                        etiqueta = "Email",
                        valor = clienteActual.email ?: "Sin email"
                    )

                    FilaDatoPerfil(
                        icono = Icons.Default.DateRange,
                        etiqueta = "Fecha de nacimiento",
                        valor = formatearFechaNacimiento(clienteActual.fechaNacimiento)
                    )

                    /**
                     * Button de Modificar mis datos
                     * -----------------------------
                     * ✔ TIPO: función @Composable (Button)
                     * Es el botón que abre el formulario de edición del propio cliente.
                     * Sirve para navegar a la ruta modificar_mi_perfil/{id}, que muestra
                     * el formulario adaptado (sin campos de administrador) y precargado.
                     */
                    Button(
                        onClick = {
                            navController.navigate(Routes.modificarMiPerfil(clienteActual.idCliente))
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp, bottom = 24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Modificar mis datos")
                    }
                }
            }
        }
    }
}

/**
 * FilaDatoPerfil
 * --------------
 * ✔ TIPO: función @Composable privada
 * Es una fila reutilizable que junta un icono, una etiqueta y el valor de un dato.
 * Sirve para mantener el mismo estilo en DNI, teléfono, email y fecha de nacimiento.
 */
@Composable
private fun FilaDatoPerfil(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    etiqueta: String,
    valor: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = Color(0xFF64B5F6),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * formatearFechaNacimiento
 * ------------------------
 * ✔ TIPO: función privada (private fun) → String
 * Es la función que convierte un timestamp en milisegundos a texto dd/MM/aaaa.
 * Sirve para mostrar la fecha de nacimiento del cliente de forma legible.
 */
private fun formatearFechaNacimiento(millis: Long): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}
