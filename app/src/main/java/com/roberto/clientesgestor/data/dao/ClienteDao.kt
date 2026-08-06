package com.roberto.clientesgestor.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.roberto.clientesgestor.data.entity.ClienteEntity

/**
 * ClienteDao.kt
 * -------------
 * ✔ TIPO: archivo de código fuente Kotlin (acceso a datos)
 * Es el archivo que define las operaciones de lectura y escritura sobre la tabla de clientes.
 * Sirve para que la aplicación inserte y consulte clientes en la base de datos Room.
 */

/**
 * @Dao
 * ----
 * ✔ TIPO: anotación (androidx.room.Dao)
 * Es la anotación que marca esta interfaz como objeto de acceso a datos (DAO).
 * Sirve para que Room genere automáticamente la implementación de los métodos de esta interfaz.
 */

/**
 * ClienteDao
 * ----------
 * ✔ TIPO: interfaz (DAO de Room)
 * Es la interfaz que expone las operaciones de la base de datos sobre los clientes.
 * Sirve para insertar y obtener clientes sin escribir consultas SQL a mano.
 */
@Dao
interface ClienteDao {

    /**
     * insertarCliente
     * ---------------
     * ✔ TIPO: método (fun) de Room con anotación @Insert
     * Es la operación que inserta un cliente en la tabla de la base de datos.
     * Sirve para guardar un nuevo ClienteEntity (o actualizarlo) cuando se le pasa como parámetro.
     */
    @Insert
    fun insertarCliente(cliente: ClienteEntity)

    /**
     * actualizarCliente
     * -----------------
     * ✔ TIPO: método (fun) de Room con anotación @Update
     * Es la operación que actualiza los datos de un cliente ya existente en la tabla.
     * Sirve para guardar los cambios de un ClienteEntity en la base de datos
     * buscando el cliente por su clave primaria.
     */
    @Update
    fun actualizarCliente(cliente: ClienteEntity)

    /**
     * eliminarCliente
     * ---------------
     * ✔ TIPO: método (fun) de Room con anotación @Delete
     * Es la operación que elimina un cliente de la tabla de la base de datos.
     * Sirve para borrar un ClienteEntity de la base de datos
     * buscando el cliente por su clave primaria.
     */
    @Delete
    fun eliminarCliente(cliente: ClienteEntity)

    /**
     * obtenerClientes
     * ---------------
     * ✔ TIPO: método (fun) de Room con anotación @Query → List<ClienteEntity>
     * Es la operación que consulta todos los clientes guardados en la tabla.
     * Sirve para recuperar la lista completa de clientes con la consulta SQL
     * "SELECT * FROM ClienteEntity".
     */
    @Query("SELECT * FROM ClienteEntity")
    fun obtenerClientes(): List<ClienteEntity>

}