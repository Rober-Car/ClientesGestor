package com.roberto.gestorpro.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.roberto.gestorpro.ui.auth.LoginScreen
import com.roberto.gestorpro.ui.auth.SeleccionTipoUsuarioScreen
import com.roberto.gestorpro.ui.clases.ClasesScreen
import com.roberto.gestorpro.ui.clases.CrearClaseScreen
import com.roberto.gestorpro.ui.clases.DetalleClaseScreen
import com.roberto.gestorpro.ui.clases.DetalleSesionReservasScreen
import com.roberto.gestorpro.ui.clientes.AñadirClienteScreen
import com.roberto.gestorpro.ui.clientes.ClientesScreen
import com.roberto.gestorpro.ui.clientes.MiPerfilScreen
import com.roberto.gestorpro.ui.clientes.PerfilClienteScreen
import com.roberto.gestorpro.ui.configuracion.ConfiguracionScreen
import com.roberto.gestorpro.ui.configuracion.CuentaScreen
import com.roberto.gestorpro.ui.configuracion.DatosScreen
import com.roberto.gestorpro.ui.configuracion.MiNegocioScreen
import com.roberto.gestorpro.ui.configuracion.PreferenciasScreen
import com.roberto.gestorpro.ui.economia.EconomiaScreen
import com.roberto.gestorpro.ui.home.HomeClienteScreen
import com.roberto.gestorpro.ui.home.HomeScreen
import com.roberto.gestorpro.ui.viewmodel.MainViewModel

/**
 * AppNavigation.kt
 * ----------------
 * ✔ TIPO: archivo de código fuente Kotlin (navegación)
 * Es el archivo encargado de configurar la navegación entre pantallas.
 * Sirve para tener un punto central donde se definen rutas, pantallas y el flujo de navegación de la app.
 */

/**
 * AppNavigation
 * -------------
 * ✔ TIPO: función @Composable
 * Es la función que prepara la navegación de la aplicación usando Jetpack Compose.
 * Sirve para crear el NavHost y mover al usuario entre las pantallas del proyecto.
 */
