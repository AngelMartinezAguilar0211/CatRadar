package com.catradar.proyectofinal.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.catradar.proyectofinal.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class SeleccionarUbicacionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private var marcador: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_ubicacion)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapaSeleccion) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<View>(R.id.botonConfirmarUbicacion).setOnClickListener {
            marcador?.position?.let {
                val resultIntent = Intent()
                resultIntent.putExtra("latitud", it.latitude)
                resultIntent.putExtra("longitud", it.longitude)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val defaultLatLng = LatLng(19.4326, -99.1332) // CDMX por defecto
        marcador = googleMap.addMarker(MarkerOptions()
            .position(defaultLatLng)
            .title("Ubicación del reporte")
            .draggable(true)
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 15f))

        googleMap.setOnMapClickListener {
            marcador?.position = it
        }
    }
}
