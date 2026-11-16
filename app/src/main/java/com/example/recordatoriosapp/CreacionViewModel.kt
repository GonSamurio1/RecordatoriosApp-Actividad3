package com.example.recordatoriosapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreacionViewModel(
    private val repository: RecordatorioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreacionUiState())
    val uiState: StateFlow<CreacionUiState> = _uiState.asStateFlow()

    fun onTituloChanged(nuevoTitulo: String) {
        val error = if (nuevoTitulo.trim().length < 4) "El título debe tener al menos 4 caracteres" else null
        _uiState.update { it.copy(titulo = nuevoTitulo, tituloError = error) }
    }

    fun onDescripcionChanged(nuevaDescripcion: String) {
        val error = if (nuevaDescripcion.length > 200) "La descripción no puede superar los 200 caracteres" else null
        _uiState.update { it.copy(descripcion = nuevaDescripcion, descripcionError = error) }
    }

    fun onCategoriaChanged(nuevaCategoria: String) {
        _uiState.update { it.copy(categoria = nuevaCategoria) }
    }

    fun onPrioridadChanged(nuevaPrioridad: Prioridad) {
        _uiState.update { it.copy(prioridad = nuevaPrioridad) }
    }

    fun guardarRecordatorio() {
        if (!_uiState.value.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, generalError = null) }

            repository.crearRecordatorio(
                titulo = _uiState.value.titulo.trim(),
                descripcion = _uiState.value.descripcion.ifBlank { null },
                categoria = _uiState.value.categoria,
                prioridad = _uiState.value.prioridad,
                estado = EstadoRecordatorio.Pendiente
            )

            // Al guardarse en Room (Local-First), la creación es exitosa incluso sin internet
            _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
        }
    }
}