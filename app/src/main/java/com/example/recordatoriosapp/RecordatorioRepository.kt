package com.example.recordatoriosapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// Resultado controlado de operaciones de red (Consigna 4)
sealed class ResultadoSync {
    object Exito : ResultadoSync()
    data class ErrorControlado(val mensaje: String) : ResultadoSync()
}

class RecordatorioRepository(
    private val dao: RecordatorioDao,
    private val api: RecordatorioApiService
) {
    // Mutex para evitar ejecuciones simultáneas de sincronización y duplicación de registros (Consigna 3)
    private val syncMutex = Mutex()

    // Room es la fuente única de verdad para la interfaz (Consigna 2 y 3)
    val recordatoriosFlow: Flow<List<Recordatorio>> = dao.observarTodos()
        .map { entidades -> entidades.map { it.toDomain() } }

    suspend fun obtenerPorId(id: Int): Recordatorio? {
        return dao.obtenerPorId(id)?.toDomain()
    }

    // 1. CREAR (Local-First: guarda primero en Room como PENDIENTE_CREACION)
    suspend fun crearRecordatorio(
        titulo: String,
        descripcion: String?,
        categoria: String,
        prioridad: Prioridad,
        estado: EstadoRecordatorio = EstadoRecordatorio.Pendiente
    ): ResultadoSync {
        val nuevo = Recordatorio(
            id = 0,
            titulo = titulo,
            descripcion = descripcion,
            categoria = categoria,
            prioridad = prioridad,
            estado = estado,
            estadoSync = EstadoSync.PENDIENTE_CREACION
        )
        dao.insertar(nuevo.toEntity(estadoSync = EstadoSync.PENDIENTE_CREACION))

        // Intentamos sincronizar inmediatamente si hay red; si falla, queda guardado offline
        return sincronizarPendientes()
    }

    // 2. ACTUALIZAR (Local-First: actualiza primero en Room)
    suspend fun actualizarRecordatorio(recordatorio: Recordatorio): ResultadoSync {
        val existente = dao.obtenerPorId(recordatorio.id) ?: return ResultadoSync.ErrorControlado("Recordatorio no encontrado")

        // Si nunca se subió al servidor, sigue pendiente de creación; si ya existía remoto, queda pendiente de actualización
        val nuevoEstadoSync = if (existente.remoteId == null) {
            EstadoSync.PENDIENTE_CREACION
        } else {
            EstadoSync.PENDIENTE_ACTUALIZACION
        }

        val entidadActualizada = recordatorio.toEntity(
            remoteId = existente.remoteId,
            estadoSync = nuevoEstadoSync
        )
        dao.actualizar(entidadActualizada)

        return sincronizarPendientes()
    }

    // 3. ELIMINAR (Local-First: si nunca se sincronizó se borra directo; si tiene remoteId se marca pendiente)
    suspend fun eliminarRecordatorio(id: Int): ResultadoSync {
        val existente = dao.obtenerPorId(id) ?: return ResultadoSync.Exito

        if (existente.remoteId == null) {
            dao.eliminarPorId(id)
            return ResultadoSync.Exito
        } else {
            dao.actualizar(existente.copy(estadoSync = EstadoSync.PENDIENTE_ELIMINACION.name))
            return sincronizarPendientes()
        }
    }

    // 4. SINCRONIZACIÓN CONTROLADA (Sube pendientes y descarga datos remotos sin duplicar)
    suspend fun sincronizarConServidor(descargarRemotos: Boolean = false): ResultadoSync {
        return syncMutex.withLock {
            try {
                // Paso A: Enviar operaciones pendientes locales
                val resultadoSubida = procesarColaPendientesInterna()
                if (resultadoSubida is ResultadoSync.ErrorControlado) {
                    return@withLock resultadoSubida
                }

                // Paso B: Si se solicita refrescar desde el servidor, traer y fusionar sin duplicar
                if (descargarRemotos) {
                    val respuesta = api.obtenerRecordatorios(limite = 5)
                    if (respuesta.isSuccessful) {
                        val listaRemota = respuesta.body().orEmpty()
                        for (dto in listaRemota) {
                            val remoteId = dto.id ?: continue
                            val localExistente = dao.obtenerPorRemoteId(remoteId)
                            if (localExistente == null) {
                                dao.insertar(dto.toEntity(localId = 0))
                            } else if (localExistente.estadoSync == EstadoSync.SINCRONIZADO.name) {
                                dao.actualizar(dto.toEntity(localId = localExistente.id))
                            }
                        }
                    } else {
                        return@withLock interpretarErrorHttp(respuesta.code())
                    }
                }

                ResultadoSync.Exito
            } catch (e: Exception) {
                interpretarExcepcionRed(e)
            }
        }
    }

    private suspend fun sincronizarPendientes(): ResultadoSync {
        return syncMutex.withLock {
            try {
                procesarColaPendientesInterna()
            } catch (e: Exception) {
                interpretarExcepcionRed(e)
            }
        }
    }

    private suspend fun procesarColaPendientesInterna(): ResultadoSync {
        val pendientes = dao.obtenerPendientesDeSync()

        for (item in pendientes) {
            when (item.estadoSync) {
                EstadoSync.PENDIENTE_CREACION.name, EstadoSync.ERROR_SYNC.name -> {
                    val resp = api.crearRecordatorio(item.toDto())
                    if (resp.isSuccessful) {
                        val remotoCreado = resp.body()
                        dao.actualizar(
                            item.copy(
                                remoteId = remotoCreado?.id ?: item.id,
                                estadoSync = EstadoSync.SINCRONIZADO.name
                            )
                        )
                    } else {
                        dao.actualizar(item.copy(estadoSync = EstadoSync.ERROR_SYNC.name))
                        return interpretarErrorHttp(resp.code())
                    }
                }

                EstadoSync.PENDIENTE_ACTUALIZACION.name -> {
                    val rId = item.remoteId ?: item.id
                    val resp = api.actualizarRecordatorio(rId, item.toDto())
                    if (resp.isSuccessful) {
                        dao.actualizar(item.copy(estadoSync = EstadoSync.SINCRONIZADO.name))
                    } else {
                        dao.actualizar(item.copy(estadoSync = EstadoSync.ERROR_SYNC.name))
                        return interpretarErrorHttp(resp.code())
                    }
                }

                EstadoSync.PENDIENTE_ELIMINACION.name -> {
                    val rId = item.remoteId ?: item.id
                    val resp = api.eliminarRecordatorio(rId)
                    if (resp.isSuccessful) {
                        dao.eliminar(item)
                    } else {
                        return interpretarErrorHttp(resp.code())
                    }
                }
            }
        }
        return ResultadoSync.Exito
    }

    // Interpretación controlada de códigos HTTP (Consigna 4)
    private fun interpretarErrorHttp(codigo: Int): ResultadoSync.ErrorControlado {
        val mensaje = when (codigo) {
            in 400..499 -> "Solicitud inválida al servidor (HTTP $codigo). Los cambios siguen guardados localmente."
            in 500..599 -> "El servidor no está disponible temporalmente (HTTP $codigo). Trabajando en modo offline."
            else -> "Respuesta inesperada del servidor (HTTP $codigo)."
        }
        return ResultadoSync.ErrorControlado(mensaje)
    }

    // Interpretación controlada de excepciones de red (Consigna 4)
    private fun interpretarExcepcionRed(e: Exception): ResultadoSync.ErrorControlado {
        val mensaje = when (e) {
            is SocketTimeoutException -> "Tiempo de espera agotado (Timeout). Tus datos están a salvo en el dispositivo."
            is UnknownHostException, is IOException -> "Sin conexión a internet. Modo offline activo: los cambios se sincronizarán al volver la red."
            else -> "No se pudo sincronizar (${e.localizedMessage ?: "error desconocido"}). Datos locales conservados."
        }
        return ResultadoSync.ErrorControlado(mensaje)
    }
}