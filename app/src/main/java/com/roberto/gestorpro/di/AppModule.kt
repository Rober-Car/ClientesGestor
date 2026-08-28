package com.roberto.gestorpro.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.roberto.gestorpro.data.dao.ClaseDao
import com.roberto.gestorpro.data.dao.ClienteDao
import com.roberto.gestorpro.data.dao.GastoDao
import com.roberto.gestorpro.data.dao.MovimientoDao
import com.roberto.gestorpro.data.dao.ReservaDao
import com.roberto.gestorpro.data.dao.SesionClaseDao
import com.roberto.gestorpro.data.dao.SolicitudDao
import com.roberto.gestorpro.data.database.ClientesDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.roberto.gestorpro.data.repository.PreferencesRepository
import com.roberto.gestorpro.data.repository.ClaseRepository
import com.roberto.gestorpro.data.repository.ClienteRepository
import com.roberto.gestorpro.data.repository.GastoRepository
import com.roberto.gestorpro.data.repository.MovimientoRepository
import com.roberto.gestorpro.data.repository.ReservaRepository
import com.roberto.gestorpro.data.repository.SolicitudRepository
import com.roberto.gestorpro.data.repository.SesionClaseRepository
import com.roberto.gestorpro.model.EstadoCliente
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
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
     * provideFirebaseAuth
     * -------------------
     * ✔ TIPO: método (fun) de Hilt con anotación @Provides y @Singleton → FirebaseAuth
     * Es la receta que proporciona la instancia única de Firebase Authentication.
     * Sirve para que el repositorio de autenticación no cree sus propias instancias
     * y toda la app comparta la misma sesión.
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    /**
     * provideFirebaseFirestore
     * ------------------------
     * ✔ TIPO: método (fun) de Hilt con anotación @Provides y @Singleton → FirebaseFirestore
     * Es la receta que proporciona la instancia única de Firestore.
     * Sirve para que los repositorios remotos compartan la misma conexión a la nube.
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    /**
     * provideFirebaseStorage
     * ----------------------
     * ✔ TIPO: método (fun) de Hilt con anotación @Provides y @Singleton → FirebaseStorage
     * Es la receta que proporciona la instancia única de Firebase Storage.
     * Sirve para que el repositorio de negocio suba el logo del gimnasio.
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

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
            "gestorpro_database"
        )

        databaseBuilder = databaseBuilder
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    insertarDatosPrueba(db)
                }
            })

        return databaseBuilder.build()
    }

    // TODO(PRODUCCION): eliminar insertarDatosPrueba() - datos ficticios solo para desarrollo
    // (20 clientes, movimientos y gastos de ejemplo insertados solo al crear la BD)
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
        val hace4anos = ahora - TimeUnit.DAYS.toMillis(365 * 4)
        val haceDosMeses = ahora - TimeUnit.DAYS.toMillis(60)
        val haceQuinceDias = ahora - TimeUnit.DAYS.toMillis(15)
        val hace19anos = ahora - TimeUnit.DAYS.toMillis(365 * 19)
        val hace22anos = ahora - TimeUnit.DAYS.toMillis(365 * 22)
        val hace24anos = ahora - TimeUnit.DAYS.toMillis(365 * 24)
        val hace26anos = ahora - TimeUnit.DAYS.toMillis(365 * 26)
        val hace33anos = ahora - TimeUnit.DAYS.toMillis(365 * 33)
        val hace38anos = ahora - TimeUnit.DAYS.toMillis(365 * 38)
        val hace42anos = ahora - TimeUnit.DAYS.toMillis(365 * 42)
        val hace47anos = ahora - TimeUnit.DAYS.toMillis(365 * 47)

        val clientesSql = listOf(
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Carlos', 'Garcia Lopez', '12345678A', '612345678', 'carlos.garcia@email.com', '', $hace30anos, $haceDosAnos, $haceDosAnos, NULL, 'ACTIVO', 1, 'Cliente fiel desde hace 2 anios')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Maria', 'Fernandez Ruiz', '23456789B', '698765432', 'maria.fernandez@email.com', '', $hace25anos, $haceUnAno, $haceUnAno, NULL, 'ACTIVO', 0, NULL)",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Pedro', 'Martinez Sanchez', '34567890C', '654321987', 'pedro.martinez@email.com', '', $hace45anos, $haceTresAnos, $haceTresAnos, NULL, 'ACTIVO', 1, 'Cliente con pagos atrasados')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Ana', 'Lopez Garcia', '45678901D', '611122233', 'ana.lopez@email.com', '', $hace50anos, $haceDosAnos, $haceDosAnos, $haceUnAno, 'BAJA', 0, 'Se dio de baja por mudanza')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Javier', 'Rodriguez Diaz', '56789012E', '699887766', 'javier.rodriguez@email.com', '', $hace20anos, $haceUnAno, $haceUnAno, NULL, 'ACTIVO', 0, NULL)",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Laura', 'Sanchez Moreno', '67890123F', '655443322', 'laura.sanchez@email.com', '', $hace35anos, $haceDosAnos, $haceDosAnos, NULL, 'ACTIVO', 1, 'Entrena 3 veces por semana')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Miguel', 'Hernandez Jimenez', '78901234G', '677889900', 'miguel.hernandez@email.com', '', $hace28anos, $haceTresAnos, $haceTresAnos, $haceSeisMeses, 'BAJA', 1, 'Lesion de rodilla')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Elena', 'Jimenez Torres', '89012345H', '633221100', 'elena.jimenez@email.com', '', $hace40anos, $haceUnAno, $haceUnAno, NULL, 'ACTIVO', 0, 'Pendiente de regularizar pagos')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Sara', 'Moreno Ruiz', '11223344B', '611234567', 'sara.moreno@email.com', '', $hace22anos, $haceTresMeses, $haceTresMeses, NULL, 'ACTIVO', 0, 'Objetivo: perder peso para el verano')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Diego', 'Torres Navarro', '22334455Y', '622345678', 'diego.torres@email.com', '', $hace19anos, $haceQuinceDias, $haceQuinceDias, NULL, 'ACTIVO', 0, NULL)",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Carmen', 'Vega Serrano', '33445566R', '633456789', 'carmen.vega@email.com', '', $hace47anos, $hace4anos, $hace4anos, $haceUnMes, 'BAJA', 1, 'Dejo el gimnasio por cambio de ciudad. Pendiente devolucion de llave')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Pablo', 'Ortega Castillo', '44556677L', '644567890', 'pablo.ortega@email.com', '', $hace33anos, $haceDosAnos, $haceDosAnos, NULL, 'ACTIVO', 1, 'Entrena crossfit, muy puntual')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Lucia', 'Ramos Hidalgo', '55667788Z', '655678901', 'lucia.ramos@email.com', '', $hace26anos, $haceSeisMeses, $haceSeisMeses, NULL, 'ACTIVO', 0, 'Clases de spinning martes y jueves')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Andres', 'Molina Cruz', '66778899D', '666789012', 'andres.molina@email.com', '', $hace42anos, $haceTresAnos, $haceTresAnos, $haceTresMeses, 'BAJA', 0, 'Baja por lesion de espalda')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Isabel', 'Castro Leon', '77889900D', '677890123', 'isabel.castro@email.com', '', $hace38anos, $haceUnAno, $haceUnAno, $haceDosMeses, 'BAJA', 0, 'Baja temporal, espera volver en otonio')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Sergio', 'Pena Marquez', '88990011K', '688901234', 'sergio.pena@email.com', '', $hace24anos, $haceDosMeses, $haceDosMeses, NULL, 'ACTIVO', 1, 'Interesado en entrenamiento personal')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Marta', 'Gil Romero', '99001122Z', '699012345', 'marta.gil@email.com', '', $hace30anos, $haceUnMes, $haceUnMes, NULL, 'ACTIVO', 0, 'Pago domiciliado')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Raul', 'Herrera Aguilar', '50123456Q', '610123456', 'raul.herrera@email.com', '', $hace26anos, $haceTresAnos, $haceTresAnos, $haceSeisMeses, 'BAJA', 1, 'Pendiente devolucion de llave')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Patricia', 'Rubio Mendez', '60234567M', '620234567', 'patricia.rubio@email.com', '', $hace33anos, $haceDosAnos, $haceDosAnos, NULL, 'ACTIVO', 0, 'Viene con su hermana, tarifa familiar')",
            "INSERT OR IGNORE INTO cliente (nombre, apellidos, dni, telefono, email, foto, fechaNacimiento, fechaRegistro, fechaAlta, fechaBaja, estado, tieneLlave, observaciones) VALUES ('Jorge', 'Delgado Fuentes', '70345678V', '630345678', 'jorge.delgado@email.com', '', $hace19anos, $haceTresMeses, $haceTresMeses, NULL, 'ACTIVO', 0, 'Estudiante, cuota reducida')"
        )

        clientesSql.forEach { db.execSQL(it) }

        val movimientosSql = listOf(
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (1, 'Cuota anual gimnasio', $haceUnAno, $ahora, 300.0, 'PAGADO', 'Pago completo anual')",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (2, 'Clases de spinning', $haceSeisMeses, $ahora, 150.0, 'PAGADO', NULL)",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (3, 'Cuota trimestral', $haceTresMeses, $haceUnMes, 90.0, 'PENDIENTE', 'No ha pagado aun')",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (3, 'Cuota trimestral anterior', $haceSeisMeses, $haceTresMeses, 90.0, 'PAGADO', NULL)",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (6, 'Pack entrenamiento personal', $haceTresMeses, $ahora, 500.0, 'PAGADO', '10 sesiones')",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (8, 'Cuota mensual', $haceUnMes, $haceSeisMeses, 40.0, 'PENDIENTE', 'Atraso en el pago')",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (5, 'Cuota mensual', $haceTresMeses, $ahora, 45.0, 'PAGADO', NULL)",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (9, 'Cuota mensual', $haceTresMeses, $haceUnMes, 40.0, 'PAGADO', NULL)",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (9, 'Cuota mensual', $haceUnMes, $ahora, 40.0, 'PENDIENTE', 'Mes actual sin pagar')",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (10, 'Cuota mensual', $haceQuinceDias, $ahora, 40.0, 'PAGADO', 'Cliente nuevo')",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (12, 'Pack entrenamiento personal', $haceDosMeses, $ahora, 250.0, 'PAGADO', '5 sesiones')",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (13, 'Clases de spinning', $haceSeisMeses, $ahora, 120.0, 'PAGADO', NULL)",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (14, 'Cuota trimestral', $haceSeisMeses, $haceTresMeses, 90.0, 'PAGADO', NULL)",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (16, 'Cuota mensual', $haceDosMeses, $haceUnMes, 40.0, 'PAGADO', NULL)",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (16, 'Entrenamiento personal sesion suelta', $haceQuinceDias, $ahora, 30.0, 'PENDIENTE', 'Pendiente de cobro')",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (17, 'Cuota anual', $haceUnMes, $ahora, 300.0, 'PAGADO', 'Pago anual completo')",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (19, 'Cuota trimestral', $haceDosMeses, $haceUnMes, 90.0, 'PENDIENTE', 'Tarifa familiar pendiente')",
            "INSERT OR IGNORE INTO movimiento (idCliente, servicio, fechaInicio, fechaFin, precio, estado, observaciones) VALUES (20, 'Cuota mensual estudiante', $haceTresMeses, $haceUnMes, 30.0, 'PAGADO', 'Precio reducido')"
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
    fun provideClaseDao(database: ClientesDatabase): ClaseDao {
        return database.claseDao()
    }

    @Provides
    fun provideClaseRepository(claseDao: ClaseDao): ClaseRepository {
        return ClaseRepository(claseDao)
    }

    @Provides
    fun provideSesionClaseDao(database: ClientesDatabase): SesionClaseDao {
        return database.sesionClaseDao()
    }

    @Provides
    fun provideSesionClaseRepository(sesionClaseDao: SesionClaseDao): SesionClaseRepository {
        return SesionClaseRepository(sesionClaseDao)
    }

    @Provides
    fun provideReservaDao(database: ClientesDatabase): ReservaDao {
        return database.reservaDao()
    }

    @Provides
    fun provideReservaRepository(reservaDao: ReservaDao): ReservaRepository {
        return ReservaRepository(reservaDao)
    }

    @Provides
    fun provideSolicitudDao(database: ClientesDatabase): SolicitudDao {
        return database.solicitudDao()
    }

    @Provides
    fun provideSolicitudRepository(solicitudDao: SolicitudDao): SolicitudRepository {
        return SolicitudRepository(solicitudDao)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context
    ): PreferencesRepository {
        return PreferencesRepository(context)
    }
}
