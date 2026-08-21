package com.roberto.gestorpro.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.roberto.gestorpro.data.converter.EstadoClienteConverter
import com.roberto.gestorpro.data.converter.EstadoMovimientoConverter
import com.roberto.gestorpro.data.converter.EstadoSolicitudConverter
import com.roberto.gestorpro.data.converter.StringListConverter
import com.roberto.gestorpro.data.converter.TipoSolicitudConverter
import com.roberto.gestorpro.data.dao.ClaseDao
import com.roberto.gestorpro.data.dao.ClienteDao
import com.roberto.gestorpro.data.dao.GastoDao
import com.roberto.gestorpro.data.dao.MovimientoDao
import com.roberto.gestorpro.data.dao.ReservaDao
import com.roberto.gestorpro.data.dao.SesionClaseDao
import com.roberto.gestorpro.data.dao.SolicitudDao
import com.roberto.gestorpro.data.entity.ClaseEntity
import com.roberto.gestorpro.data.entity.ClienteEntity
import com.roberto.gestorpro.data.entity.GastoEntity
import com.roberto.gestorpro.data.entity.MovimientoEntity
import com.roberto.gestorpro.data.entity.ReservaEntity
import com.roberto.gestorpro.data.entity.SesionClaseEntity
import com.roberto.gestorpro.data.entity.SolicitudEntity

// TODO(PRODUCCION): version subida a 10 solo para forzar recreacion de BD en desarrollo
// (el backup de Google restauraba una BD vieja y no se ejecutaban los datos de prueba).
// Antes de publicar: sustituir fallbackToDestructiveMigration por migraciones reales.
@Database(
    entities = [
        ClienteEntity::class,
        MovimientoEntity::class,
        GastoEntity::class,
        ClaseEntity::class,
        SesionClaseEntity::class,
        ReservaEntity::class,
        SolicitudEntity::class
    ],
    version = 10
)
@TypeConverters(
    EstadoClienteConverter::class,
    EstadoMovimientoConverter::class,
    TipoSolicitudConverter::class,
    EstadoSolicitudConverter::class,
    StringListConverter::class
)
abstract class ClientesDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun gastoDao(): GastoDao
    abstract fun claseDao(): ClaseDao
    abstract fun sesionClaseDao(): SesionClaseDao
    abstract fun reservaDao(): ReservaDao
    abstract fun solicitudDao(): SolicitudDao
}
