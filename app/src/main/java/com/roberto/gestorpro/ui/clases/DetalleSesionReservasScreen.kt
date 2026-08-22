package com.roberto.gestorpro.ui.clases

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.roberto.gestorpro.model.ReservaConCliente
import com.roberto.gestorpro.model.SesionConClase
import com.roberto.gestorpro.ui.viewmodel.ClaseViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * DetalleSesionReservasScreen.kt
 * ------------------------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de detalle de sesión)
 * Es el archivo que define la pantalla que muestra las reservas de una sesión concreta.
 * Sirve para que el administrador vea quién ha reservado plaza y cuántas quedan libres.
 */

/**
 * DetalleSesionReservasScreen
 * ---------------------------
 * ✔ TIPO: función @Composable
 * Es la pantalla de detalle con los datos de la sesión y su lista de reservas.
 * Sirve para localizar la sesión por su id entre las sesiones activas del día,
 * cargar sus reservas a través de ClaseViewModel y presentarlas en una lista.
 */
@Composable
fun DetalleSesionReservasScreen(
    /**
     * navController
     * -------------
     * ✔ TIPO: parámetro (param) → NavHostController
     * Es el controlador de navegación que recibe la pantalla.
     * Sirve para volver atrás hacia la lista de clases.
     */
    navController: NavHostController,
    /**
     * idSesion
     * --------
     * ✔ TIPO: parámetro (param) → Int
     * Es el identificador de la sesión cuyas reservas se quieren ver.
     * Sirve para buscar la sesión dentro de las sesiones activas cargadas del día.
     */
    idSesion: Int,
    /**
     * viewModel
     * ---------
     * ✔ TIPO: parámetro (param) → ClaseViewModel (inyectado por Hilt)
     * Es el ViewModel compartido de clases y sesiones.
     * Sirve para obtener la sesión seleccionada y la lista de reservas con cliente.
     */
    viewModel: ClaseViewModel = hiltViewModel()
) {

    /**
     * LaunchedEffect(idSesion)
     * ------------------------
     * ✔ TIPO: efecto de composición (LaunchedEffect)
     * Se lanza al entrar en la pantalla.
     * Sirve para pedir al ViewModel la carga de las sesiones activas de hoy,
     * entre las que se encontrará la sesión cuyo id llega por navegación.
     */
    LaunchedEffect(idSesion) {
        viewModel.cargarSesionesActivas()
    }

    /**
     * sesiones / sesion
     * -----------------
     * ✔ TIPO: variable observable + variable derivada (val)
     * Son la lista de sesiones activas del día y la sesión buscada dentro de ella.
     * Sirven para encontrar los datos completos (nombre de clase, hora, plazas)
     * de la sesión cuyo id se recibió como argumento de ruta.
     */
    val sesiones by viewModel.sesionesActivas.collectAsState()
    val sesion = sesiones.firstOrNull { it.idSesion == idSesion }

    /**
     * LaunchedEffect(sesion)
     * ----------------------
     * ✔ TIPO: efecto de composición (LaunchedEffect)
     * Se lanza cuando la sesión buscada aparece en la lista cargada.
     * Sirve para pedir al ViewModel las reservas de esa sesión solo una vez,
     * comprobando antes si ya está seleccionada para no recargarlas en cada recomposición.
     */
    LaunchedEffect(sesion) {
        if (sesion != null &&
            viewModel.sesionDetalleSeleccionada.value?.idSesion != idSesion
        ) {
            viewModel.cargarReservasSesion(sesion)
        }
    }

    /**
     * DisposableEffect
     * ----------------
     * ✔ TIPO: efecto de ciclo de vida (DisposableEffect)
     * Se ejecuta al abandonar la pantalla.
     * Sirve para limpiar la sesión seleccionada y las reservas del detalle
     * en el ViewModel, dejándolo listo para la próxima apertura.
     */
    DisposableEffect(Unit) {
        onDispose {
            viewModel.limpiarDetalleSesion()
        }
    }

    /**
     * reservas
     * --------
     * ✔ TIPO: variable observable (val by collectAsState) → List<ReservaConCliente>
     * Es la lista de reservas de la sesión con los datos de cada cliente.
     * Sirve para pintar la lista final de asistentes de la sesión.
     */
    val reservas by viewModel.reservasDetalle.collectAsState()

    /**
     * formatoFecha
     * ------------
     * ✔ TIPO: variable (val) → DateTimeFormatter
     * Es el formateador que convierte la fecha de la sesión en texto legible en español.
     * Sirve para mostrar el día completo ("lunes, 12 de mayo de 2026") en la cabecera.
     */
    val formatoFecha = remember {
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale("es"))
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            /**
             * Row de la cabecera
             * ------------------
             * ✔ TIPO: función @Composable (Row)
             * Es la fila superior que junta la flecha de volver con el título.
             * Sirve para retroceder a la pantalla de clases e indicar dónde estamos.
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
                    text = "Reservas de la sesión",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (sesion == null) {

                /**
                 * Texto de carga
                 * --------------
                 * ✔ TIPO: bloque condicional (if) + Text
                 * Es el aviso mostrado mientras la sesión aún no aparece en la lista cargada.
                 * Sirve para informar de que se están recuperando los datos de la sesión.
                 */
                Text(
                    text = "Cargando sesión...",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            } else {

                /**
                 * Tarjeta resumen de la sesión
                 * ----------------------------
                 * ✔ TIPO: bloque Composable (Card)
                 * Es la tarjeta con los datos generales de la sesión.
                 * Sirve para ver de un vistazo clase, fecha, hora y ocupación de plazas.
                 */
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = sesion.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1E88E5)
                        )

                        FilaDatoSesion(
                            icono = Icons.Default.CalendarMonth,
                            texto = fechaLegible(sesion.fecha, formatoFecha)
                        )

                        FilaDatoSesion(
                            icono = Icons.Default.EventSeat,
                            texto = "Plazas: ${sesion.capacidadMaxima - sesion.plazasDisponibles} de ${sesion.capacidadMaxima} reservadas"
                        )
                    }
                }

                /**
                 * Título de la lista de reservas
                 * ------------------------------
                 * ✔ TIPO: función @Composable (Text)
                 * Es el encabezado de la sección de reservas.
                 * Sirve para indicar cuántos clientes han reservado la sesión.
                 */
                Text(
                    text = "Clientes reservados (${reservas.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )

                /**
                 * Contenido de la lista de reservas
                 * ---------------------------------
                 * ✔ TIPO: bloque condicional (if/else) + LazyColumn
                 * Es la zona que lista los clientes reservados o avisa de que no hay ninguno.
                 * Sirve para consultar rápidamente quién asiste a la sesión sin salir de la app.
                 */
                if (reservas.isEmpty()) {
                    Text(
                        text = "No hay reservas para esta sesión",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(reservas, key = { it.idReserva }) { reserva ->
                            ItemReservaCliente(reserva)
                        }
                    }
                }
            }
        }
    }
}

/**
 * FilaDatoSesion
 * --------------
 * ✔ TIPO: función @Composable privada
 * Es una fila reutilizable que junta un icono con un texto de dato de la sesión.
 * Sirve para mantener el mismo estilo en la fecha y las plazas del resumen.
 */
@Composable
private fun FilaDatoSesion(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = Color(0xFF1E88E5),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * ItemReservaCliente
 * ------------------
 * ✔ TIPO: función @Composable privada
 * Es la tarjeta que representa la reserva de un cliente concreto.
 * Sirve para mostrar su nombre completo y teléfono dentro de la lista de la sesión.
 */
@Composable
private fun ItemReservaCliente(reserva: ReservaConCliente) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${reserva.nombre} ${reserva.apellidos}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = reserva.telefono.ifBlank { "Sin teléfono" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ID cliente: ${reserva.idCliente}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * fechaLegible
 * ------------
 * ✔ TIPO: función privada (private fun) → String
 * Es la función que convierte el timestamp de la sesión en texto con formato largo.
 * Sirve para mostrar la fecha completa de la sesión en la cabecera del detalle.
 */
private fun fechaLegible(millis: Long, formato: DateTimeFormatter): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(formato)
}
