package com.roberto.clientesgestor.data.database

import android.content.Context
import androidx.room.Room

/**
 * DatabaseProvider.kt
 * -------------------
 * ✔ TIPO: archivo de código fuente Kotlin (acceso a datos)
 * Es el archivo que define el proveedor de la base de datos Room del gimnasio.
 * Sirve para centralizar la creación de la base de datos y acceder a ella desde cualquier parte de la app.
 */

/**
 * DatabaseProvider
 * ----------------
 * ✔ TIPO: object (objeto singleton de Kotlin)
 * Es el objeto único encargado de construir y devolver la base de datos Room.
 * Sirve para tener un punto central desde el que obtener la instancia de GymDatabase.
 */
object DatabaseProvider {

    /**
     * database
     * --------
     * ✔ TIPO: propiedad privada mutable (private var) → GymDatabase?
     * Es la instancia de la base de datos Room, que empieza sin valor (null).
     * Sirve para guardar la base de datos en memoria y no tener que crearla varias veces.
     */
    private var database: GymDatabase? = null

    /**
     * getDatabase
     * -----------
     * ✔ TIPO: método (fun) → GymDatabase
     * Es el método que devuelve la base de datos Room de la aplicación.
     * Sirve para construir la base de datos solo la primera vez que se solicita y reutilizarla después.
     */
    fun getDatabase(context: Context): GymDatabase {

        /**
         * return database ?: synchronized(this) { ... }
         * -----------------------------------------------
         * ✔ TIPO: expresión de retorno con operador elvis (?:) y bloque sincronizado
         * Es el retorno que devuelve la base de datos ya creada o la construye si no existe.
         * Sirve para garantizar que solo se construye una vez, incluso si varios hilos la piden a la vez.
         *
         * Operador elvis (?:):
         * ✔ TIPO: operador de Kotlin
         * Es un operador que devuelve el valor de la izquierda si NO es null, o el de la derecha si sí lo es.
         * Sirve aquí para devolver "database" directamente cuando ya existe, sin entrar al bloque sincronizado.
         *
         * synchronized(this):
         * ✔ TIPO: bloque sincronizado
         * Es un bloque que solo permite que un hilo lo ejecute a la vez sobre el objeto DatabaseProvider.
         * Sirve para que, si varios hilos piden la base de datos al mismo tiempo, solo uno la construya
         * y los demás esperen su turno para reutilizar la que ya se haya creado.
         *
         * database ?: (comprobación interna):
         * ✔ TIPO: operador elvis (?:) dentro del bloque sincronizado
         * Es la segunda comprobación de "database" dentro del bloque sincronizado.
         * Sirve como "doble chequeo": si otro hilo ya construyó la base de datos mientras este esperaba
         * su turno, se devuelve esa instancia en vez de construir otra.
         *
         * Room.databaseBuilder(context, GymDatabase::class.java, DATABASE_NAME):
         * ✔ TIPO: método (fun) de Room
         * Es el constructor de la base de datos Room, que recibe el contexto, la clase de la base y su nombre.
         * Sirve para configurar cómo se creará el archivo de la base de datos en el dispositivo.
         *
         * .build():
         * ✔ TIPO: método (fun) encadenado del builder de Room
         * Es la llamada que termina la configuración y crea la instancia real de GymDatabase.
         * Sirve para obtener el objeto de la base de datos ya listo para usarse.
         *
         * .also { database = it }:
         * ✔ TIPO: función de alcance (scope function) de Kotlin
         * Es una función que ejecuta el bloque con la instancia creada ("it") y devuelve esa misma instancia.
         * Sirve para guardar la base de datos en la propiedad "database" y devolverla como resultado del método.
         */
        return database ?: synchronized(this) {

            database ?: Room.databaseBuilder(
                context,
                GymDatabase::class.java,
                DATABASE_NAME
            ).build().also { database = it }
        }
    }

    /**
     * DATABASE_NAME
     * -------------
     * ✔ TIPO: constante privada (private const val) → String
     * Es el nombre con el que se crea el archivo de la base de datos Room.
     * Sirve para usar el mismo nombre en la construcción de la base de datos sin repetirlo.
     */
    private const val DATABASE_NAME = "clientesgestor_database"
}
