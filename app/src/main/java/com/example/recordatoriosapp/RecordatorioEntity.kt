package com.example.recordatoriosapp

import androidx.room.Entity
import androidx.room.PrimaryKey

// Estado de sincronización de cada registro local (Consigna 3)
enum class EstadoSync {
    SINCRONIZADO,
    PENDIENTE_CREACION,
    PENDIENTE_ACTUALIZACION,
    PENDIENTE_ELIMINACION,
    ERROR_SYNC
}

@Entity(tableName = "recordatorios")
data class RecordatorioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteId: Int? = null, // ID asignado por el servidor remoto cuando se sincroniza
    val titulo: String,
    val descripcion: String?,
    val categoria: String,
    val prioridad: String, // Guardamos el .name de Prioridad (ALTA, MEDIA, BAJA)
    val estado: String,    // Guardamos el .name de EstadoRecordatorio
    val fechaCreacion: Long = System.currentTimeMillis(),
    val estadoSync: String = EstadoSync.PENDIENTE_CREACION.name
)