package com.roberto.gestorpro.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.roberto.gestorpro.data.converter.EstadoClienteConverter
import com.roberto.gestorpro.data.converter.EstadoMovimientoConverter
import com.roberto.gestorpro.data.dao.ClienteDao
import com.roberto.gestorpro.data.dao.GastoDao
import com.roberto.gestorpro.data.dao.MovimientoDao
import com.roberto.gestorpro.data.entity.ClienteEntity
import com.roberto.gestorpro.data.entity.GastoEntity
import com.roberto.gestorpro.data.entity.MovimientoEntity

/**
 * ClientesDatabase.kt
 * -------------------
 * ✔ TIPO: archivo de código fuente Kotlin (base de datos)
 * Es el archivo que define la base de datos Room de la aplicación.
 * Sirve para que la app guarde y recupere los datos de clientes de forma local y persistente.
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
    entities = [ClienteEntity::class, MovimientoEntity::class, GastoEntity::class],
    version = 4
)

/**
 * @TypeConverters(EstadoClienteConverter::class)
 * ---------------------------------------------
 * ✔ TIPO: anotación (androidx.room.TypeConverters)
 * Es la anotación que registra los conversores de tipos de toda la base de datos.
 * Sirve para que Room sepa cómo guardar tipos que no soporta por defecto
 * (en este caso los enums EstadoCliente y EstadoMovimiento) convirtiéndolos a String y viceversa.
 */
@TypeConverters(EstadoClienteConverter::class, EstadoMovimientoConverter::class)

/**
 * ClientesDatabase
 * ----------------
 * ✔ TIPO: clase abstracta (RoomDatabase con anotación @Database)
 * Es la base de datos Room de la app que gestiona las entidades persistentes.
 * Sirve para que la aplicación pueda guardar y recuperar datos de forma local,
 * y para que Room genere el código de acceso a las tablas a partir de las anotaciones.
 */
abstract class ClientesDatabase : RoomDatabase() {

    /**
     * clienteDao
     * ----------
     * ✔ TIPO: método abstracto (fun) → ClienteDao
     * Es el método que devuelve el DAO de clientes de la base de datos.
     * Sirve para acceder a las operaciones de ClienteDao (insertar y obtener clientes);
     * Room genera automáticamente la implementación de este método.
     */
    abstract fun clienteDao(): ClienteDao

    /**
     * movimientoDao
     * -------------
     * ✔ TIPO: método abstracto (fun) → MovimientoDao
     * Es el método que devuelve el DAO de movimientos de la base de datos.
     * Sirve para acceder a las operaciones de MovimientoDao (insertar, actualizar, eliminar y consultar movimientos);
     * Room genera automáticamente la implementación de este método.
     */
    abstract fun movimientoDao(): MovimientoDao

    abstract fun gastoDao(): GastoDao

}
