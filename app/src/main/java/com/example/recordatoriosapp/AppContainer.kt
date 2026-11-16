package com.example.recordatoriosapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// Contenedor único de dependencias de la aplicación (Singleton)
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    // 1. Preferencias persistentes (DataStore)
    val preferenciasRepository: PreferenciasRepository by lazy {
        PreferenciasRepository(appContext.dataStore)
    }

    // 2. Base de datos local (Room)
    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(appContext)
    }

    // 3. Servicio remoto (Retrofit)
    private val apiService: RecordatorioApiService by lazy {
        RetrofitClient.getApiService(appContext)
    }

    // 4. Repositorio Local-First
    val recordatorioRepository: RecordatorioRepository by lazy {
        RecordatorioRepository(
            dao = database.recordatorioDao(),
            api = apiService
        )
    }
}

// Factory para instanciar los ViewModels con el repositorio real
class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ListadoViewModel::class.java) -> {
                ListadoViewModel(container.recordatorioRepository, container.preferenciasRepository) as T
            }
            modelClass.isAssignableFrom(CreacionViewModel::class.java) -> {
                CreacionViewModel(container.recordatorioRepository) as T
            }
            modelClass.isAssignableFrom(DetalleViewModel::class.java) -> {
                DetalleViewModel(container.recordatorioRepository) as T
            }
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}