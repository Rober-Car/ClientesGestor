package com.roberto.clientesgestor.ui.clientes

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notes
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.roberto.clientesgestor.model.EstadoCliente
import com.roberto.clientesgestor.navigation.Routes
import com.roberto.clientesgestor.ui.components.ServicioItem
import com.roberto.clientesgestor.ui.viewmodel.ClienteViewModel
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * PerfilClienteAdministradorScreen.kt
 * ------------------------------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de perfil de cliente)
 * Es el archivo que define la pantalla de perfil de un cliente.
 * Sirve para mostrar los datos y la gestión detallada de cada cliente.
 */

/**
 * PerfilClienteScreen
 * -------------------
 * ✔ TIPO: función @Composable
 * Es la pantalla de perfil del cliente con su estructura base.
 * Sirve para mostrar la información del cliente y su gestión.
 */
@Composable
fun PerfilClienteScreen(
    /**
     * navController
     * -------------
     * ✔ TIPO: parámetro (param) → NavHostController
     * Es el controlador de navegación que recibe la pantalla de perfil.
     * Sirve para poder volver atrás hacia la lista de clientes.
     */
    navController: NavHostController,
    idCliente: Int,
    viewModel: ClienteViewModel = hiltViewModel()
) {

    /**
     * LifecycleResumeEffect(idCliente)
     * --------------------------------
     * ✔ TIPO: efecto de ciclo de vida (LifecycleResumeEffect)
     * Se ejecuta al entrar en la pantalla y cada vez que se vuelve a ella.
     * Sirve para cargar el cliente por su id y para refrescar los datos
     * cuando el usuario vuelve del formulario de modificar cliente.
     */
    LifecycleResumeEffect(idCliente) {
        viewModel.obtenerClientePorId(idCliente)
        onPauseOrDispose { }
    }

    val cliente by viewModel.clienteSeleccionado.collectAsState()

    /**
     * textoEstado
     * -----------
     * ✔ TIPO: variable inmutable (val) → String
     * Es el texto legible del estado del cliente en el perfil.
     * Sirve para traducir el enum a "Activo", "Moroso", "Baja" o "Registrado".
     */
    val textoEstado = when (cliente?.estado) {
        EstadoCliente.ACTIVO -> "Activo"
        EstadoCliente.MOROSO -> "Moroso"
        EstadoCliente.BAJA -> "Baja"
        else -> "Registrado"
    }

    /**
     * colorEstado
     * -----------
     * ✔ TIPO: variable inmutable (val) → Color
     * Es el color asociado al estado del cliente.
     * Sirve para pintar el chip del estado con el mismo color que la lista de clientes.
     */
    val colorEstado = when (cliente?.estado) {
        EstadoCliente.ACTIVO -> Color(0xFF4CAF50)
        EstadoCliente.MOROSO -> Color.Red
        EstadoCliente.BAJA -> Color.Gray
        else -> Color(0xFF64B5F6)
    }

    /**
     * Scaffold
     * --------
     * ✔ TIPO: función @Composable (androidx.compose.material3.Scaffold)
     * Es el contenedor base de la pantalla de perfil del cliente.
     * Sirve como estructura general y respeta las barras del sistema (status y navegación).
     */
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->

        /**
         * Column principal
         * ----------------
         * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
         * Es el contenedor vertical general de la pantalla de perfil.
         * Sirve para apilar con un margen lateral la cabecera, los datos y los servicios,
         * permitiendo hacer scroll si el contenido no cabe en la pantalla.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            /**
             * Row de la cabecera
             * ------------------
             * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Row)
             * Es la fila superior que junta la flecha de volver con el título.
             * Sirve para retroceder a la lista y mostrar en qué pantalla estamos.
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
                    text = "Perfil de cliente",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            /**
             * Column de la cabecera del cliente
             * ----------------------------------
             * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
             * Es el bloque que centra la foto, el nombre y el estado del cliente.
             * Sirve para presentar visualmente al cliente al inicio del perfil.
             */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                if (cliente?.foto.isNullOrEmpty()) {
                    /**
                     * Box del placeholder de la foto
                     * ------------------------------
                     * ✔ TIPO: bloque condicional (if) + función @Composable (androidx.compose.foundation.layout.Box)
                     * Es el círculo con el icono de persona que se muestra cuando el cliente no tiene foto.
                     * Sirve para mantener la cabecera visualmente completa con el mismo estilo del formulario.
                     */
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, Color(0xFF64B5F6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Foto del cliente",
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = File(cliente?.foto.orEmpty()),
                        contentDescription = "Foto del cliente",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF64B5F6), CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                /**
                 * Text del nombre
                 * ---------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
                 * Es el nombre del cliente que se muestra en el perfil.
                 * Sirve para presentar el nombre con un estilo destacado y centrado.
                 */
                Text(
                    text = cliente?.nombre ?: "Cargando...",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                /**
                 * Surface del estado
                 * ------------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Surface)
                 * Es el chip redondeado que muestra el estado del cliente.
                 * Sirve para indicar de un vistazo si el cliente está activo, moroso o de baja.
                 */
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colorEstado.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = textoEstado,
                        color = colorEstado,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            /**
             * Título de la sección de contacto
             * ---------------------------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
             * Es el encabezado de la sección de datos de contacto del cliente.
             * Sirve para ordenar el perfil por secciones con el mismo estilo del formulario.
             */
            Text(
                text = "Datos de contacto",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Icono de teléfono",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = cliente?.telefono ?: "No disponible",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Icono de email",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = cliente?.email ?: "Sin email",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = "Icono de DNI",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = cliente?.dni ?: "Cargando...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            /**
             * Título de la sección de otros datos
             * ------------------------------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
             * Es el encabezado de la sección de otros datos del cliente.
             * Sirve para mostrar fecha de nacimiento, llave y observaciones con el mismo estilo.
             */
            Text(
                text = "Otros datos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Icono de fecha de nacimiento",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fecha de nacimiento",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = cliente?.let { formatearFechaPerfil(it.fechaNacimiento) } ?: "No disponible",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Icono de llave",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tiene llave",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (cliente?.tieneLlave == true) "Sí" else "No",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            cliente?.observaciones?.takeIf { it.isNotBlank() }?.let { observaciones ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = "Icono de observaciones",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Observaciones",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = observaciones,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            /**
             * Título de la sección de servicios
             * ---------------------------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
             * Es el encabezado de la sección de servicios contratados del cliente.
             * Sirve para encabezar la lista de servicios con el mismo estilo del formulario.
             */
            Text(
                text = "Servicios contratados",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )

            ServicioItem(
                nombreServicio = "Sala de máquinas",
                iconoServicio = Icons.Default.FitnessCenter
            )

            ServicioItem(
                nombreServicio = "CrossFit",
                iconoServicio = Icons.Default.Bolt
            )

            /**
             * Button de Modificar cliente
             * ---------------------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.Button)
             * Es el botón que abre el formulario de edición del cliente.
             * Sirve para navegar a la pantalla de modificar con los datos precargados,
             * con el mismo estilo azul que el botón de guardar del formulario.
             */
            Button(
                onClick = {
                    navController.navigate(Routes.modificarCliente(idCliente))
                },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5),
                    contentColor = Color.White
                )
            ) {
                Text("Modificar cliente")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * formatearFechaPerfil
 * --------------------
 * ✔ TIPO: función privada (private fun) → String
 * Es la función que convierte un timestamp en milisegundos a texto legible.
 * Sirve para mostrar la fecha de nacimiento del cliente con formato dd/MM/aaaa.
 */
private fun formatearFechaPerfil(millis: Long): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}
