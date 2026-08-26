package com.roberto.gestorpro.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * BotonSelectorFoto
 * -----------------
 * ✔ TIPO: función @Composable
 * Es el botón "Seleccionar/Cambiar foto" que despliega el menú con las dos
 * vías de obtención de la foto: elegir de la galería o hacer una foto con
 * la cámara. Sirve para unificar el flujo en todas las pantallas de perfil.
 *
 * @param tieneFoto Indica si ya hay una foto seleccionada (cambia el texto del botón)
 * @param onElegirGaleria Acción al elegir "Elegir de galería"
 * @param onHacerFoto Acción al elegir "Hacer una foto"
 */
@Composable
fun BotonSelectorFoto(
    tieneFoto: Boolean,
    onElegirGaleria: () -> Unit,
    onHacerFoto: () -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { menuAbierto = true }
        ) {
            Text(if (tieneFoto) "Cambiar foto" else "Seleccionar foto")
        }

        DropdownMenu(
            expanded = menuAbierto,
            onDismissRequest = { menuAbierto = false }
        ) {
            DropdownMenuItem(
                text = { Text("Elegir de galería") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null
                    )
                },
                onClick = {
                    menuAbierto = false
                    onElegirGaleria()
                }
            )
            DropdownMenuItem(
                text = { Text("Hacer una foto") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null
                    )
                },
                onClick = {
                    menuAbierto = false
                    onHacerFoto()
                }
            )
        }
    }
}
