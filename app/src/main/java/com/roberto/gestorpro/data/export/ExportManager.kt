package com.roberto.gestorpro.data.export

import android.content.Context
import android.net.Uri
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.roberto.gestorpro.data.dao.ClienteDao
import com.roberto.gestorpro.data.dao.GastoDao
import com.roberto.gestorpro.data.dao.MovimientoDao
import com.roberto.gestorpro.data.entity.ClienteEntity
import com.roberto.gestorpro.data.entity.GastoEntity
import com.roberto.gestorpro.data.entity.MovimientoEntity

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
            val json = leerJson(context, uri) ?: return false
            val jsonObject = JsonParser.parseString(json).asJsonObject
            val gson = GsonBuilder().create()

            if (jsonObject.has("clientes")) {
                val clientes = gson.fromJson(
                    jsonObject.getAsJsonArray("clientes"),
                    Array<ClienteEntity>::class.java
                )
                clientes.forEach { clienteDao.insertarClienteDao(it) }
            }

            if (jsonObject.has("movimientos")) {
                val movimientos = gson.fromJson(
                    jsonObject.getAsJsonArray("movimientos"),
                    Array<MovimientoEntity>::class.java
                )
                movimientos.forEach { movimiento ->
                    val clienteExiste = clienteDao.obtenerClientePorIdDao(movimiento.idCliente) != null
                    if (clienteExiste) {
                        movimientoDao.insertarMovimiento(movimiento)
                    }
                }
            }

            if (jsonObject.has("gastos")) {
                val gastos = gson.fromJson(
                    jsonObject.getAsJsonArray("gastos"),
                    Array<GastoEntity>::class.java
                )
                gastos.forEach { gastoDao.insertarGasto(it) }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun restaurarDatos(
        context: Context,
        uri: Uri,
        clienteDao: ClienteDao,
        movimientoDao: MovimientoDao,
        gastoDao: GastoDao
    ): Boolean {
        return try {
            val json = leerJson(context, uri) ?: return false
            val jsonObject = JsonParser.parseString(json).asJsonObject
            val gson = GsonBuilder().create()

            movimientoDao.borrarTodosLosMovimientos()
            gastoDao.borrarTodosLosGastos()
            clienteDao.borrarTodosLosClientes()

            if (jsonObject.has("clientes")) {
                val clientes = gson.fromJson(
                    jsonObject.getAsJsonArray("clientes"),
                    Array<ClienteEntity>::class.java
                )
                clientes.forEach { clienteDao.insertarClienteDao(it) }
            }

            if (jsonObject.has("movimientos")) {
                val movimientos = gson.fromJson(
                    jsonObject.getAsJsonArray("movimientos"),
                    Array<MovimientoEntity>::class.java
                )
                movimientos.forEach { movimientoDao.insertarMovimiento(it) }
            }

            if (jsonObject.has("gastos")) {
                val gastos = gson.fromJson(
                    jsonObject.getAsJsonArray("gastos"),
                    Array<GastoEntity>::class.java
                )
                gastos.forEach { gastoDao.insertarGasto(it) }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun leerJson(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val reader = inputStream.bufferedReader()
            val json = reader.readText()
            reader.close()
            json
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
