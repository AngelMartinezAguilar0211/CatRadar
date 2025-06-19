package com.catradar.proyectofinal.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.catradar.proyectofinal.R

class ReporteDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_detail)

        findViewById<Button>(R.id.buttonRegresar).setOnClickListener {
            finish()
        }

        val titulo = intent.getStringExtra("titulo") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""
        val fotoUrl = intent.getStringExtra("fotoUrl") ?: ""
        val lat = intent.getDoubleExtra("latitud", 0.0)
        val lng = intent.getDoubleExtra("longitud", 0.0)

        val imageView = findViewById<ImageView>(R.id.imageViewDetalle)
        val textDescripcion = findViewById<TextView>(R.id.textViewDescripcion)
        val textUbicacion = findViewById<TextView>(R.id.textViewUbicacion)
        val textTitulo = findViewById<TextView>(R.id.textViewTitulo)

        textTitulo.text = titulo
        textDescripcion.text = descripcion

        textDescripcion.text = descripcion
        textUbicacion.text = "Ubicación: $lat, $lng"
        Glide.with(this).load(fotoUrl).into(imageView)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

}
