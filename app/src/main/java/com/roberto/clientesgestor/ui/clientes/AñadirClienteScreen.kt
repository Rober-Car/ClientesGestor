/* ============================================================
 * ============ BLOQUE 1: IMPORTS =============================
 * ============================================================ */
package com.roberto.clientesgestor.ui.clientes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.roberto.clientesgestor.data.entity.ClienteEntity
import com.roberto.clientesgestor.model.EstadoCliente
import com.roberto.clientesgestor.ui.viewmodel.ClienteViewModel
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/* ============================================================
 * ============ BLOQUE 2: DOCUMENTACIÓN DEL ARCHIVO ===========
 * ============================================================ */
/**
 * AñadirClienteScreen.kt
 * ----------------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de alta de clientes)
 * Es el archivo que define la pantalla para añadir un nuevo cliente.
 * Sirve para capturar los datos del cliente antes de guardarlos en la base de datos.
 */

/* ============================================================
 * ============ BLOQUE 3: PANTALLA AÑADIR CLIENTE =============
 * ============================================================ */
/**
 * AñadirClienteScreen
 * -------------------
 * ✔ TIPO: función @Composable
 * Es la pantalla de alta de un cliente con los campos del formulario.
 * Sirve para que el administrador introduzca los datos de un nuevo cliente
 * y los prepare para ser guardados a través del ClienteViewModel.
 */
