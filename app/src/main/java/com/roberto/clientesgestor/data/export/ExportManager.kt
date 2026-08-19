package com.roberto.clientesgestor.data.export

import android.content.Context
import android.net.Uri
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.roberto.clientesgestor.data.dao.ClienteDao
import com.roberto.clientesgestor.data.dao.GastoDao
import com.roberto.clientesgestor.data.dao.MovimientoDao
import kotlinx.coroutines.runBlocking

object ExportManager {

    suspend fun exportarDatos(
        context: Context,
        uri: Uri,
        clienteDao: ClienteDao,
        movimientoDao: MovimientoDao,
        gastoDao: GastoDao
    ): Boolean {
        return try {
            val clientes = clienteDao.obtenerTodosLosClientesSync()
            val movimientos = movimientoDao.obtenerTodosLosMovimientosSync()
            val gastos = gastoDao.obtenerTodosLosGastosSync()

            val datos = mapOf(
                "clientes" to clientes,
                "movimientos" to movimientos,
                "gastos" to gastos
            )

            val gson = GsonBuilder().setPrettyPrinting().create()
            val json = gson.toJson(datos)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importarDatos(
        context: Context,
        uri: Uri,
        clienteDao: ClienteDao,
        movimientoDao: MovimientoDao,
        gastoDao: GastoDao
    ): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val reader = inputStream.bufferedReader()
            val json = reader.readText()
            reader.close()

            val jsonObject = JsonParser.parseString(json).asJsonObject
            val gson = GsonBuilder().create()

            if (jsonObject.has("clientes")) {
                val clientesArray = jsonObject.getAsJsonArray("clientes")
                val clientes = gson.fromJson(clientesArray, Array<com.roberto.clientesgestor.data.entity.ClienteEntity>::class.java)
                clientes.forEach { cliente ->
                    clienteDao.insertarClienteDao(cliente)
                }
            }

            if (jsonObject.has("movimientos")) {
                val movimientosArray = jsonObject.getAsJsonArray("movimientos")
                val movimientos = gson.fromJson(movimientosArray, Array<com.roberto.clientesgestor.data.entity.MovimientoEntity>::class.java)
                movimientos.forEach { movimiento ->
                    movimientoDao.insertarMovimiento(movimiento)
                }
            }

            if (jsonObject.has("gastos")) {
                val gastosArray = jsonObject.getAsJsonArray("gastos")
                val gastos = gson.fromJson(gastosArray, Array<com.roberto.clientesgestor.data.entity.GastoEntity>::class.java)
                gastos.forEach { gasto ->
                    gastoDao.insertarGasto(gasto)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