@Composable
fun AppNavigation() {

    /**
     * navController
     * -------------
     * ✔ TIPO: variable inmutable (val) → NavController
     * Es el objeto que controla la navegación entre pantallas.
     * Sirve para guardar el historial, navegar a otras rutas y volver atrás.
     */
    val navController = rememberNavController()

    /**
     * mainViewModel
     * -------------
     * ✔ TIPO: variable inmutable (val) → MainViewModel
     * Es el ViewModel de preferencias de la app.
     * Sirve para leer el tipo de usuario guardado y decidir la pantalla inicial.
     */
    val mainViewModel: MainViewModel = hiltViewModel()

    /**
     * destinoInicial
     * --------------
     * ✔ TIPO: variable observable (var by mutableStateOf) → String? (nullable)
     * Es la ruta de inicio del NavHost, calculada al arrancar.
     * Sirve para mostrar la pantalla de selección solo si el usuario aún no eligió
     * su perfil; mientras es null se muestra una pantalla de carga mínima.
     */
    var destinoInicial by remember { mutableStateOf<String?>(null) }

    /**
     * LaunchedEffect (lectura inicial de preferencias)
     * ------------------------------------------------
     * ✔ TIPO: bloque de efecto (LaunchedEffect)
     * Es el bloque que lee el tipo de usuario guardado en DataStore una sola vez.
     * Sirve para decidir si la app arranca en la pantalla de selección o en el Login.
     */
    LaunchedEffect(Unit) {
        destinoInicial = if (mainViewModel.obtenerTipoUsuario() == null) {
            Routes.SELECCION_TIPO_USUARIO
        } else {
            Routes.LOGIN
        }
    }

    /**
     * Pantalla de carga mínima
     * ------------------------
     * ✔ TIPO: condición + Composable (Box con CircularProgressIndicator)
     * Es lo que se muestra mientras se lee la preferencia de tipo de usuario.
     * Sirve para evitar parpadeos: no se monta el NavHost hasta conocer el destino real.
     */
    val destino = destinoInicial
    if (destino == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    /**
     * NavHost
     * -------
     * ✔ TIPO: función @Composable (androidx.navigation.compose.NavHost)
     * Es el contenedor principal donde se registran todas las rutas de la aplicación.
     * Sirve como el "mapa" que indica qué pantalla se muestra según la ruta actual.
     * Arranca en la pantalla de selección (primera vez) o en el Login (resto de veces).
     */
    NavHost(
        navController = navController,
        startDestination = destino
    ) {

        /**
         * Ruta SELECCION_TIPO_USUARIO
         * ---------------------------
         * ✔ TIPO: ruta de navegación (composable)
         * Es la ruta de la pantalla "¿Cómo vas a utilizar GestorPro?".
         * Sirve para que el usuario elija su perfil la primera vez que abre la app;
         * tras elegir, guarda la preferencia y navega al Login sin poder volver atrás.
         */
        composable(Routes.SELECCION_TIPO_USUARIO) {
            SeleccionTipoUsuarioScreen(navController)
        }

        /**
         * Ruta LOGIN
         * ----------
         * ✔ TIPO: ruta de navegación (composable)
         * Es la ruta que muestra la pantalla de Login.
         * Sirve para iniciar sesión y pasar el navController para poder navegar al Home.
         */
        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }

        /**
         * Ruta HOME
         * ---------
         * ✔ TIPO: ruta de navegación (composable)
         * Es la ruta que muestra la pantalla de inicio o menú principal.
         * Sirve para que desde Login se llegue al menú principal
         * y se pase el navController para poder navegar al resto de pantallas.
         */
        composable(Routes.HOME) {
            HomeScreen(navController)
        }

        /**
         * Ruta HOME_CLIENTE
         * -----------------
         * ✔ TIPO: ruta de navegación (composable)
         * Es la ruta que muestra el menú principal exclusivo del perfil Cliente.
         * Sirve para que los usuarios que entraron como Cliente tengan su propio
         * inicio, distinto al del administrador.
         */
        composable(Routes.HOME_CLIENTE) {
            HomeClienteScreen(navController)
        }

        /**
         * Ruta CLIENTES
         * -------------
         * ✔ TIPO: ruta de navegación (composable)
         * Es la ruta que muestra la pantalla de la lista de clientes.
         * Sirve para que desde el menú principal se acceda a la gestión de clientes
         * y se pase el navController para poder navegar al perfil de un cliente.
         */
        composable(Routes.CLIENTES) {
            ClientesScreen(navController)
        }

        /**
         * Ruta PERFILCLIENTE
         * ------------------
         * ✔ TIPO: ruta de navegación (composable)
         * Es la ruta que muestra la pantalla de perfil de un cliente.
         * Sirve para que desde la lista de clientes se acceda a los detalles
         * y se pase el navController para poder volver atrás.
         */
        composable(
            route = "${Routes.PERFILCLIENTE}/{idCliente}"
        ) { backStackEntry ->

            val idCliente = backStackEntry.arguments
                ?.getString("idCliente")
                ?.toIntOrNull()

            if (idCliente != null) {
                PerfilClienteScreen(
                    navController = navController,
                    idCliente = idCliente
                )
            }
        }


        /**
         * Ruta AÑADIRCLIENTE
         * ------------------
         * ✔ TIPO: ruta de navegación (composable)
         * Es la ruta que muestra el formulario para dar de alta un nuevo cliente.
         * Sirve para que desde la lista de clientes se acceda al formulario de creación
         * y se pase el navController para poder volver atrás al guardar.
         */
        composable(Routes.AÑADIRCLIENTE){

            AñadirClienteScreen(navController)
        }

        /**
         * Ruta MODIFICARCLIENTE
         * ---------------------
         * ✔ TIPO: ruta de navegación (composable) con parámetro dinámico
         * Es la ruta que muestra el formulario para modificar un cliente ya existente.
         * Sirve para que desde el perfil de un cliente se acceda al formulario de edición,
         * recibiendo el id del cliente a modificar a través de la URL.
         *
         * CÓMO FUNCIONA:
         * 1. La ruta contiene "{idCliente}" como parte dinámica de la URL.
         * 2. backStackEntry.arguments extrae los argumentos de la URL.
         * 3. Se obtiene el "idCliente" de los argumentos y se convierte a Int.
         * 4. Si el id es válido, se muestra AñadirClienteScreen pasándole el id
         *    para que precargue los datos del cliente en el formulario.
         * 5. Si el id no es válido (null), no se muestra nada.
         */
        composable(
            route = "${Routes.MODIFICARCLIENTE}/{idCliente}"
        ) { backStackEntry ->

            val idCliente = backStackEntry.arguments
                ?.getString("idCliente")
                ?.toIntOrNull()

            if (idCliente != null) {
                AñadirClienteScreen(
                    navController = navController,
                    idCliente = idCliente
                )
            }
        }

        composable(Routes.ECONOMIA) {
            EconomiaScreen(navController)
        }

        /**
         * Ruta MIPERFIL
         * -------------
         * ✔ TIPO: ruta de navegación (composable)
         * Es la ruta que muestra la ficha del propio cliente del dispositivo.
         * Sirve para que la tarjeta "Mi perfil" de HomeClienteScreen llegue aquí;
         * la pantalla decide sola si pedir el registro o mostrar los datos guardados.
         */
        composable(Routes.MIPERFIL) {
            MiPerfilScreen(navController)
        }

        /**
         * Ruta REGISTRO_CLIENTE
         * ---------------------
         * ✔ TIPO: ruta de navegación (composable)
         * Es la ruta del formulario de alta del propio cliente desde "Mi perfil".
         * Sirve para reutilizar AñadirClienteScreen en modo registro (modoRegistroCliente
         * = true), ocultando los campos exclusivos del administrador; al guardar, el id
         * creado se registra como sesión y se navega a Mi perfil.
         */
        composable(Routes.REGISTRO_CLIENTE) {
            AñadirClienteScreen(
                navController = navController,
                modoRegistroCliente = true
            )
        }

        /**
         * Ruta MODIFICAR_MIPERFIL
         * -----------------------
         * ✔ TIPO: ruta de navegación (composable) con parámetro dinámico
         * Es la ruta que muestra el formulario para que el cliente edite sus propios datos.
         * Sirve para abrir AñadirClienteScreen en modo registro y edición a la vez:
         * precarga el cliente del id recibido y oculta los campos del administrador.
         */
        composable(
            route = "${Routes.MODIFICAR_MIPERFIL}/{idCliente}"
        ) { backStackEntry ->

            val idCliente = backStackEntry.arguments
                ?.getString("idCliente")
                ?.toIntOrNull()

            if (idCliente != null) {
                AñadirClienteScreen(
                    navController = navController,
                    idCliente = idCliente,
                    modoRegistroCliente = true
                )
            }
        }

        composable(Routes.CONFIGURACION) {
            ConfiguracionScreen(navController)
        }

        /**
         * Ruta MINEGOCIO
         * --------------
         * ✔ TIPO: ruta de navegación (composable)
         * Es la ruta de la pantalla de personalización del negocio.
         * Sirve para que el administrador configure el nombre y el logo
         * que se muestran en Home y Login.
         */
        composable(Routes.MINEGOCIO) {
            MiNegocioScreen(navController)
        }

        composable(Routes.PREFERENCIAS) {
            PreferenciasScreen(navController)
        }

        composable(Routes.DATOS) {
            DatosScreen(navController)
        }

        composable(Routes.CUENTA) {
            CuentaScreen(navController)
        }

        composable(Routes.CLASES) {
            ClasesScreen(navController)
        }

        composable(Routes.CLIENTE_CLASES) {
            ClasesScreen(navController, esCliente = true)
        }

        composable(Routes.CREAR_CLASE) {
            CrearClaseScreen(navController)
        }

        composable(
            route = "${Routes.DETALLE_CLASE}/{idClase}"
        ) { backStackEntry ->
            val idClase = backStackEntry.arguments
                ?.getString("idClase")
                ?.toIntOrNull()
            if (idClase != null) {
                DetalleClaseScreen(
                    navController = navController,
                    idClase = idClase
                )
            }
        }

        composable(
            route = "${Routes.DETALLE_SESION_RESERVAS}/{idSesion}"
        ) { backStackEntry ->
            val idSesion = backStackEntry.arguments
                ?.getString("idSesion")
                ?.toIntOrNull()
            if (idSesion != null) {
                DetalleSesionReservasScreen(
                    navController = navController,
                    idSesion = idSesion
                )
            }
        }

    }
}
