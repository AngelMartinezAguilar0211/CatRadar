package com.catradar.proyectofinal.ui.activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.catradar.proyectofinal.R

class ReporteDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_detail)

        val descripcion = intent.getStringExtra("descripcion") ?: ""
        val fotoUrl = intent.getStringExtra("fotoUrl") ?: ""
        val lat = intent.getDoubleExtra("latitud", 0.0)
        val lng = intent.getDoubleExtra("longitud", 0.0)

        val imageView = findViewById<ImageView>(R.id.imageViewDetalle)
        val textDescripcion = findViewById<TextView>(R.id.textViewDescripcion)
        val textUbicacion = findViewById<TextView>(R.id.textViewUbicacion)

        textDescripcion.text = descripcion
        textUbicacion.text = "Ubicación: $lat, $lng"
        Glide.with(this).load(fotoUrl).into(imageView)
    }
}
