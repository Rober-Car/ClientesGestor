package com.roberto.clientesgestor.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.roberto.clientesgestor.data.converter.EstadoClienteConverter
import com.roberto.clientesgestor.data.dao.ClienteDao
import com.roberto.clientesgestor.data.entity.ClienteEntity



/**
 * GymDatabase
 * -----------
 * ✔ TIPO: clase abstracta (RoomDatabase con anotación @Database)
 * Es la base de datos Room del gimnasio que gestiona las entidades persistentes.
 * Sirve para que la aplicación pueda guardar y recuperar datos de forma local,
 * y para que Room genere el código de acceso a las tablas a partir de las anotaciones.
 */

/**
 * @Database(entities = [...], version = ...)
 * ------------------------------------------
 * ✔ TIPO: anotación (androidx.room.Database)
 * Es la anotación que declara que esta clase es la base de datos Room de la app.
 * Sirve para que Room conozca las entidades (tablas) y la versión del esquema:
 * - entities: lista de clases @Entity que forman las tablas de la base de datos.
 * - version: número de versión del esquema; se sube cuando cambian las tablas para migrar los datos.
 */
@Database(
    entities = [ClienteEntity::class],
    version = 1
)

/**
 * @TypeConverters(EstadoClienteConverter::class)
 * ---------------------------------------------
 * ✔ TIPO: anotación (androidx.room.TypeConverters)
 * Es la anotación que registra los conversores de tipos de toda la base de datos.
 * Sirve para que Room sepa cómo guardar tipos que no soporta por defecto
 * (en este caso el enum EstadoCliente) convirtiéndolos a String y viceversa.
 */
@TypeConverters(EstadoClienteConverter::class)
abstract class GymDatabase : RoomDatabase() {

    /**
     * clienteDao
     * ----------
     * ✔ TIPO: método abstracto (fun) → ClienteDao
     * Es el método que devuelve el DAO de clientes de la base de datos.
     * Sirve para acceder a las operaciones de ClienteDao (insertar y obtener clientes);
     * Room genera automáticamente la implementación de este método.
     */
    abstract fun clienteDao(): ClienteDao

}