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

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "configuracion_recordatorios")

enum class CriterioOrden {
    FECHA,
    PRIORIDAD,
    TITULO
}

data class PreferenciasApp(
    val temaOscuro: Boolean = false,
    val mostrarCompletados: Boolean = true,
    val ordenamiento: CriterioOrden = CriterioOrden.FECHA
)

class PreferenciasRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val TEMA_OSCURO = booleanPreferencesKey("tema_oscuro")
        val MOSTRAR_COMPLETADOS = booleanPreferencesKey("mostrar_completados")
        val ORDENAMIENTO = stringPreferencesKey("criterio_ordenamiento")
    }

    val preferenciasFlow: Flow<PreferenciasApp> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val temaOscuro = preferences[PreferencesKeys.TEMA_OSCURO] ?: false
            val mostrarCompletados = preferences[PreferencesKeys.MOSTRAR_COMPLETADOS] ?: true

            val ordenGuardado = preferences[PreferencesKeys.ORDENAMIENTO]
            val ordenamiento = try {
                if (ordenGuardado != null) CriterioOrden.valueOf(ordenGuardado) else CriterioOrden.FECHA
            } catch (e: IllegalArgumentException) {
                CriterioOrden.FECHA
            }

            PreferenciasApp(
                temaOscuro = temaOscuro,
                mostrarCompletados = mostrarCompletados,
                ordenamiento = ordenamiento
            )
        }

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
