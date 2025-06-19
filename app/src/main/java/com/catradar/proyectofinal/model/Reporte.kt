package com.catradar.proyectofinal.model

data class Reporte(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fotoUrl: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fecha: Long = System.currentTimeMillis()
)
