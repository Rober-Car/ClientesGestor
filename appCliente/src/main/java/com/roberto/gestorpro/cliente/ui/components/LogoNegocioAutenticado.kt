package com.roberto.gestorpro.cliente.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.roberto.gestorpro.cliente.data.firebase.LogoNegocioCache
import java.io.File

/**
 * LogoNegocioAutenticado
 * ----------------------
 * Muestra el logo del negocio cuando `url` es una URL remota de Firebase
 * Storage, descargándola con el SDK AUTENTICADO y caché local
 * (LogoNegocioCache). Mientras no está disponible no pinta nada.
 */
@Composable
fun LogoNegocioAutenticado(
    url: String,
    contentDescription: String,
    tamano: Dp,
    contentScale: ContentScale = ContentScale.Crop,
    modifier: Modifier = Modifier
) {
    val esUrl = LogoNegocioCache.esUrlLogo(url)
    val context = LocalContext.current
    var fichero by remember(url, esUrl) { mutableStateOf<File?>(null) }
    LaunchedEffect(url, esUrl) {
        fichero = if (esUrl) {
            LogoNegocioCache.obtener(context, url)
        } else {
            null
        }
    }
    val logo = fichero ?: return
    AsyncImage(
        model = logo,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
            .size(tamano)
            .clip(CircleShape)
    )
}
