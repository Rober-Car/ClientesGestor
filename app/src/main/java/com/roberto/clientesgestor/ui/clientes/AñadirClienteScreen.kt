package com.roberto.clientesgestor.ui.clientes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.roberto.clientesgestor.ui.viewmodel.ClienteViewModel
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * AñadirClienteScreen.kt
 * ----------------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de alta de clientes)
 * Es el archivo que define la pantalla para añadir un nuevo cliente.
 * Sirve para capturar los datos del cliente antes de guardarlos en la base de datos.
 */

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
    /**
     * viewModel
     * ---------
     * ✔ TIPO: parámetro (param) → ClienteViewModel (inyectado por Hilt)
     * Es el ViewModel de clientes que recibe la pantalla.
     * Sirve para guardar el nuevo cliente en la base de datos cuando se complete el formulario.
     */
    viewModel: ClienteViewModel = hiltViewModel()
) {

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

    /**
     * Column
     * ------
     * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
     * Es el contenedor vertical del formulario de alta.
     * Sirve para apilar todos los campos uno debajo de otro,
     * ocupando toda la pantalla y permitiendo hacer scroll si no caben.
     */


    Column( modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())

    ) {

        /**
         * TextField de Nombre
         * -------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.TextField)
         * Es el campo de texto donde se escribe el nombre del cliente.
         * Sirve para capturar el nombre y guardarlo en la variable nombre al teclear.
         */
        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") }
        )

        /**
         * TextField de Apellidos
         * ----------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.TextField)
         * Es el campo de texto donde se escriben los apellidos del cliente.
         * Sirve para capturar los apellidos y guardarlos en la variable apellidos al teclear.
         */
        TextField(
            value = apellidos,
            onValueChange = { apellidos = it },
            label = { Text("Apellidos") }
        )

        /**
         * TextField de DNI
         * ----------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.TextField)
         * Es el campo de texto donde se escribe el DNI del cliente.
         * Sirve para capturar el DNI y guardarlo en la variable dni al teclear.
         */
        TextField(
            value = dni,
            onValueChange = { dni = it },
            label = { Text("DNI") }
        )

        /**
         * TextField de Teléfono
         * ---------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.TextField)
         * Es el campo de texto donde se escribe el teléfono del cliente.
         * Sirve para capturar el teléfono y guardarlo en la variable telefono al teclear.
         */
        TextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") }
        )

        /**
         * TextField de Email
         * ------------------
         * ✔ TIPO: función @Composable (androidx.compose.material3.TextField)
         * Es el campo de texto donde se escribe el email del cliente.
         * Sirve para capturar el email y guardarlo en la variable email al teclear.
         */
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
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
            label = { Text("Fecha de nacimiento") },
            placeholder = { Text("dd/MM/aaaa") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Seleccionar fecha de nacimiento"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { mostrarDatePicker = true }
        )

        /**
         * Bloque de selección de foto
         * ----------------------------
         * ✔ TIPO: Column anidada (androidx.compose.foundation.layout.Column)
         * Es la sección que muestra la foto del cliente y el botón para elegirla.
         * Sirve para centrar la vista previa de la foto y abrir el selector de imágenes.
         */
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
                        .size(120.dp)
                        .clip(CircleShape)
                )
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
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Text(if (foto.isEmpty()) "Seleccionar foto" else "Cambiar foto")
            }
        }
    }

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
         * options / bounds
         * ----------------
         * ✔ TIPO: variables (val) → BitmapFactory.Options
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
