package com.example.recordatoriosapp

import com.google.gson.annotations.SerializedName

// DTO (Data Transfer Object) que representa exclusivamente el contrato JSON del servicio remoto
data class RecordatorioDto(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("userId")
    val userId: Int = 1,
    @SerializedName("title")
    val title: String,
    @SerializedName("completed")
    val completed: Boolean = false
)