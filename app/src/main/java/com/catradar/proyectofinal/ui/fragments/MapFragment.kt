package com.catradar.proyectofinal.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.catradar.proyectofinal.R
import com.catradar.proyectofinal.model.Reporte
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        loadReportesDesdeFirestore()
    }

    private fun loadReportesDesdeFirestore() {
        FirebaseFirestore.getInstance().collection("reportes")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val reporte = document.toObject(Reporte::class.java)
                    val posicion = LatLng(reporte.latitud, reporte.longitud)

                    googleMap.addMarker(
                        MarkerOptions()
                            .position(posicion)
                            .title("Gato reportado")
                            .snippet(reporte.descripcion)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                    )
                }

                // Opcional: centrar el mapa si hay reportes
                if (result.documents.isNotEmpty()) {
                    val first = result.documents.first().toObject(Reporte::class.java)
                    first?.let {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitud, it.longitud), 13f))
                    }
                }
            }
    }
}
