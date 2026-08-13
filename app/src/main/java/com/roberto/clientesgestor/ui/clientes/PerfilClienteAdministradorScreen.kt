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
    /**
     * navController
     * -------------
     * ✔ TIPO: parámetro (param) → NavHostController
     * Es el controlador de navegación que recibe la pantalla de perfil.
     * Sirve para poder volver atrás hacia la lista de clientes.
     */
    navController: NavHostController,
    idCliente: Int
) {

    /**
     * Scaffold
     * --------
     * ✔ TIPO: función @Composable (androidx.compose.material3.Scaffold)
     * Es el contenedor base de la pantalla de perfil del cliente.
     * Sirve como estructura general y proporciona el innerPadding para el contenido.
     */
    Scaffold { innerPadding ->

        /**
         * Column principal
         * ----------------
         * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
         * Es el contenedor vertical general de la pantalla de perfil.
         * Sirve para apilar la cabecera con el avatar, los datos de contacto,
         * el separador y los servicios contratados.
         */
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {

            /**
             * Column de la cabecera
             * ---------------------
             * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
             * Es el bloque superior que centra el avatar y los datos principales del cliente.
             * Sirve para mostrar el icono de persona, el nombre y el estado del cliente centrados.
             */
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                /**
                 * Icon de persona
                 * ---------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Icon)
                 * Es el icono de avatar del cliente.
                 * Sirve como imagen de perfil provisional del cliente.
                 */
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Icono de persona",
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                /**
                 * Text del nombre
                 * ---------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
                 * Es el nombre del cliente que se muestra en el perfil.
                 * Sirve para presentar el nombre con un estilo destacado.
                 */
                Text(
                    text = "Nombre del Cliente",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                /**
                 * Text del estado
                 * ---------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
                 * Es el estado actual del cliente en el perfil.
                 * Sirve para indicar si el cliente está activo, moroso o de baja.
                 */
                Text(
                    text = "Estado",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            /**
             * Column de datos de contacto
             * ---------------------------
             * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
             * Es el bloque que agrupa las filas de teléfono, email y DNI.
             * Sirve para mostrar los datos de contacto del cliente apilados.
             */
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {

                /**
                 * Row del teléfono
                 * ----------------
                 * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Row)
                 * Es la fila que junta el icono de teléfono con su texto.
                 * Sirve para mostrar el teléfono de contacto del cliente.
                 */
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
                        text = "Teléfono",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                /**
                 * Row del email
                 * -------------
                 * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Row)
                 * Es la fila que junta el icono de email con su texto.
                 * Sirve para mostrar el correo electrónico de contacto del cliente.
                 */
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
                        text = "Pollinox@hotmail.com",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                /**
                 * Row del DNI
                 * -----------
                 * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Row)
                 * Es la fila que junta el icono de DNI con su texto.
                 * Sirve para mostrar el documento de identidad del cliente.
                 */
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
                        text = "4891856456D",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            /**
             * HorizontalDivider
             * -----------------
             * ✔ TIPO: función @Composable (androidx.compose.material3.HorizontalDivider)
             * Es la línea separadora entre los datos de contacto y los servicios.
             * Sirve para dividir visualmente las dos secciones del perfil.
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
             * Column de servicios contratados
             * -------------------------------
             * ✔ TIPO: función @Composable (androidx.compose.foundation.layout.Column)
             * Es el bloque que agrupa los servicios que tiene contratados el cliente.
             * Sirve para listar los servicios del cliente uno debajo de otro.
             */
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {

                /**
                 * Text de la sección de servicios
                 * ------------------------------
                 * ✔ TIPO: función @Composable (androidx.compose.material3.Text)
                 * Es el título de la sección de servicios contratados.
                 * Sirve para encabezar la lista de servicios del cliente.
                 */
                Text(
                    text = "Servicios contratados",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                /**
                 * ServicioItem de Sala de máquinas
                 * --------------------------------
                 * ✔ TIPO: componente @Composable (ServicioItem)
                 * Es el elemento que muestra el servicio "Sala de máquinas" con su icono.
                 * Sirve para indicar que el cliente tiene este servicio contratado.
                 */
                ServicioItem(
                    nombreServicio = "Sala de máquinas",
                    iconoServicio = Icons.Default.FitnessCenter
                )

                /**
                 * ServicioItem de CrossFit
                 * ------------------------
                 * ✔ TIPO: componente @Composable (ServicioItem)
                 * Es el elemento que muestra el servicio "CrossFit" con su icono.
                 * Sirve para indicar que el cliente tiene este servicio contratado.
                 */
                ServicioItem(
                    nombreServicio = "CrossFit",
                    iconoServicio = Icons.Default.Bolt
                )
            }
        }
    }
}
