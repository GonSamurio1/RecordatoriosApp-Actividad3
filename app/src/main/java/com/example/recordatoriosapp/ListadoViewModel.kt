package com.example.recordatoriosapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListadoViewModel(
    private val repository: RecordatorioRepository,
    private val preferenciasRepo: PreferenciasRepository
) : ViewModel() {

    // Preferencias reactivas desde DataStore
    val preferencias: StateFlow<PreferenciasApp> = preferenciasRepo.preferenciasFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PreferenciasApp()
        )

    // Mensaje informativo de sincronización / errores controlados de red
    private val _mensajeSync = MutableStateFlow<String?>(null)
    val mensajeSync: StateFlow<String?> = _mensajeSync.asStateFlow()

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    // Combinamos el Flow de Room con el Flow de DataStore
    val uiState: StateFlow<ListadoUiState> = combine(
        repository.recordatoriosFlow,
        preferenciasRepo.preferenciasFlow
    ) { lista, prefs ->
        // 1. Aplicar preferencia de visibilidad de completados
        val filtrados = if (prefs.mostrarCompletados) {
            lista
        } else {
            lista.filter { it.estado !is EstadoRecordatorio.Completado }
        }

        // 2. Aplicar preferencia de ordenamiento
        val ordenados = when (prefs.ordenamiento) {
            CriterioOrden.FECHA -> filtrados.sortedByDescending { it.fechaCreacion }
            CriterioOrden.PRIORIDAD -> filtrados.sortedByDescending { it.prioridad.ordinal }
            CriterioOrden.TITULO -> filtrados.sortedBy { it.titulo.lowercase() }
        }

        if (ordenados.isEmpty()) {
            ListadoUiState.Vacio
        } else {
            ListadoUiState.Exito(recordatorios = ordenados)
        }
    }
        .catch { emit(ListadoUiState.Error("Error al leer la base de datos local")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ListadoUiState.Cargando
        )

    init {
        // Sincronización inicial al abrir la app (sube pendientes y descarga remotos sin duplicar)
        sincronizar(descargarRemotos = true)
    }

    fun sincronizar(descargarRemotos: Boolean = true) {
        viewModelScope.launch {
            _sincronizando.value = true
            val resultado = repository.sincronizarConServidor(descargarRemotos = descargarRemotos)
            _sincronizando.value = false
            _mensajeSync.value = when (resultado) {
                is ResultadoSync.Exito -> null
                is ResultadoSync.ErrorControlado -> resultado.mensaje
            }
        }
    }

    fun limpiarMensajeSync() {
        _mensajeSync.value = null
    }

    // Acciones sobre preferencias (DataStore)
    fun cambiarTemaOscuro(habilitado: Boolean) {
        viewModelScope.launch { preferenciasRepo.actualizarTemaOscuro(habilitado) }
    }

    fun cambiarMostrarCompletados(mostrar: Boolean) {
        viewModelScope.launch { preferenciasRepo.actualizarMostrarCompletados(mostrar) }
    }

    fun cambiarOrdenamiento(orden: CriterioOrden) {
        viewModelScope.launch { preferenciasRepo.actualizarOrdenamiento(orden) }
    }
}