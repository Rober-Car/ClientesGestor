package com.roberto.clientesgestor.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.roberto.clientesgestor.data.dao.ClienteDao
import com.roberto.clientesgestor.data.dao.GastoDao
import com.roberto.clientesgestor.data.dao.MovimientoDao
import com.roberto.clientesgestor.data.database.ClientesDatabase
import com.roberto.clientesgestor.data.preferences.PreferencesRepository
import com.roberto.clientesgestor.data.repository.ClienteRepository
import com.roberto.clientesgestor.data.repository.GastoRepository
import com.roberto.clientesgestor.data.repository.MovimientoRepository
import com.roberto.clientesgestor.model.EstadoCliente
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import java.util.concurrent.TimeUnit

/**
 * AppModule.kt
 * ------------
 * ✔ TIPO: archivo de código fuente Kotlin (inyección de dependencias)
 * Es el archivo que define el módulo de Hilt de la aplicación.
 * Sirve para que Hilt sepa cómo construir y proporcionar la base de datos, el DAO y el repositorio.
 */

/**
 * @Module
 * -------
 * ✔ TIPO: anotación (dagger.Module)
 * Es la anotación que marca este objeto como módulo de inyección de dependencias de Hilt.
 * Sirve para indicar a Hilt qué objetos puede proporcionar a la aplicación.
 */

/**
 * @InstallIn(SingletonComponent::class)
 * -------------------------------------
 * ✔ TIPO: anotación (dagger.hilt.InstallIn)
 * Es la anotación que indica en qué componente de Hilt se instala este módulo.
 * Sirve para que las dependencias proporcionadas estén disponibles en toda la aplicación
 * (alcance singleton, es decir, una única instancia mientras viva el proceso).
 */

