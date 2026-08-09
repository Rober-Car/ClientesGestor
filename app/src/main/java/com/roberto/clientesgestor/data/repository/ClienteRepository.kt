package com.roberto.clientesgestor.data.repository

import com.roberto.clientesgestor.data.dao.ClienteDao
import com.roberto.clientesgestor.data.entity.ClienteEntity
import com.roberto.clientesgestor.model.EstadoCliente
import kotlinx.coroutines.flow.Flow

/**
 * ClienteRepository.kt
 * --------------------
 * ✔ TIPO: archivo de código fuente Kotlin (repositorio de datos)
 * Es el archivo que define la capa de repositorio de clientes.
 * Sirve para separar la interfaz de usuario del acceso directo a la base de datos.
 */

/**
 * ClienteRepository
 * -----------------
 * ✔ TIPO: clase (repositorio de datos)
 * Es la capa que separa la interfaz de usuario del acceso directo a la base de datos.
 * Sirve para centralizar las operaciones con clientes usando ClienteDao internamente.
 */
class ClienteRepository(
    /**
     * clienteDao
     * ----------
     * ✔ TIPO: parámetro (param) → ClienteDao
     * Es el DAO de clientes que recibirá el repositorio.
     * Sirve para que el repositorio acceda a la base de datos a través de las operaciones del DAO.
     */
    private val clienteDao: ClienteDao
) {

    /**
     * insertarClienteRepo
     * -------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que inserta un cliente en la base de datos a través del DAO.
     * Sirve para guardar un nuevo ClienteEntity desde la capa de repositorio.
     */
    suspend fun insertarClienteRepo(cliente: ClienteEntity) {
        clienteDao.insertarClienteDao(cliente)
    }

    /**
     * actualizarClienteRepo
     * ---------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que actualiza los datos de un cliente ya existente.
     * Sirve para guardar los cambios de un ClienteEntity a través del DAO.
     */
    suspend fun actualizarClienteRepo(cliente: ClienteEntity) {
        clienteDao.actualizarClienteDao(cliente)
    }

    /**
     * eliminarClienteRepo
     * -------------------
     * ✔ TIPO: método (fun) suspend de Kotlin
     * Es la operación que elimina un cliente de la base de datos.
     * Sirve para borrar un ClienteEntity a través del DAO.
     */
    suspend fun eliminarClienteRepo(cliente: ClienteEntity) {
        clienteDao.eliminarClienteDao(cliente)
    }

    /**
     * obtenerClientesRepo
     * -------------------
     * ✔ TIPO: método (fun) de Kotlin → Flow<List<ClienteEntity>>
     * Es la operación que recupera todos los clientes guardados en la base de datos.
     * Sirve para obtener la lista completa de clientes de forma reactiva desde la capa de repositorio.
     */
    fun obtenerClientesRepo(): Flow<List<ClienteEntity>> {
        return clienteDao.obtenerClientesDao()
    }

    /**
     * obtenerClientePorDniRepo
     * ------------------------
     * ✔ TIPO: método (fun) suspend de Kotlin → ClienteEntity?
     * Es la operación que recupera un cliente buscándolo por su DNI.
     * Sirve para obtener un ClienteEntity concreto (o null si no existe)
     * a través del DAO desde la capa de repositorio.
     */
    suspend fun obtenerClientePorDniRepo(dni: String): ClienteEntity? {
        return clienteDao.obtenerClientePorDniDao(dni)
    }

    /**
     * obtenerClientePorEstadoRepo
     * ---------------------------
     * ✔ TIPO: método (fun) de Kotlin → Flow<List<ClienteEntity>>
     * Es la operación que recupera los clientes filtrados por su estado.
     * Sirve para obtener la lista de ClienteEntity con un EstadoCliente concreto
     * de forma reactiva a través del DAO desde la capa de repositorio.
     */
    fun obtenerClientePorEstadoRepo(estado: EstadoCliente): Flow<List<ClienteEntity>> {
        return clienteDao.obtenerClientesPorEstadoDao(estado)
    }
}
