 package com.example.recordatoriosapp

    sealed class EstadoRecordatorio(val descripcion: String) {
        object Pendiente : EstadoRecordatorio("Pendiente")
        object EnProceso : EstadoRecordatorio("En Proceso")
        object Completado : EstadoRecordatorio("Completado")
    }

    enum class Prioridad {
        BAJA, MEDIA, ALTA
    }

    data class Recordatorio(
        val id: Int,
        val titulo: String,
        val descripcion: String? = null,
        val categoria: String,
        val prioridad: Prioridad,
        val estado: EstadoRecordatorio,
        val fechaCreacion: Long = System.currentTimeMillis(),
        val estadoSync: EstadoSync = EstadoSync.SINCRONIZADO
    )
