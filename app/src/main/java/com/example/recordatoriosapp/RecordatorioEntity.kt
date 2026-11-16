package com.example.recordatoriosapp

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val remoteId: Int? = null, 
    val titulo: String,
    val descripcion: String?,
    val categoria: String,
    val prioridad: String, 
    val estado: String,    
    val fechaCreacion: Long = System.currentTimeMillis(),
    val estadoSync: String = EstadoSync.PENDIENTE_CREACION.name
)
