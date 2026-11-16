package com.example.recordatoriosapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {

    // Listado completo reactivo (excluyendo los marcados para eliminar offline)
    @Query("SELECT * FROM recordatorios WHERE estadoSync != 'PENDIENTE_ELIMINACION' ORDER BY fechaCreacion DESC")
    fun observarTodos(): Flow<List<RecordatorioEntity>>

    // Consulta con filtro por estado (Consigna 2)
    @Query("SELECT * FROM recordatorios WHERE estado = :estado AND estadoSync != 'PENDIENTE_ELIMINACION' ORDER BY fechaCreacion DESC")
    fun observarPorEstado(estado: String): Flow<List<RecordatorioEntity>>

    // Obtener un recordatorio específico por ID local
    @Query("SELECT * FROM recordatorios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): RecordatorioEntity?

    // Obtener un recordatorio por su ID remoto (para evitar duplicados al sincronizar)
    @Query("SELECT * FROM recordatorios WHERE remoteId = :remoteId LIMIT 1")
    suspend fun obtenerPorRemoteId(remoteId: Int): RecordatorioEntity?

    // Obtener operaciones pendientes de sincronización
    @Query("SELECT * FROM recordatorios WHERE estadoSync != 'SINCRONIZADO'")
    suspend fun obtenerPendientesDeSync(): List<RecordatorioEntity>

    // Insertar o reemplazar y devolver el ID autogenerado
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(recordatorio: RecordatorioEntity): Long

    // Actualizar registro existente
    @Update
    suspend fun actualizar(recordatorio: RecordatorioEntity)

    // Eliminación física definitiva
    @Delete
    suspend fun eliminar(recordatorio: RecordatorioEntity)

    @Query("DELETE FROM recordatorios WHERE id = :id")
    suspend fun eliminarPorId(id: Int)
}