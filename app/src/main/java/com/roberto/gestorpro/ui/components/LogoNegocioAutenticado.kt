package com.roberto.gestorpro.ui.components

import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.roberto.gestorpro.data.firebase.LogoNegocioCache
import java.io.File

/**
 * LogoNegocioAutenticado
 * ----------------------
 * Muestra el logo del negocio cuando `url` es una URL remota de Firebase
 * Storage. La imagen se descarga con el SDK AUTENTICADO y se cachea
 * (LogoNegocioCache): nunca se hace un GET HTTP anónimo a la URL.
 * Mientras la imagen no está disponible no pinta nada (el caller mantiene el
 * placeholder). Los valores de ruta local NO deben pasar por aquí.
 */
@Composable
fun LogoNegocioAutenticado(
    url: String,
    contentDescription: String,
    tamano: Dp,
    contentScale: ContentScale = ContentScale.Crop,
    modifier: Modifier = Modifier,
    bordeColor: Color? = null,
    bordeAncho: Dp = 2.dp
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
    val conRecorte = modifier
        .size(tamano)
        .clip(CircleShape)
    val conBorde = if (bordeColor != null) {
        conRecorte.border(bordeAncho, bordeColor, CircleShape)
    } else {
        conRecorte
    }
    AsyncImage(
        model = logo,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = conBorde
    )
}
