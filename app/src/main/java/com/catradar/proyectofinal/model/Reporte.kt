package com.catradar.proyectofinal.model
import com.google.firebase.Timestamp

data class Reporte(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fotoUrl: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val estado: String = "",
    val fecha: Timestamp? = Timestamp.now(),
    val uid: String = ""
)
