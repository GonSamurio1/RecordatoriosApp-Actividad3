package com.example.recordatoriosapp

sealed interface ListadoUiState {
    data object Cargando : ListadoUiState
    data object Vacio : ListadoUiState
    data class Exito(val recordatorios: List<Recordatorio>) : ListadoUiState
    data class Error(val mensaje: String) : ListadoUiState
}

data class CreacionUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "Personal",
    val prioridad: Prioridad = Prioridad.MEDIA,

    val tituloError: String? = null,
    val descripcionError: String? = null,
    val categoriaError: String? = null,

    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val generalError: String? = null
) {
    val isValid: Boolean
        get() = titulo.trim().length >= 4 &&
                descripcion.length <= 200 &&
                tituloError == null &&
                descripcionError == null &&
                !isSubmitting
}

sealed interface DetalleUiState {
    data object Cargando : DetalleUiState
    data class Exito(val recordatorio: Recordatorio) : DetalleUiState
    data class Error(val mensaje: String) : DetalleUiState
}
