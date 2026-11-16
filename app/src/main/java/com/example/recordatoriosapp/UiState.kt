package com.example.recordatoriosapp

// 1. Estados para la Pantalla de Listado (Consigna 3)
sealed interface ListadoUiState {
    data object Cargando : ListadoUiState
    data object Vacio : ListadoUiState
    data class Exito(val recordatorios: List<Recordatorio>) : ListadoUiState
    data class Error(val mensaje: String) : ListadoUiState
}

// 2. Estado para la Pantalla de Creación / Formulario (Consigna 3)
data class CreacionUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "Personal",
    val prioridad: Prioridad = Prioridad.MEDIA,

    // Mensajes de error de validación
    val tituloError: String? = null,
    val descripcionError: String? = null,
    val categoriaError: String? = null,

    // Estados de envío del formulario
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val generalError: String? = null
) {
    // El botón se deshabilita si el formulario no es válido o está enviando (Consigna 2)
    val isValid: Boolean
        get() = titulo.trim().length >= 4 &&
                descripcion.length <= 200 &&
                tituloError == null &&
                descripcionError == null &&
                !isSubmitting
}

// 3. Estados para la Pantalla de Detalle
sealed interface DetalleUiState {
    data object Cargando : DetalleUiState
    data class Exito(val recordatorio: Recordatorio) : DetalleUiState
    data class Error(val mensaje: String) : DetalleUiState
}