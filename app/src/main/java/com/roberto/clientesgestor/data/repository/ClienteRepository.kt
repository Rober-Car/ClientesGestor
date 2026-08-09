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
    private val clienteDao: ClienteDao
) {

    suspend fun insertarClienteRepo(cliente: ClienteEntity) {
        clienteDao.insertarClienteDao(cliente)
    }

    suspend fun actualizarClienteRepo(cliente: ClienteEntity) {
        clienteDao.actualizarClienteDao(cliente)
    }

    suspend fun eliminarClienteRepo(cliente: ClienteEntity) {
        clienteDao.eliminarClienteDao(cliente)
    }

    fun obtenerClientesRepo(): Flow<List<ClienteEntity>> {
        return clienteDao.obtenerClientesDao()
    }

    suspend fun obtenerClientePorDniRepo(dni: String): ClienteEntity? {
        return clienteDao.obtenerClientePorDniDao(dni)
    }

    fun obtenerClientePorEstadoRepo(estado: EstadoCliente): Flow<List<ClienteEntity>> {
        return clienteDao.obtenerClientesPorEstadoDao(estado)
    }
}