@Composable
fun AñadirClienteScreen(


    navController: NavHostController,
    /**
     * viewModel
     * ---------
     * ✔ TIPO: parámetro (param) → ClienteViewModel (inyectado por Hilt)
     * Es el ViewModel de clientes que recibe la pantalla.
     * Sirve para guardar el nuevo cliente en la base de datos cuando se complete el formulario.
     */
    viewModel: ClienteViewModel = hiltViewModel()


) {

    /* ============================================================
     * ============ BLOQUE 4: ESTADO DEL FORMULARIO ===============
     * ============================================================ */
    /**
     * nombre
     * ------
     * ✔ TIPO: variable con estado (var) → String
     * Es el nombre del cliente que se escribe en el campo.
     * Sirve para guardar el texto del campo "Nombre" mientras se rellena el formulario.
     */
    var nombre by remember { mutableStateOf("") }

    /**
     * apellidos
     * ---------
     * ✔ TIPO: variable con estado (var) → String
     * Es el apellido o apellidos del cliente que se escriben en el campo.
     * Sirve para guardar el texto del campo "Apellidos" mientras se rellena el formulario.
     */
    var apellidos by remember { mutableStateOf("") }

    /**
     * dni
     * ---
     * ✔ TIPO: variable con estado (var) → String
     * Es el DNI del cliente que se escribe en el campo.
     * Sirve para guardar el texto del campo "DNI" mientras se rellena el formulario.
     */
    var dni by remember { mutableStateOf("") }

    /**
     * telefono
     * --------
     * ✔ TIPO: variable con estado (var) → String
     * Es el teléfono del cliente que se escribe en el campo.
     * Sirve para guardar el texto del campo "Teléfono" mientras se rellena el formulario.
     */
    var telefono by remember { mutableStateOf("") }

    /**
     * email
     * -----
     * ✔ TIPO: variable con estado (var) → String
     * Es el email del cliente que se escribe en el campo.
     * Sirve para guardar el texto del campo "Email" mientras se rellena el formulario.
     */
    var email by remember { mutableStateOf("") }

    /**
     * fechaNacimiento
     * ---------------
     * ✔ TIPO: variable con estado (var) → Long?
     * Es la fecha de nacimiento del cliente convertida a timestamp.
     * Sirve para guardar la fecha en milisegundos; es null mientras no se escriba una fecha válida.
     */
    var fechaNacimiento by remember { mutableStateOf<Long?>(null) }

    /**
     * mostrarDatePicker
     * -----------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es la variable que controla si el selector de fecha está visible.
     * Sirve para abrir y cerrar el DatePickerDialog al tocar el campo de fecha.
     */
    var mostrarDatePicker by remember { mutableStateOf(false) }

    /**
     * fechaNacimientoFormateada
     * -------------------------
     * ✔ TIPO: variable con estado (val) → String
     * Es la fecha de nacimiento ya convertida en texto legible.
     * Sirve para mostrar la fecha seleccionada dentro del campo de texto de solo lectura.
     */
    val fechaNacimientoFormateada = remember(fechaNacimiento) {
        fechaNacimiento?.let { formatearFecha(it) } ?: ""
    }

    /**
     * tieneLlave
     * ----------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es el interruptor que indica si el cliente tiene llave del centro.
     * Sirve para guardar la opción "Tiene llave" marcada en el formulario.
     */
    var tieneLlave by remember { mutableStateOf(false) }

    /**
     * observaciones
     * -------------
     * ✔ TIPO: variable con estado (var) → String
     * Es el texto de observaciones que se escribe en el campo.
     * Sirve para guardar las notas opcionales sobre el cliente.
     */
    var observaciones by remember { mutableStateOf("") }

    /**
     * errorNombre
     * -----------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es el indicador de error del campo Nombre.
     * Sirve para resaltar el campo y mostrar "El nombre es obligatorio" si está vacío al pulsar Guardar.
     */
    var errorNombre by remember { mutableStateOf(false) }

    /**
     * errorApellidos
     * --------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es el indicador de error del campo Apellidos.
     * Sirve para resaltar el campo y mostrar "Los apellidos son obligatorios" si está vacío al pulsar Guardar.
     */
    var errorApellidos by remember { mutableStateOf(false) }

    /**
     * errorDni
     * --------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es el indicador de error del campo DNI.
     * Sirve para resaltar el campo y mostrar "Introduce un DNI válido" si el DNI no es correcto.
     */
    var errorDni by remember { mutableStateOf(false) }

    /**
     * errorTelefono
     * -------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es el indicador de error del campo Teléfono.
     * Sirve para resaltar el campo y mostrar "Introduce un teléfono válido" si el teléfono no es correcto.
     */
    var errorTelefono by remember { mutableStateOf(false) }

    /**
     * errorEmail
     * ----------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es el indicador de error del campo Email.
     * Sirve para resaltar el campo y mostrar "Introduce un email válido" si el email no es correcto.
     */
    var errorEmail by remember { mutableStateOf(false) }

    /**
     * errorFechaNacimiento
     * --------------------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es el indicador de error del campo Fecha de nacimiento.
     * Sirve para resaltar el campo y mostrar "La fecha de nacimiento es obligatoria" si no se elige ninguna.
     */
    var errorFechaNacimiento by remember { mutableStateOf(false) }

    /**
     * errorFoto
     * ---------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es el indicador de error de la foto del cliente.
     * Sirve para avisar de que la foto es obligatoria si no se selecciona ninguna al pulsar Guardar.
     */
    var errorFoto by remember { mutableStateOf(false) }

    val error by viewModel.error.collectAsState()


    /* ============================================================
     * ============ BLOQUE 5: LÓGICA DE FECHA Y GALERÍA ===========
     * ============================================================ */
    /**
     * selectableDates
     * ---------------
     * ✔ TIPO: variable con estado (val) → SelectableDates
     * Es la regla que decide qué días y años puede elegir el usuario en el calendario.
     * Sirve para impedir fechas futuras y fechas anteriores a 120 años.
     */
    val selectableDates = remember {
        val hoy = LocalDate.now()
        val hoyUtc = hoy.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val fechaMinimaUtc = hoy.minusYears(120).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis in fechaMinimaUtc..hoyUtc

            override fun isSelectableYear(year: Int): Boolean =
                year in (hoy.minusYears(120).year..hoy.year)
        }
    }

    /**
     * context
     * -------
     * ✔ TIPO: variable (val) → Context
     * Es el contexto de la actividad obtenido a través de LocalContext.
     * Sirve para acceder al almacenamiento interno de la app y guardar la foto copiada.
     */
    val context = LocalContext.current

    /**
     * foto
     * ----
     * ✔ TIPO: variable con estado (var) → String
     * Es la ruta del archivo de la foto elegida por el usuario.
     * Sirve para guardar en el formulario la foto seleccionada y mostrarla en la vista previa.
     */
    var foto by remember { mutableStateOf("") }

    /**
     * launcherGaleria
     * ---------------
     * ✔ TIPO: variable (val) → ActivityResultLauncher<PickVisualMediaRequest>
     * Es el lanzador que abre el selector de fotos del sistema (Photo Picker).
     * Sirve para que el usuario elija una imagen; al volver, guarda la foto en
     * el almacenamiento interno y muestra la ruta en la variable foto.
     */
    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val ruta = guardarFotoEnInterna(context, uri)
            if (ruta != null) {
                foto = ruta
            }
        }
    }


    /* ============================================================
     * ============ BLOQUE 6: UI - FORMULARIO DE DATOS ============
     * ============================================================ */

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
    /**
     * Column del formulario
     * ---------------------
     * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
     * Es el contenedor vertical del formulario de alta.
     * Sirve para apilar todos los campos uno debajo de otro con un margen lateral,
     * permitir hacer scroll si no caben y subir el contenido cuando se abre el teclado.
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            /**
             * IconButton de volver
             * --------------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.IconButton)
             * Es el botón con forma de icono que permite retroceder.
             * Sirve para volver a la pantalla anterior pulsando la flecha.
             */
            IconButton(
                onClick = {
                    navController.popBackStack()
                }
            ) {

                /**
                 * Icon de flecha
                 * --------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Icon)
                 * Es el icono de flecha hacia atrás del botón.
                 * Sirve para indicar visualmente la acción de volver.
                 */
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            /**
             * Text del título
             * ---------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
             * Es el título de la pantalla de clientes.
             * Sirve para indicar al usuario en qué sección se encuentra.
             */
            Text(
                text = "Añadir cliente",
                style = MaterialTheme.typography.titleLarge
            )
        }


        Text(
            text = "Datos personales",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    errorNombre = false
                },
                label = { Text("Nombre") },
                isError = errorNombre,
                supportingText = {
                    if (errorNombre) {
                        Text("El nombre es obligatorio")
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = apellidos,
                onValueChange = {
                    apellidos = it
                    errorApellidos = false
                },
                label = { Text("Apellidos") },
                isError = errorApellidos,
                supportingText = {
                    if (errorApellidos) {
                        Text("Los apellidos son obligatorios")
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = dni,
                onValueChange = {
                    dni = it
                    errorDni = false
                },
                label = { Text("DNI") },
                isError = errorDni,
                supportingText = {
                    if (errorDni) {
                        Text("Introduce un DNI válido")
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = {
                    telefono = it
                    errorTelefono = false
                },
                label = { Text("Teléfono") },
                isError = errorTelefono,
                supportingText = {
                    if (errorTelefono) {
                        Text("Introduce un teléfono válido")
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorEmail = false
            },
            label = { Text("Email") },
            isError = errorEmail,
            supportingText = {
                if (errorEmail) {
                    Text("Introduce un email válido")
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        /**
         * OutlinedTextField de Fecha de nacimiento
         * ------------------------------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.OutlinedTextField)
         * Es el campo de texto de solo lectura donde se muestra la fecha de nacimiento.
         * Sirve para mostrar la fecha ya formateada y abrir el selector de fecha al tocarlo.
         */
        OutlinedTextField(
            value = fechaNacimientoFormateada,
            onValueChange = { },
            readOnly = true,
            enabled = false,
            label = { Text("Fecha de nacimiento") },
            placeholder = { Text("dd/MM/aaaa") },
            isError = errorFechaNacimiento,
            supportingText = {
                if (errorFechaNacimiento) {
                    Text("La fecha de nacimiento es obligatoria")
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = Color.Transparent,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Seleccionar fecha de nacimiento"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    mostrarDatePicker = true
                    errorFechaNacimiento = false
                }
        )

        /* ============================================================
         * ============ BLOQUE 7: UI - SELECCIÓN DE FOTO ==============
         * ============================================================ */
        /**
         * Column de selección de foto
         * ---------------------------
         * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
         * Es la sección que muestra la foto del cliente y el botón para elegirla.
         * Sirve para centrar la vista previa de la foto y abrir el selector de imágenes.
         */
        Text(
            text = "Foto",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /**
             * AsyncImage de la foto
             * ---------------------
             * ✔ TIPO: bloque condicional (if) + función @Composable (coil3.compose.AsyncImage)
             * Es la vista previa circular de la foto elegida por el usuario.
             * Sirve para mostrar en pantalla la foto guardada y confirmar visualmente la selección.
             */
            if (foto.isNotEmpty()) {
                AsyncImage(
                    model = File(foto),
                    contentDescription = "Foto del cliente",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF64B5F6), CircleShape)
                )
            } else {
                /**
                 * Box del placeholder de la foto
                 * ------------------------------
                 * ✔ TIPO: bloque condicional (else) + función @Composable (androidx.compose.foundation.layout.Box)
                 * Es el círculo con el icono de persona que se muestra mientras no hay foto elegida.
                 * Sirve para mantener la sección visualmente completa aunque el cliente aún no tenga foto.
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
                        contentDescription = null,
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
                    launcherGaleria.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                    errorFoto = false
                }
            ) {
                Text(if (foto.isEmpty()) "Seleccionar foto" else "Cambiar foto")
            }

            if (errorFoto) {
                Text(
                    text = "La foto es obligatoria",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        /* ============================================================
         * ============ BLOQUE 8: UI - OPCIONES Y GUARDAR =============
         * ============================================================ */
        /**
         * Row de "Tiene llave"
         * --------------------
         * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Row)
         * Es la fila que junta el texto "Tiene llave" con el interruptor.
         * Sirve para activar o desactivar la opción de que el cliente tenga llave.
         */
        Text(
            text = "Otros datos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tiene llave")

            Switch(
                checked = tieneLlave,
                onCheckedChange = { tieneLlave = it }
            )
        }

        OutlinedTextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            label = { Text("Observaciones") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        /**
         * Button de Guardar cliente
         * -------------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.Button)
         * Es el botón que valida el formulario y guarda el cliente.
         * Sirve para comprobar todos los campos obligatorios a la vez, marcar los incorrectos
         * y, si no hay errores, crear el ClienteEntity y volver a la lista de clientes.
         */
        Button(
            onClick = {
                errorNombre = nombre.isBlank()
                errorApellidos = apellidos.isBlank()
                errorDni = !esDniValido(dni)
                errorTelefono = !esTelefonoValido(telefono)
                errorEmail = email.isNotBlank() && !esEmailValido(email)
                errorFechaNacimiento = fechaNacimiento == null
                errorFoto = foto.isBlank()

                val hayErrores =
                    errorNombre ||
                        errorApellidos ||
                        errorDni ||
                        errorTelefono ||
                        errorEmail ||
                        errorFechaNacimiento ||
                        errorFoto

                if (!hayErrores) {
                    val cliente = ClienteEntity(
                        nombre = nombre,
                        apellidos = apellidos,
                        dni = dni,
                        telefono = telefono,
                        email = email,
                        foto = foto,
                        fechaNacimiento = fechaNacimiento!!,
                        estado = EstadoCliente.REGISTRADO,
                        tieneLlave = tieneLlave,
                        observaciones = observaciones.ifBlank { null }
                    )

                    viewModel.insertarCliente(cliente) {
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp, bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E88E5),
                contentColor = Color.White
            )
        ) {
            Text("Guardar cliente")
        }

        /**
         * Card de error
         * -------------
         * ✔ TIPO: bloque condicional (let) + función @Composable (androidx.compose.material3.Card)
         * Es la tarjeta de aviso que se muestra cuando el guardado falla.
         * Sirve para mostrar el mensaje del ViewModel (por ejemplo "El DNI ya está registrado")
         * con un icono de aviso debajo del botón Guardar.
         */
        error?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
    /* ============================================================
     * ============ BLOQUE 9: UI - SELECTOR DE FECHA ==============
     * ============================================================ */
    /**
     * DatePickerDialog
     * ----------------
     * ✔ TIPO: bloque condicional (if) + función @Composable (androidx.compose.material3.DatePickerDialog)
     * Es el diálogo con calendario que se muestra al tocar el campo de fecha.
     * Sirve para que el usuario elija el día, mes y año de nacimiento del cliente.
     */
    if (mostrarDatePicker) {

        /**
         * datePickerState
         * ---------------
         * ✔ TIPO: variable con estado (val) → DatePickerState
         * Es el estado del selector de fecha con las fechas restringidas.
         * Sirve para conocer la fecha elegida por el usuario y confirmarla al pulsar Aceptar.
         */
        val datePickerState = rememberDatePickerState(selectableDates = selectableDates)

        DatePickerDialog(
            onDismissRequest = {
                mostrarDatePicker = false
            },
            confirmButton = {
                /**
                 * TextButton de Aceptar
                 * ---------------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.TextButton)
                 * Es el botón que confirma la fecha elegida en el calendario.
                 * Sirve para guardar la fecha en la variable fechaNacimiento y cerrar el diálogo;
                 * se mantiene deshabilitado mientras no haya una fecha seleccionada.
                 */
                TextButton(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        fechaNacimiento = datePickerState.selectedDateMillis?.let { millisUtcAMedianocheLocal(it) }
                        mostrarDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                /**
                 * TextButton de Cancelar
                 * ----------------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.TextButton)
                 * Es el botón que cierra el selector de fecha sin guardar nada.
                 * Sirve para cancelar la elección de la fecha y ocultar el diálogo.
                 */
                TextButton(
                    onClick = {
                        mostrarDatePicker = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            /**
             * DatePicker
             * ----------
             * ✔ TIPO: función @Composable (androidx.compose.material3.DatePicker)
             * Es el calendario en sí que muestra los días del mes.
             * Sirve para que el usuario seleccione con un toque el día de nacimiento.
             */
            DatePicker(
                state = datePickerState
            )
        }
    }
}

/* ============================================================
 * ============ BLOQUE 10: FUNCIONES AUXILIARES ===============
 * ============================================================ */
/**
 * formatearFecha
 * --------------
 * ✔ TIPO: función privada (private fun) → String
 * Es la función que convierte un timestamp en milisegundos a texto legible.
 * Sirve para mostrar la fecha de nacimiento en el campo de solo lectura con formato dd/MM/aaaa.
 */
private fun formatearFecha(millis: Long): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

/**
 * millisUtcAMedianocheLocal
 * -------------------------
 * ✔ TIPO: función privada (private fun) → Long
 * Es la función que convierte la medianoche UTC del selector de fecha a medianoche local.
 * Sirve para guardar la fecha en la base de datos sin desfases por la zona horaria del dispositivo.
 */
private fun millisUtcAMedianocheLocal(utcMillis: Long): Long {
    val fecha = Instant.ofEpochMilli(utcMillis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
    return fecha.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

/**
 * guardarFotoEnInterna
 * --------------------
 * ✔ TIPO: función privada (private fun) → String?
 * Es la función que copia la imagen elegida en la galería al almacenamiento interno de la app.
 * Sirve para que la foto no dependa del permiso temporal de lectura del URI,
 * comprimiéndola a JPEG y devolviendo la ruta absoluta del archivo guardado.
 */
private fun guardarFotoEnInterna(context: Context, uri: Uri): String? {
    return try {

        /**
         * bounds
         * ------
         * ✔ TIPO: variable (val) → BitmapFactory.Options
         * Es la configuración que solo lee las dimensiones de la imagen sin cargarla completa.
         * Sirve para conocer el tamaño real de la foto y decidir cuánto reducirla después.
         */
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }

        /**
         * sample
         * ------
         * ✔ TIPO: variable (var) → Int
         * Es el factor de reducción que se aplica al decodificar la imagen.
         * Sirve para que el sistema no cargue en memoria una foto gigante antes de redimensionarla.
         */
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= MAX_FOTO_DIMENSION ||
            bounds.outHeight / (sample * 2) >= MAX_FOTO_DIMENSION
        ) {
            sample *= 2
        }

        /**
         * bitmap
         * ------
         * ✔ TIPO: variable (val) → Bitmap
         * Es la imagen ya decodificada desde el URI con el tamaño reducido.
         * Sirve para redimensionarla a la dimensión máxima y comprimirla después.
         */
        val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null

        /**
         * escala / redimensionada
         * -----------------------
         * ✔ TIPO: variables (val) → Float / Bitmap
         * Son el factor de escala y la imagen ajustada a la dimensión máxima permitida.
         * Sirve para limitar el peso final de la foto y que ocupe poco en el dispositivo.
         */
        val escala = minOf(
            1f,
            MAX_FOTO_DIMENSION / maxOf(bounds.outWidth, bounds.outHeight).toFloat()
        )
        val ancho = (bitmap.width * escala).toInt()
        val alto = (bitmap.height * escala).toInt()
        val redimensionada = if (escala < 1f) {
            Bitmap.createScaledBitmap(bitmap, ancho, alto, true)
        } else {
            bitmap
        }

        /**
         * dir / archivo
         * -------------
         * ✔ TIPO: variables (val) → File
         * Son la carpeta de fotos del almacenamiento interno y el archivo JPEG final.
         * Sirven para guardar la foto de forma permanente con un nombre único por cliente.
         */
        val dir = File(context.filesDir, "fotos").apply { mkdirs() }
        val archivo = File(dir, "foto_${System.currentTimeMillis()}.jpg")

        FileOutputStream(archivo).use { output ->
            redimensionada.compress(Bitmap.CompressFormat.JPEG, FOTO_CALIDAD, output)
        }

        if (redimensionada != bitmap) {
            bitmap.recycle()
        }
        redimensionada.recycle()

        archivo.absolutePath
    } catch (e: Exception) {
        null
    }
}

/**
 * esDniValido
 * -----------
 * ✔ TIPO: función privada (private fun) → Boolean
 * Es la función que valida el formato y la letra de control de un DNI español.
 * Sirve para comprobar que el DNI tenga 8 dígitos y una letra correcta antes de guardarlo.
 */
private fun esDniValido(dni: String): Boolean {

    if (!dni.matches(Regex("\\d{8}[A-Za-z]"))) {
        return false
    }

    val numeros = dni.substring(0, 8).toInt()
    val letra = dni.last().uppercaseChar()

    val letras = "TRWAGMYFPDXBNJZSQVHLCKE"

    return letras[numeros % 23] == letra
}

/**
 * esTelefonoValido
 * ----------------
 * ✔ TIPO: función privada (private fun) → Boolean
 * Es la función que valida el formato de un teléfono móvil español.
 * Sirve para comprobar que el teléfono empiece por 6, 7, 8 o 9 y tenga 9 dígitos.
 */
private fun esTelefonoValido(telefono: String): Boolean {
    return telefono.matches(Regex("[6789]\\d{8}"))
}

/**
 * esEmailValido
 * -------------
 * ✔ TIPO: función privada (private fun) → Boolean
 * Es la función que valida el formato de un correo electrónico.
 * Sirve para comprobar que el email tenga un formato válido antes de guardarlo.
 */
private fun esEmailValido(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS
        .matcher(email)
        .matches()
}

/* ============================================================
 * ============ BLOQUE 11: CONSTANTES =========================
 * ============================================================ */
/**
 * MAX_FOTO_DIMENSION
 * ------------------
 * ✔ TIPO: constante (private const val) → Int
 * Es la dimensión máxima en píxeles que puede tener la foto guardada.
 * Sirve para redimensionar imágenes grandes y evitar que ocupen demasiado espacio.
 */
private const val MAX_FOTO_DIMENSION = 1024

/**
 * FOTO_CALIDAD
 * ------------
 * ✔ TIPO: constante (private const val) → Int
 * Es el nivel de compresión JPEG que se aplica al guardar la foto.
 * Sirve para mantener un equilibrio entre calidad de imagen y tamaño de archivo.
 */
private const val FOTO_CALIDAD = 85
