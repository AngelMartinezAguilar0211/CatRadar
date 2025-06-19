package com.catradar.proyectofinal.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.catradar.proyectofinal.R
import com.catradar.proyectofinal.model.Reporte
import com.catradar.proyectofinal.ui.activities.ReporteDetailActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ContextCompat.checkSelfPermission(requireContext(), locationPermission) ==
            PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            centrarEnUbicacionActual()
        } else {
            requestPermissions(arrayOf(locationPermission), 1002)
        }

        googleMap.uiSettings.isZoomControlsEnabled = true
        loadReportesDesdeFirestore()
        googleMap.setOnInfoWindowClickListener { marker ->
            val reporte = marker.tag as? Reporte
            reporte?.let {
                val intent = Intent(requireContext(), ReporteDetailActivity::class.java).apply {
                    putExtra("descripcion", it.descripcion)
                    putExtra("fotoUrl", it.fotoUrl)
                    putExtra("latitud", it.latitud)
                    putExtra("longitud", it.longitud)
                }
                startActivity(intent)
            }
        }

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
                    val marcador = googleMap.addMarker(
                        MarkerOptions()
                            .position(posicion)
                            .title("Gato reportado")
                            .snippet(reporte.descripcion)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                    )
                    marcador?.tag = reporte
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
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun centrarEnUbicacionActual() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val posicion = LatLng(it.latitude, it.longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 15f))
            }
        }
    }

}
