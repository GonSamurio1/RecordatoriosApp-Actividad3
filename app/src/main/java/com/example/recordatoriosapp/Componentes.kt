package com.example.recordatoriosapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Mapa local de colores por categoría para evitar referencias externas faltantes
val coloresCategoriaMap = mapOf(
    "Trabajo" to Color(0xFF1E88E5),
    "Estudio" to Color(0xFF43A047),
    "Personal" to Color(0xFFFB8C00),
    "Hogar" to Color(0xFF8E24AA)
)

// Componente de Ítem reutilizable
@Composable
fun RecordatorioItem(
    recordatorio: Recordatorio,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = recordatorio.titulo, style = MaterialTheme.typography.titleMedium)
                recordatorio.descripcion?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = "Prioridad: ${recordatorio.prioridad} | Estado: ${recordatorio.estado}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Surface(
                color = coloresCategoriaMap[recordatorio.categoria] ?: Color.Gray,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = recordatorio.categoria,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// Componente para Carga, Vacío o Error (Consigna 2)
@Composable
fun EstadoPantalla(
    cargando: Boolean = false,
    mensajeError: String? = null,
    mensajeVacio: String? = null,
    onReintentar: (() -> Unit)? = null,
    onCrear: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            cargando -> CircularProgressIndicator()
            mensajeError != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = mensajeError, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                onReintentar?.let { Button(onClick = it) { Text("Reintentar") } }
            }
            mensajeVacio != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = mensajeVacio)
                Spacer(modifier = Modifier.height(8.dp))
                onCrear?.let { Button(onClick = it) { Text("Crear Recordatorio") } }
            }
        }
    }
}