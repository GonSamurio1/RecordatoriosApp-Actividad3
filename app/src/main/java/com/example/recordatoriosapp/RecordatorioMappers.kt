package com.example.recordatoriosapp

fun RecordatorioEntity.toDomain(): Recordatorio {
    val estadoDominio = when (this.estado) {
        "Completado" -> EstadoRecordatorio.Completado
        "EnProceso" -> EstadoRecordatorio.EnProceso
        else -> EstadoRecordatorio.Pendiente
    }

    val prioridadDominio = try {
        Prioridad.valueOf(this.prioridad)
    } catch (e: IllegalArgumentException) {
        Prioridad.MEDIA
    }

    val syncDominio = try {
        EstadoSync.valueOf(this.estadoSync)
    } catch (e: IllegalArgumentException) {
        EstadoSync.SINCRONIZADO
    }

    return Recordatorio(
        id = this.id,
        titulo = this.titulo,
        descripcion = this.descripcion,
        categoria = this.categoria,
        prioridad = prioridadDominio,
        estado = estadoDominio,
        fechaCreacion = this.fechaCreacion,
        estadoSync = syncDominio
    )
}

fun Recordatorio.toEntity(
    remoteId: Int? = null,
    estadoSync: EstadoSync = this.estadoSync
): RecordatorioEntity {
    val estadoTexto = when (this.estado) {
        is EstadoRecordatorio.Completado -> "Completado"
        is EstadoRecordatorio.EnProceso -> "EnProceso"
        is EstadoRecordatorio.Pendiente -> "Pendiente"
    }

    return RecordatorioEntity(
        id = this.id,
        remoteId = remoteId,
        titulo = this.titulo,
        descripcion = this.descripcion,
        categoria = this.categoria,
        prioridad = this.prioridad.name,
        estado = estadoTexto,
        fechaCreacion = this.fechaCreacion,
        estadoSync = estadoSync.name
    )
}

fun RecordatorioDto.toEntity(localId: Int = 0): RecordatorioEntity {
    return RecordatorioEntity(
        id = localId,
        remoteId = this.id,
        titulo = this.title.replaceFirstChar { it.uppercase() },
        descripcion = "Sincronizado desde servicio remoto (ID #${this.id ?: "-"})",
        categoria = "Remoto",
        prioridad = Prioridad.MEDIA.name,
        estado = if (this.completed) "Completado" else "Pendiente",
        fechaCreacion = System.currentTimeMillis(),
        estadoSync = EstadoSync.SINCRONIZADO.name
    )
}

fun RecordatorioEntity.toDto(): RecordatorioDto {
    return RecordatorioDto(
        id = this.remoteId,
        userId = 1,
        title = this.titulo,
        completed = this.estado == "Completado"
    )
}
