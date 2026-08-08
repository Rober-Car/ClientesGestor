package com.roberto.clientesgestor.di
import android.content.Context
import androidx.room.Room
import com.roberto.clientesgestor.data.dao.ClienteDao
import com.roberto.clientesgestor.data.database.ClientesDatabase
import com.roberto.clientesgestor.data.repository.ClienteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

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
    fun provideDatabase(context: Context): ClientesDatabase {

        /**
         * Room.databaseBuilder(context, ClientesDatabase::class.java, "clientesgestor_database"):
         * ✔ TIPO: método (fun) de Room
         * Es el constructor de la base de datos Room, que recibe el contexto, la clase de la base y su nombre.
         * Sirve para configurar cómo se creará el archivo de la base de datos en el dispositivo.
         *
         * .build():
         * ✔ TIPO: método (fun) encadenado del builder de Room
         * Es la llamada que termina la configuración y crea la instancia real de ClientesDatabase.
         * Sirve para obtener el objeto de la base de datos ya listo para usarse.
         */
        return Room.databaseBuilder(
            context,
            ClientesDatabase::class.java,
            "clientesgestor_database"
        ).build()
    }

    /**
     * provideClienteDao
     * -----------------
     * ✔ TIPO: método (fun) de Hilt con anotación @Provides → ClienteDao
     * Es la función que proporciona el DAO de clientes a partir de la base de datos.
     * Sirve para que Hilt inyecte ClienteDao en las clases que necesiten acceder a la tabla de clientes.
     */
    @Provides
    fun provideClienteDao(database: ClientesDatabase): ClienteDao {

        /**
         * database.clienteDao():
         * ✔ TIPO: método (fun) de Room
         * Es la llamada que devuelve la instancia de ClienteDao generada por Room.
         * Sirve para obtener el DAO de clientes desde la base de datos.
         */
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
         * Es el DAO de clientes que recibirá el repositorio.
         * Sirve para que Hilt lo inyecte automáticamente al construir ClienteRepository.
         */
        clienteDao: ClienteDao
    ): ClienteRepository {

        /**
         * ClienteRepository(clienteDao):
         * ✔ TIPO: constructor de clase
         * Es la llamada que crea el repositorio pasándole el DAO de clientes.
         * Sirve para devolver una instancia de ClienteRepository lista para usarse.
         */
        return ClienteRepository(clienteDao)
    }
}