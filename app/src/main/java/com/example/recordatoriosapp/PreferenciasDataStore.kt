package com.example.recordatoriosapp

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Instancia única (Singleton) de Preferences DataStore vinculada al Context
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "configuracion_recordatorios")

// Enum tipado para el criterio de ordenamiento
enum class CriterioOrden {
    FECHA,
    PRIORIDAD,
    TITULO
}

// Modelo propio de dominio para las preferencias de la aplicación
data class PreferenciasApp(
    val temaOscuro: Boolean = false,
    val mostrarCompletados: Boolean = true,
    val ordenamiento: CriterioOrden = CriterioOrden.FECHA
)

// Gestor / Repositorio de Preferencias
class PreferenciasRepository(private val dataStore: DataStore<Preferences>) {

    // Claves tipadas privadas
    private object PreferencesKeys {
        val TEMA_OSCURO = booleanPreferencesKey("tema_oscuro")
        val MOSTRAR_COMPLETADOS = booleanPreferencesKey("mostrar_completados")
        val ORDENAMIENTO = stringPreferencesKey("criterio_ordenamiento")
    }

    // Exposición reactiva mediante Flow con manejo de errores de lectura y valores por defecto
    val preferenciasFlow: Flow<PreferenciasApp> = dataStore.data
        .catch { exception ->
            // Si ocurre un error recuperable de lectura en disco, emitimos preferencias vacías
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val temaOscuro = preferences[PreferencesKeys.TEMA_OSCURO] ?: false
            val mostrarCompletados = preferences[PreferencesKeys.MOSTRAR_COMPLETADOS] ?: true

            // Validación ante dato inválido o desconocido en el enum
            val ordenGuardado = preferences[PreferencesKeys.ORDENAMIENTO]
            val ordenamiento = try {
                if (ordenGuardado != null) CriterioOrden.valueOf(ordenGuardado) else CriterioOrden.FECHA
            } catch (e: IllegalArgumentException) {
                // Si hay un texto corrupto o desconocido, volvemos al valor predeterminado
                CriterioOrden.FECHA
            }

            PreferenciasApp(
                temaOscuro = temaOscuro,
                mostrarCompletados = mostrarCompletados,
                ordenamiento = ordenamiento
            )
        }

    // Funciones suspendidas para actualizar cada preferencia sin bloquear el hilo principal
    suspend fun actualizarTemaOscuro(habilitado: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEMA_OSCURO] = habilitado
        }
    }

    suspend fun actualizarMostrarCompletados(mostrar: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MOSTRAR_COMPLETADOS] = mostrar
        }
    }

    suspend fun actualizarOrdenamiento(nuevoOrden: CriterioOrden) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ORDENAMIENTO] = nuevoOrden.name
        }
    }
}