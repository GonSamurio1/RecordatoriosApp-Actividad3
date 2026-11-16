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

    @Query("SELECT * FROM recordatorios WHERE estadoSync != 'PENDIENTE_ELIMINACION' ORDER BY fechaCreacion DESC")
    fun observarTodos(): Flow<List<RecordatorioEntity>>

    @Query("SELECT * FROM recordatorios WHERE estado = :estado AND estadoSync != 'PENDIENTE_ELIMINACION' ORDER BY fechaCreacion DESC")
    fun observarPorEstado(estado: String): Flow<List<RecordatorioEntity>>

    @Query("SELECT * FROM recordatorios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): RecordatorioEntity?

    @Query("SELECT * FROM recordatorios WHERE remoteId = :remoteId LIMIT 1")
    suspend fun obtenerPorRemoteId(remoteId: Int): RecordatorioEntity?

    @Query("SELECT * FROM recordatorios WHERE estadoSync != 'SINCRONIZADO'")
    suspend fun obtenerPendientesDeSync(): List<RecordatorioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(recordatorio: RecordatorioEntity): Long

    @Update
    suspend fun actualizar(recordatorio: RecordatorioEntity)

    @Delete
    suspend fun eliminar(recordatorio: RecordatorioEntity)

    @Query("DELETE FROM recordatorios WHERE id = :id")
    suspend fun eliminarPorId(id: Int)
}