/**
 * AppModule
 * ---------
 * ✔ TIPO: object (objeto singleton de Kotlin)
 * Es el objeto que agrupa los métodos que proporcionan las dependencias de datos.
 * Sirve para construir la base de datos, el DAO y el repositorio e inyectarlos donde se necesiten.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * provideDatabase
     * ---------------
     * ✔ TIPO: método (fun) de Hilt con anotación @Provides y @Singleton → ClientesDatabase
     * Es la función que proporciona la instancia de la base de datos Room de la aplicación.
     * Sirve para que Hilt cree una única ClientesDatabase y la inyecte en las clases que la pidan.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ClientesDatabase {

        var databaseBuilder = Room.databaseBuilder(
            context,
            ClientesDatabase::class.java,
            "clientesgestor_database"
        )

        databaseBuilder = databaseBuilder
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    insertarDatosPrueba(db)
                }

                override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                    super.onDestructiveMigration(db)
                    insertarDatosPrueba(db)
                }
            })

        return databaseBuilder.build()
    }

    private fun insertarDatosPrueba(db: SupportSQLiteDatabase) {
        val ahora = System.currentTimeMillis()
        val hace30anos = ahora - TimeUnit.DAYS.toMillis(365 * 30)
        val hace25anos = ahora - TimeUnit.DAYS.toMillis(365 * 25)
        val hace20anos = ahora - TimeUnit.DAYS.toMillis(365 * 20)
        val hace45anos = ahora - TimeUnit.DAYS.toMillis(365 * 45)
        val hace50anos = ahora - TimeUnit.DAYS.toMillis(365 * 50)
        val hace35anos = ahora - TimeUnit.DAYS.toMillis(365 * 35)
        val hace28anos = ahora - TimeUnit.DAYS.toMillis(365 * 28)
        val hace40anos = ahora - TimeUnit.DAYS.toMillis(365 * 40)
        val unAno = TimeUnit.DAYS.toMillis(365)
        val dosAnos = TimeUnit.DAYS.toMillis(365 * 2)
        val tresAnos = TimeUnit.DAYS.toMillis(365 * 3)
        val seisMeses = TimeUnit.DAYS.toMillis(180)
        val tresMeses = TimeUnit.DAYS.toMillis(90)
        val unMes = TimeUnit.DAYS.toMillis(30)
        val haceUnAno = ahora - unAno
        val haceDosAnos = ahora - dosAnos
        val haceTresAnos = ahora - tresAnos
        val haceSeisMeses = ahora - seisMeses
        val haceTresMeses = ahora - tresMeses
        val haceUnMes = ahora - unMes

        val clientesSql = listOf(
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Carlos', 'Garcia Lopez', '12345678A', '612345678', 'carlos.garcia@email.com', '', $hace30anos, $haceDosAnos, $haceDosAnos, NULL, 'ACTIVO', 1, 'Cliente fiel desde hace 2 anios')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Maria', 'Fernandez Ruiz', '23456789B', '698765432', 'maria.fernandez@email.com', '', $hace25anos, $haceUnAno, $haceUnAno, NULL, 'ACTIVO', 0, NULL)",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Pedro', 'Martinez Sanchez', '34567890C', '654321987', 'pedro.martinez@email.com', '', $hace45anos, $haceTresAnos, $haceTresAnos, NULL, 'ACTIVO', 1, 'Cliente con pagos atrasados')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Ana', 'Lopez Garcia', '45678901D', '611122233', 'ana.lopez@email.com', '', $hace50anos, $haceDosAnos, $haceDosAnos, $haceUnAno, 'BAJA', 0, 'Se dio de baja por mudanza')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Javier', 'Rodriguez Diaz', '56789012E', '699887766', 'javier.rodriguez@email.com', '', $hace20anos, $haceUnAno, $haceUnAno, NULL, 'ACTIVO', 0, NULL)",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Laura', 'Sanchez Moreno', '67890123F', '655443322', 'laura.sanchez@email.com', '', $hace35anos, $haceDosAnos, $haceDosAnos, NULL, 'ACTIVO', 1, 'Entrena 3 veces por semana')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Miguel', 'Hernandez Jimenez', '78901234G', '677889900', 'miguel.hernandez@email.com', '', $hace28anos, $haceTresAnos, $haceTresAnos, $haceSeisMeses, 'BAJA', 1, 'Lesion de rodilla')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Elena', 'Jimenez Torres', '89012345H', '633221100', 'elena.jimenez@email.com', '', $hace40anos, $haceUnAno, $haceUnAno, NULL, 'ACTIVO', 0, 'Pendiente de regularizar pagos')"
        )

        clientesSql.forEach { db.execSQL(it) }

        val movimientosSql = listOf(
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (1, 'Cuota anual gimnasio', $haceUnAno, $ahora, 300.0, 'PAGADO', 'Pago completo anual')",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (2, 'Clases de spinning', $haceSeisMeses, $ahora, 150.0, 'PAGADO', NULL)",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (3, 'Cuota trimestral', $haceTresMeses, $haceUnMes, 90.0, 'PENDIENTE', 'No ha pagado aun')",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (3, 'Cuota trimestral anterior', $haceSeisMeses, $haceTresMeses, 90.0, 'PAGADO', NULL)",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (6, 'Pack entrenamiento personal', $haceTresMeses, $ahora, 500.0, 'PAGADO', '10 sesiones')",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (8, 'Cuota mensual', $haceUnMes, $haceSeisMeses, 40.0, 'PENDIENTE', 'Atraso en el pago')",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (5, 'Cuota mensual', $haceTresMeses, $ahora, 45.0, 'PAGADO', NULL)"
        )

        movimientosSql.forEach { db.execSQL(it) }

        val gastosSql = listOf(
            "INSERT OR IGNORE INTO gasto (concepto, importe, fecha, observaciones) VALUES ('Alquiler lokal', 800.0, $haceUnMes, 'Pago mensual del local')",
            "INSERT OR IGNORE INTO gasto (concepto, importe, fecha, observaciones) VALUES ('Mantenimiento aparatos', 150.0, $haceUnMes, 'Revision trimestral')",
            "INSERT OR IGNORE INTO gasto (concepto, importe, fecha, observaciones) VALUES ('Suministros luz', 120.0, $haceUnMes, NULL)",
            "INSERT OR IGNORE INTO gasto (concepto, importe, fecha, observaciones) VALUES ('Material deportivo', 200.0, $haceSeisMeses, 'Compra de pesas y bandas')"
        )

        gastosSql.forEach { db.execSQL(it) }
    }

    /**
     * provideClienteDao
     * -----------------
     * ✔ TIPO: método (fun) de Hilt con anotación @Provides → ClienteDao
     * Es la función que proporciona el DAO de clientes a partir de la base de datos.
     * Sirve para que Hilt inyecte ClienteDao en las clases que necesiten acceder a la tabla de clientes.
     */
    @Provides
    fun provideClienteDao(
        /**
         * database
         * --------
         * ✔ TIPO: parámetro (param) → ClientesDatabase
         * Es la base de datos Room de la aplicación.
         * Sirve para obtener el DAO de clientes a partir de ella.
         */
        database: ClientesDatabase
    ): ClienteDao {
        return database.clienteDao()
    }

    /**
     * provideClienteRepository
     * ------------------------
     * ✔ TIPO: método (fun) de Hilt con anotación @Provides → ClienteRepository
     * Es la función que proporciona el repositorio de clientes con su DAO.
     * Sirve para que Hilt inyecte ClienteRepository en las clases que necesiten operar con clientes.
     */
    @Provides
    fun provideClienteRepository(
        /**
         * clienteDao
         * ----------
         * ✔ TIPO: parámetro (param) → ClienteDao
         * Es el DAO de clientes de la base de datos.
         * Sirve para construir el repositorio de clientes a partir de él.
         */
        clienteDao: ClienteDao
    ): ClienteRepository {
        return ClienteRepository(clienteDao)
    }

    /**
     * provideMovimientoDao
     * --------------------
     * ✔ TIPO: método (fun) de Hilt con anotación @Provides → MovimientoDao
     * Es la función que proporciona el DAO de movimientos a partir de la base de datos.
     * Sirve para que Hilt inyecte MovimientoDao en las clases que necesiten acceder a la tabla de movimientos.
     */
    @Provides
    fun provideMovimientoDao(
        /**
         * database
         * --------
         * ✔ TIPO: parámetro (param) → ClientesDatabase
         * Es la base de datos Room de la aplicación.
         * Sirve para obtener el DAO de movimientos a partir de ella.
         */
        database: ClientesDatabase
    ): MovimientoDao {
        return database.movimientoDao()
    }

    /**
     * provideMovimientoRepository
     * ---------------------------
     * ✔ TIPO: método (fun) de Hilt con anotación @Provides → MovimientoRepository
     * Es la función que proporciona el repositorio de movimientos con su DAO.
     * Sirve para que Hilt inyecte MovimientoRepository en las clases que necesiten operar con movimientos.
     */
    @Provides
    fun provideMovimientoRepository(
        /**
         * movimientoDao
         * -------------
         * ✔ TIPO: parámetro (param) → MovimientoDao
         * Es el DAO de movimientos de la base de datos.
         * Sirve para construir el repositorio de movimientos a partir de él.
         */
        movimientoDao: MovimientoDao
    ): MovimientoRepository {
        return MovimientoRepository(movimientoDao)
    }

    @Provides
    fun provideGastoDao(database: ClientesDatabase): GastoDao {
        return database.gastoDao()
    }

    @Provides
    fun provideGastoRepository(gastoDao: GastoDao): GastoRepository {
        return GastoRepository(gastoDao)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PreferencesRepository {
        return PreferencesRepository(context)
    }
}
