package com.roberto.gestorpro.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * AccionSeleccionContextual
 * -------------------------
 * Acción de presentación para la barra contextual de selección. Contiene solo
 * etiqueta, color de acento (opcional) y el callback; NO contiene lógica de
 * negocio de Clientes ni de Economía.
 */
data class AccionSeleccionContextual(
    val etiqueta: String,
    val onClick: () -> Unit,
    val color: Color = Color(0xFF1E88E5)
)

/**
 * BarraSeleccionContextual
 * ------------------------
 * Barra contextual común para el modo de selección múltiple de Clientes y de
 * Economía (movimientos). Es SOLO presentación; cada pantalla aporta las
 * acciones (etiqueta + color + callback).
 *
 * Distribución (SIEMPRE visible, sin menú ⋮):
 *
 *     [←]   N   Acción1  Acción2  Acción3
 *
 * - El botón de salir y el contador (SOLO el número, sin la palabra
 *   "seleccionado(s)") se miden con su ancho intrínseco y quedan SIEMPRE
 *   visibles en una sola línea, sin comprimirse.
 * - Solo la ZONA DE ACCIONES ocupa el espacio restante; si no cabe en una
 *   pantalla estrecha, se desplaza horizontalmente sin afectar al contador.
 *
 * Estilo: fondo neutro `MaterialTheme.colorScheme.surface` con una elevación
 * tonal ligera (2–4 dp). No se usan fondos de color fuerte (ni azul, verde,
 * rojo ni amarillo completos); los colores semánticos van solo en el
 * texto/iconos de cada acción.
 */
@Composable
fun BarraSeleccionContextual(
    numeroSeleccionados: Int,
    onSalir: () -> Unit,
    accionesPrincipales: List<AccionSeleccionContextual> = emptyList(),
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Salir (siempre visible, color normal de la aplicación).
            AppNavigationBackButton(onClick = onSalir)

            Spacer(modifier = Modifier.width(8.dp))

            // 2. Contador: SOLO el número (mínimo espacio, siempre visible).
            Text(
                text = numeroSeleccionados.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.padding(end = 8.dp)
            )

            // 3. Zona de acciones: ocupa el espacio restante y, si no cabe, se
            //    desplaza horizontalmente. El salir y el contador no se tocan.
            if (accionesPrincipales.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    accionesPrincipales.forEach { accion ->
                        TextButton(
                            onClick = accion.onClick,
                            contentPadding = PaddingValues(
                                horizontal = 10.dp,
                                vertical = 0.dp
                            )
                        ) {
                            Text(
                                text = accion.etiqueta,
                                color = accion.color,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
