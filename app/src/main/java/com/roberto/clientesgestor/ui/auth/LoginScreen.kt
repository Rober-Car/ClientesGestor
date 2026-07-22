package com.roberto.clientesgestor.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavHostController
import com.roberto.clientesgestor.navigation.Routes

/**
 * LoginScreen
 * -----------
 * ✔ Tipo: función @Composable
 *
 * ¿Qué hace?
 * Dibuja la pantalla de inicio de sesión.
 *
 * Elementos clave:
 * - Recibe un NavHostController para poder navegar.
 * - Muestra textos y un botón.
 * - Gestiona estados internos si la pantalla los necesita.
 */
@Composable
fun LoginScreen(

    navController: NavHostController

) {


    /**
     * mensaje
     * -------
     * ✔ TIPO: variable con estado (var)
     * ✔ MECANISMO: delegación con 'by'
     * ✔ ORIGEN DEL ESTADO: remember { mutableStateOf(...) }
     *
     * ¿Qué es?
     * Una **variable de estado** que Compose recuerda entre recomposiciones.
     * No es una variable normal: está conectada al sistema de UI.
     *
     * ¿Cómo funciona?
     * - `remember` → ✔ FUNCIÓN que guarda un valor dentro del Composable.
     * - `mutableStateOf` → ✔ FUNCIÓN que crea un **estado observable**.
     * - `by` → ✔ OPERADOR de delegación que permite usar la variable como si fuera normal.
     *
     * Cuando el valor cambia:
     * - Compose detecta el cambio.
     * - Vuelve a dibujar solo las partes necesarias.
     *
     * ¿Qué hace?
     * Guarda el texto que se muestra en pantalla (inicialmente el nombre de la app).
     *
     * ¿Para qué sirve?
     * Para que, al pulsar el botón, el texto cambie automáticamente
     * y la UI se actualice sin que tú tengas que hacer nada más.
     *
     * ¿Qué elementos contiene?
     * nombreApp → ✔ PARÁMETRO de la función LoginScreen
     * remember → ✔ FUNCIÓN de Compose
     * mutableStateOf → ✔ FUNCIÓN que crea un estado
     * by → ✔ OPERADOR de delegación
     */
    var mensaje by remember {
        mutableStateOf("ClientesGestor")
    }


    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    /**
     * formularioValido
     * ----------------
     * ✔ TIPO DE DATO: Boolean
     *
     *  Es Una variable booleana que guarda el resultado de una condición.
     * Evalúa dos comprobaciones
     *  - email.isNotBlank()
     *  - password.isNotBlank()
     * Y combina ambas usando el operador lógico AND (&&).
     *
     * Funciona como un condicional. Es una expresión lógica que decide un valor.
     *
     * ¿Cómo se lee?
     * “formularioValido es true si email NO está vacío Y password NO está vacía”.
     *
     * .isNotBlank()
     * ------------------
     * ✔ TIPO: función de extensión de String
     * ✔ DEVUELVE: Boolean
     * Devuelve true si el texto no está vacío y no son solo espacios.
     *
     * &&
     * --
     * ✔ TIPO: operador lógico AND
     * Devuelve true solo si las dos condiciones son true.
     */
    val formularioValido =
        email.isNotBlank() &&
                password.isNotBlank()
    /**
     * Column
     * ------
     * ✔ Tipo: contenedor vertical
     *
     * ¿Qué es?
     * Un layout que coloca elementos uno debajo del otro.
     *
     * ¿Qué hace?
     * - Ocupa toda la pantalla.
     * - Centra todo vertical y horizontalmente.
     *
     * ¿Para qué sirve?
     * Para organizar la pantalla del login de forma limpia y centrada.
     */
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /**
         * Text(text = mensaje)
         * --------------------
         * ✔ Tipo: texto en pantalla
         *
         * ¿Qué hace?
         * Muestra el contenido de la variable "mensaje".
         *
         * ¿Para qué sirve?
         * Para enseñar el nombre de la app o el mensaje actualizado.
         */
        Text(
            text = mensaje,
            modifier = Modifier.padding(32.dp)
        )



        /**
         * deja un hueco vertical de 16dp, entre los dos elemntos
         * se puede usar tambien Modifier.padding(...)
         * */
        Spacer(modifier = Modifier.height(16.dp))
        /**
         * OutlinedTextField:
         * -------------------
         * Es un campo de texto con borde (estilo Material 3).
         * Sirve para que el usuario escriba información, en este caso, un email.
         *
         * value = email:
         * ---------------
         * Qué es: el contenido actual del campo.
         * Qué hace: muestra en pantalla el valor de la variable email.
         * Para qué sirve: mantiene sincronizado el texto del usuario con tu variable de estado.
         *
         * onValueChange = { email = it }:
         * -------------------------------
         * Qué es: una función que se ejecuta cada vez que el usuario escribe algo.
         * Qué hace: actualiza la variable email con el nuevo texto (it).
         * Para qué sirve: permite que el campo sea controlado, es decir, que tú tengas el control del valor.
         * Cada vez que el usuario escriba algo lo guárda en email.
         *
         * label = { Text("Email") }:
         * ---------------------------º
         * Qué es: el texto flotante del campo.
         * Qué hace: muestra “Email” encima del borde cuando el usuario escribe.
         * Para qué sirve: indica qué debe introducir el usuario.
         * */

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
            }, label = {
                Text("Email")
            }
        )

        /**
         * deja un hueco vertical de 16dp, entre los dos elemntos
         * se puede usar tambien Modifier.padding(...)
         * */

        Spacer(modifier = Modifier.height(16.dp))



        OutlinedTextField(

            /**
             * value
             * -----
             * ✔ TIPO: parámetro
             * ✔ CONTENIDO: variable que contiene el texto actual del campo.
             * Muestra el texto que el usuario ha escrito hasta ahora.
             */
            value = password,

            /**
             * onValueChange
             * -------------
             * ✔ TIPO: parámetro función (lambda)
             * ✔ RECIBE: el nuevo texto escrito por el usuario.
             * Actualiza la variable "password" con el texto nuevo.
             */
            onValueChange = {
                password = it
            },

            /**
             * label
             * -----
             * ✔ TIPO: parámetro composable
             * ✔ CONTIENE: un Text() → función composable
             * Muestra un texto pequeño encima del campo indicando qué debe escribir el usuario.
             */
            label = {
                Text("Contraseña")   // ← FUNCIÓN composable | muestra la etiqueta del campo
            }

        )




        /**
         * deja un hueco vertical de 16dp, entre los dos elemntos
         * se puede usar tambien Modifier.padding(...)
         * */
        Spacer(modifier = Modifier.height(16.dp))


        Text(
            text = "Has escrito: $email"
        )
        /**
         * Segundo texto informativo
         * -------------------------
         * Muestra una frase fija debajo del mensaje principal.
         */

        Spacer(
            modifier = Modifier.height(16.dp)
        )


        Text("Gestión de clientes y cuotas")




        /**
         * Button
         * ------
         * ✔ TIPO: función @Composable
         * ✔ CLASE REAL: androidx.compose.material3.Button
         *
         * ¿Qué es?
         * Un componente interactivo que el usuario puede pulsar.
         *
         * ¿Qué hace?
         * Ejecuta el bloque de código definido en el parámetro `onClick`cuando el usuario toca el botón.
         *
         * Parámetro: onClick
         * ------------------
         * ✔ TIPO: función lambda
         *
         * ¿Qué hace?
         * - Actualiza la variable de estado `mensaje` con el texto "Bienvenido".
         * - Llama a navController.navigate(Routes.HOME) para cambiar de pantalla.
         *
         *
         * Parámetro: enabled
         * ------------------
         * ✔ TIPO: Boolean
         *
         * ¿Qué hace?
         * Controla si el botón está activo o desactivado.
         * - true → el usuario puede pulsarlo.
         * - false → aparece deshabilitado y no responde.
         *
         * Contenido del botón
         * -------------------
         * Text("Entrar")
         * ✔ TIPO: función @Composable
         * ✔ CLASE REAL: androidx.compose.material3.Text
         *
         * ¿Qué hace?
         * Muestra el texto dentro del botón.
         *
         */
        Button(
            onClick = {

                mensaje = "Bienvenido"   // ← VARIABLE con estado que se actualiza

                navController.navigate(Routes.HOME)
            },

            enabled = formularioValido   // ← PARÁMETRO booleano que controla si el botón está activo
        ) {

            /**
             * Text("Entrar")
             * -------------
             * ✔ Muestra el texto dentro del botón.
             */
            Text("Entrar")
        }

    }

}
