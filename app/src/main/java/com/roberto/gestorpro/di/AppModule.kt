package com.roberto.gestorpro.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.roberto.gestorpro.data.dao.ClaseDao
import com.roberto.gestorpro.data.dao.ClienteDao
import com.roberto.gestorpro.data.dao.GastoDao
import com.roberto.gestorpro.data.dao.MovimientoDao
import com.roberto.gestorpro.data.dao.ReservaDao
import com.roberto.gestorpro.data.dao.ServicioDao
import com.roberto.gestorpro.data.dao.SesionClaseDao
import com.roberto.gestorpro.data.dao.SesionDao
import com.roberto.gestorpro.data.dao.SolicitudDao
import com.roberto.gestorpro.data.database.ClientesDatabase
import com.roberto.gestorpro.data.firebase.ClienteRemotoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.roberto.gestorpro.data.repository.PreferencesRepository
import com.roberto.gestorpro.data.repository.ClaseRepository
import com.roberto.gestorpro.data.repository.ClienteRepository
import com.roberto.gestorpro.data.repository.GastoRepository
import com.roberto.gestorpro.data.repository.MovimientoRepository
import com.roberto.gestorpro.data.repository.ReservaRepository
import com.roberto.gestorpro.data.repository.ServicioRepository
import com.roberto.gestorpro.data.repository.SesionClaseRepository
import com.roberto.gestorpro.data.repository.SesionRepository
import com.roberto.gestorpro.data.repository.SolicitudRepository
import com.roberto.gestorpro.model.EstadoCliente
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
     * MIGRACION_11_12
     * ---------------
     * Añade la columna nullable horaDesdeReserva a la tabla "sesion".
     * No toca el resto de tablas ni destruye datos locales.
     */
    private val MIGRACION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE sesion ADD COLUMN horaDesdeReserva TEXT")
        }
    }

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
            .addMigrations(MIGRACION_11_12)
            .fallbackToDestructiveMigration()

        return databaseBuilder.build()
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
        movimientoDao: MovimientoDao,
        clienteRemotoRepository: ClienteRemotoRepository
    ): MovimientoRepository {
        return MovimientoRepository(movimientoDao, clienteRemotoRepository)
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
    fun provideReservaRepository(
        reservaDao: ReservaDao,
        sesionDao: SesionDao,
        servicioDao: ServicioDao,
        database: ClientesDatabase
    ): ReservaRepository {
        return ReservaRepository(reservaDao, sesionDao, servicioDao, database)
    }

    @Provides
    fun provideServicioDao(database: ClientesDatabase): ServicioDao {
        return database.servicioDao()
    }

    @Provides
    fun provideServicioRepository(servicioDao: ServicioDao): ServicioRepository {
        return ServicioRepository(servicioDao)
    }

    @Provides
    fun provideSesionDao(database: ClientesDatabase): SesionDao {
        return database.sesionDao()
    }

    @Provides
    fun provideSesionRepository(sesionDao: SesionDao): SesionRepository {
        return SesionRepository(sesionDao)
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
