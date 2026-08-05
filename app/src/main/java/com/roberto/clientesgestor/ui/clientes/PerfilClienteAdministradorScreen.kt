package com.roberto.clientesgestor.ui.clientes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.roberto.clientesgestor.ui.components.ServicioItem

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

    navController: NavHostController


) {

    /**
     * Scaffold
     * --------
     * ✔ TIPO: Scaffold (estructura de Material Design)
     * Es un contenedor de alto nivel que organiza la pantalla en zonas.
     * Sirve para estructurar la pantalla de perfil con un layout coherente.
     */
    Scaffold { innerPadding ->

        /**
         * Column
         * ------
         * ✔ TIPO: Column (layout vertical)
         * Es un contenedor que coloca elementos uno debajo del otro.
         * Sirve para apilar verticalmente el contenido de la pantalla de perfil.
         */
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {


            /**
             * Column (interior)
             * -----------------
             * Column anidado que agrupa los elementos de detalle del cliente.
             * Tipo Column detallado más arriba.
             */
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()

            ) {

                /**
                 * Icon (persona)
                 * --------------
                 * ✔ TIPO: Icon
                 * Es el icono vectorial que representa al cliente.
                 * Sirve para identificar visualmente la foto o avatar del cliente.
                 */
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Icono de persona",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier
                        .size(120.dp)
                )

                /**
                 * Spacer
                 * ------
                 * ✔ TIPO: Spacer
                 * Es un elemento invisible que ocupa espacio.
                 * Sirve para dejar un hueco vertical de 8dp entre elementos.
                 */
                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                /**
                 * Text("Nombre del Cliente")
                 * --------------------------
                 * ✔ TIPO: Text
                 * Es el nombre del cliente.
                 * Sirve para mostrar el nombre de forma destacada.
                 */
                Text(
                    text = "Nombre del Cliente",
                    style = MaterialTheme.typography.headlineMedium

                )

                /**
                 * Spacer igual que el detallado más arriba.
                 */
                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                /**
                 * Text("Estado")
                 * -------------
                 * Texto con el estado del cliente.
                 * Tipo Text detallado más arriba con "Nombre del Cliente".
                 */
                Text(

                    text = "Estado",
                    style = MaterialTheme.typography.bodyLarge
                )

            }

            /**
             * Spacer igual que el detallado más arriba.
             */
            Spacer(
                modifier = Modifier.height(8.dp)
            )

            /**
             * Column (datos)
             * --------------
             * Column que agrupa las filas de datos de contacto.
             * Tipo Column detallado más arriba.
             */
            Column(

                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)


            ) {

                /**
                 * Row (teléfono)
                 * --------------
                 * ✔ TIPO: Row (layout horizontal)
                 * Es un contenedor que coloca elementos uno al lado del otro.
                 * Sirve para mostrar el icono y el dato de contacto juntos.
                 */
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    /**
                     * Icon (teléfono)
                     * ---------------
                     * Icono que representa el teléfono del cliente.
                     * Tipo Icon detallado más arriba con "persona".
                     */
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Icono de teléfono",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier
                            .size(20.dp)
                    )

                    /**
                     * Spacer igual que el detallado más arriba.
                     */
                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    /**
                     * Text("Teléfono")
                     * ---------------
                     * Texto con el teléfono del cliente.
                     * Tipo Text detallado más arriba con "Nombre del Cliente".
                     */
                    Text(
                        text = "Teléfono",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                /**
                 * Spacer igual que el detallado más arriba.
                 */
                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                /**
                 * Row (email)
                 * -----------
                 * Fila con el icono y el email del cliente.
                 * Tipo Row detallado más arriba con "teléfono".
                 */
                Row(

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    /**
                     * Icon (email)
                     * ------------
                     * Icono que representa el email del cliente.
                     * Tipo Icon detallado más arriba con "persona".
                     */
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Icono de email",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier
                            .size(20.dp)
                    )

                    /**
                     * Spacer igual que el detallado más arriba.
                     */
                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    /**
                     * Text("Pollinox@hotmail.com")
                     * ---------------------------
                     * Texto con el email del cliente.
                     * Tipo Text detallado más arriba con "Nombre del Cliente".
                     */
                    Text(
                        text = "Pollinox@hotmail.com",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }


                /**
                 * Spacer igual que el detallado más arriba.
                 */
                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                /**
                 * Row (DNI)
                 * ---------
                 * Fila con el icono y el DNI del cliente.
                 * Tipo Row detallado más arriba con "teléfono".
                 */
                Row(

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    /**
                     * Icon (DNI)
                     * ----------
                     * Icono que representa el DNI del cliente.
                     * Tipo Icon detallado más arriba con "persona".
                     */
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = "Icono de DNI",
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier
                            .size(20.dp)
                    )

                    /**
                     * Spacer igual que el detallado más arriba.
                     */
                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    /**
                     * Text("4891856456D")
                     * ------------------
                     * Texto con el DNI del cliente.
                     * Tipo Text detallado más arriba con "Nombre del Cliente".
                     */
                    Text(
                        text = "4891856456D",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

            }



            /**
             * HorizontalDivider
             * -----------------
             * ✔ TIPO: HorizontalDivider
             * Es una línea divisoria horizontal que separa secciones.
             * Sirve para separar los datos de contacto de los servicios contratados.
             */
            HorizontalDivider(
                modifier = Modifier.padding(
                    horizontal = 18.dp,
                    vertical = 18.dp
                ),
                thickness = 1.dp,
                color = Color(0xFF64B5F6)
            )


            /**
             * Column (servicios)
             * ------------------
             * Column que agrupa la sección de servicios contratados.
             * Tipo Column detallado más arriba.
             */
            Column(

                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {

                /**
                 * Text("Servicios contratados")
                 * ----------------------------
                 * Título de la sección de servicios.
                 * Tipo Text detallado más arriba con "Nombre del Cliente".
                 */
                Text(

                    text = "Servicios contratados",
                    style = MaterialTheme.typography.titleLarge
                )

                /**
                 * Spacer igual que el detallado más arriba.
                 */
                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                /**
                 * ServicioItem (Sala de máquinas)
                 * -------------------------------
                 * ✔ TIPO: Composable personalizado (ServicioItem)
                 * Es la fila que muestra el icono y el nombre del servicio.
                 * Sirve para representar un servicio contratado del cliente.
                 */
                ServicioItem(
                    nombreServicio = "Sala de máquinas",
                    iconoServicio = Icons.Default.FitnessCenter
                )

                /**
                 * ServicioItem (CrossFit)
                 * -----------------------
                 * Segundo servicio contratado del cliente.
                 * Tipo ServicioItem detallado más arriba con "Sala de máquinas".
                 */
                ServicioItem(
                    nombreServicio = "CrossFit",
                    iconoServicio = Icons.Default.Bolt
                )


            }

        }

    }
}
