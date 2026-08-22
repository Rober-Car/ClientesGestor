package com.roberto.gestorpro.ui.configuracion

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.roberto.gestorpro.ui.viewmodel.MainViewModel
import java.io.File
import java.io.FileOutputStream

/**
 * MiNegocioScreen.kt
 * ------------------
 * ✔ TIPO: archivo de código fuente Kotlin (pantalla de personalización del negocio)
 * Es el archivo que define la pantalla donde el administrador configura el nombre y el logo.
 * Sirve para que la identidad del negocio se muestre en Home y Login.
 */

/**
 * MiNegocioScreen
 * ---------------
 * ✔ TIPO: función @Composable
 * Es la pantalla de personalización con el campo de nombre y el selector de logo.
 * Sirve para editar y guardar en DataStore (a través de MainViewModel) los datos
 * de identidad del negocio que ven tanto administrador como clientes.
 */
@Composable
fun MiNegocioScreen(
    /**
     * navController
     * -------------
     * ✔ TIPO: parámetro (param) → NavHostController
     * Es el controlador de navegación que recibe la pantalla.
     * Sirve para volver atrás hacia Configuración al terminar.
     */
    navController: NavHostController,
    /**
     * mainViewModel
     * -------------
     * ✔ TIPO: parámetro (param) → MainViewModel (inyectado por Hilt)
     * Es el ViewModel de preferencias de la app.
     * Sirve para leer el nombre y logo actuales y guardar los cambios en DataStore.
     */
    mainViewModel: MainViewModel = hiltViewModel()
) {

    /**
     * nombreActual / logoActual
     * -------------------------
     * ✔ TIPO: variables observables (val by collectAsStateWithLifecycle) → String
     * Son el nombre y la ruta del logo guardados actualmente en DataStore.
     * Sirven para precargar el formulario la primera vez que se abre la pantalla.
     */
    val nombreActual by mainViewModel.nombreNegocio.collectAsStateWithLifecycle()
    val logoActual by mainViewModel.logoNegocio.collectAsStateWithLifecycle()

    /**
     * nombre / logo
     * -------------
     * ✔ TIPO: variables con estado (var) → String
     * Son el nombre y la ruta del logo que se están editando en la pantalla.
     * Sirven para no escribir directamente sobre DataStore con cada pulsación;
     * solo se guarda al pulsar el botón "Guardar cambios".
     */
    var nombre by rememberSaveable { mutableStateOf("") }
    var logo by rememberSaveable { mutableStateOf("") }

    /**
     * precargado
     * ----------
     * ✔ TIPO: variable con estado (var) → Boolean
     * Es la marca de que ya se rellenaron los campos con los datos guardados.
     * Sirve para que LaunchedEffect solo precargue una vez y no sobrescriba
     * lo que el usuario está tecleando si el Flow vuelve a emitir.
     */
    var precargado by rememberSaveable { mutableStateOf(false) }

    /**
     * LaunchedEffect(nombreActual, logoActual)
     * ---------------------------------------
     * ✔ TIPO: efecto de composición (LaunchedEffect)
     * Se ejecuta cuando llegan por primera vez los datos guardados.
     * Sirve para rellenar el formulario una sola vez mientras precargado sea false.
     */
    LaunchedEffect(nombreActual, logoActual) {
        if (!precargado) {
            nombre = nombreActual
            logo = logoActual
            if (nombreActual.isNotBlank() || logoActual.isNotBlank()) {
                precargado = true
            }
        }
    }

    /**
     * context
     * -------
     * ✔ TIPO: variable (val) → Context
     * Es el contexto de la actividad obtenido a través de LocalContext.
     * Sirve para copiar el logo elegido al almacenamiento interno de la app.
     */
    val context = LocalContext.current

    /**
     * launcherGaleria
     * ---------------
     * ✔ TIPO: variable (val) → ActivityResultLauncher<PickVisualMediaRequest>
     * Es el lanzador que abre el selector de fotos del sistema (Photo Picker).
     * Sirve para elegir el logo; al volver lo copia a memoria interna y guarda la ruta.
     */
    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val ruta = guardarImagenEnInterna(context, uri)
            if (ruta != null) {
                logo = ruta
            }
        }
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

            /**
             * Row de la cabecera
             * ------------------
             * ✔ TIPO: función @Composable (Row)
             * Es la fila superior que junta la flecha de volver con el título.
             * Sirve para retroceder a Configuración e indicar en qué pantalla estamos.
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
                    text = "Mi negocio",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            /**
             * OutlinedTextField del nombre
             * ----------------------------
             * ✔ TIPO: función @Composable (OutlinedTextField)
             * Es el campo de texto con el nombre comercial del negocio.
             * Sirve para personalizar la cabecera de Home y Login; vacío muestra "GestorPro".
             */
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del negocio") },
                placeholder = { Text("GestorPro") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            /**
             * Text del título de la sección del logo
             * --------------------------------------
             * ✔ TIPO: función @Composable (Text)
             * Es el encabezado de la sección de selección de logo.
             * Sirve para separar visualmente el nombre del logo.
             */
            Text(
                text = "Logo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            /**
             * Column de selección del logo
             * ----------------------------
             * ✔ TIPO: función @Composable (Column)
             * Es la sección que centra la vista previa del logo y sus botones.
             * Sirve para mostrar el logo actual o el icono por defecto si aún no hay ninguno.
             */
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (logo.isNotBlank()) {

                    /**
                     * AsyncImage del logo
                     * -------------------
                     * ✔ TIPO: bloque condicional + Composable (AsyncImage)
                     * Es la vista previa circular del logo elegido.
                     * Sirve para confirmar visualmente qué logo se guardará.
                     */
                    AsyncImage(
                        model = File(logo),
                        contentDescription = "Logo del negocio",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF1E88E5), CircleShape)
                    )
                } else {

                    /**
                     * Box del placeholder del logo
                     * ----------------------------
                     * ✔ TIPO: bloque condicional (else) + Composable (Box)
                     * Es el círculo con el icono por defecto cuando no hay logo elegido.
                     * Sirve para mantener la sección visualmente completa.
                     */
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, Color(0xFF1E88E5), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = null,
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    /**
                     * OutlinedButton de seleccionar/cambiar logo
                     * ------------------------------------------
                     * ✔ TIPO: función @Composable (OutlinedButton)
                     * Es el botón que abre el selector de fotos del sistema.
                     * Sirve para elegir el logo por primera vez o sustituirlo.
                     */
                    OutlinedButton(
                        onClick = {
                            launcherGaleria.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    ) {
                        Text(if (logo.isBlank()) "Seleccionar logo" else "Cambiar logo")
                    }

                    /**
                     * OutlinedButton de quitar logo
                     * -----------------------------
                     * ✔ TIPO: función @Composable (OutlinedButton)
                     * Es el botón que vacía el logo pendiente de guardar.
                     * Sirve para volver al icono por defecto; solo se aplica al guardar.
                     */
                    if (logo.isNotBlank()) {
                        OutlinedButton(
                            onClick = { logo = "" }
                        ) {
                            Text("Quitar")
                        }
                    }
                }
            }

            /**
             * Button de Guardar cambios
             * -------------------------
             * ✔ TIPO: función @Composable (Button)
             * Es el botón que persiste nombre y logo en DataStore.
             * Sirve para aplicar la personalización de golpe y volver a Configuración.
             */
            Button(
                onClick = {
                    mainViewModel.guardarNombreNegocio(nombre)
                    mainViewModel.guardarLogoNegocio(logo)
                    navController.popBackStack()
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
                Text("Guardar cambios")
            }
        }
    }
}

/**
 * guardarImagenEnInterna
 * ----------------------
 * ✔ TIPO: función privada (private fun) → String?
 * Es la función que copia la imagen elegida al almacenamiento interno de la app.
 * Sirve para que el logo no dependa del permiso temporal del URI, decodificándolo
 * con un tamaño reducido y devolviendo la ruta absoluta del archivo guardado.
 */
private fun guardarImagenEnInterna(context: Context, uri: Uri): String? {
    return try {

        /**
         * bounds
         * ------
         * ✔ TIPO: variable (val) → BitmapFactory.Options
         * Es la configuración que solo lee las dimensiones de la imagen sin cargarla completa.
         * Sirve para conocer el tamaño real del logo antes de redimensionarlo.
         */
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }

        /**
         * sample
         * ------
         * ✔ TIPO: variable (var) → Int
         * Es el factor de reducción aplicado al decodificar la imagen.
         * Sirve para no cargar en memoria una imagen gigante antes de escalarla.
         */
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= MAX_LOGO_DIMENSION ||
            bounds.outHeight / (sample * 2) >= MAX_LOGO_DIMENSION
        ) {
            sample *= 2
        }

        /**
         * bitmap
         * ------
         * ✔ TIPO: variable (val) → Bitmap
         * Es la imagen decodificada desde el URI con el tamaño reducido.
         * Sirve como base para generar el archivo final del logo.
         */
        val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null

        /**
         * archivo
         * -------
         * ✔ TIPO: variable (val) → File
         * Es el archivo PNG/JPEG final dentro de la carpeta "logos" interna.
         * Sirve para guardar el logo de forma permanente con nombre único.
         */
        val dir = File(context.filesDir, "logos").apply { mkdirs() }
        val archivo = File(dir, "logo_${System.currentTimeMillis()}.jpg")

        FileOutputStream(archivo).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, CALIDAD_LOGO, output)
        }
        bitmap.recycle()

        archivo.absolutePath
    } catch (e: Exception) {
        null
    }
}

/**
 * MAX_LOGO_DIMENSION
 * ------------------
 * ✔ TIPO: constante (private const val) → Int
 * Es la dimensión máxima en píxeles que puede tener el logo guardado.
 * Sirve para limitar el tamaño del archivo y su consumo de memoria.
 */
private const val MAX_LOGO_DIMENSION = 512

/**
 * CALIDAD_LOGO
 * ------------
 * ✔ TIPO: constante (private const val) → Int
 * Es el nivel de compresión JPEG aplicado al logo.
 * Sirve para equilibrar calidad y peso del archivo guardado.
 */
private const val CALIDAD_LOGO = 90
