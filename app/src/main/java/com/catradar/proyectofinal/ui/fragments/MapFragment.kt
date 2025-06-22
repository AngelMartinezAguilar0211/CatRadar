package com.catradar.proyectofinal.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
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
import android.os.Handler
import android.os.Looper
import android.graphics.drawable.Drawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition



class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ContextCompat.checkSelfPermission(requireContext(), locationPermission) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter())
            centrarEnUbicacionActual()
        } else {
            requestPermissions(arrayOf(locationPermission), 1002)
        }

        googleMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.clear()

        loadReportesDesdeFirestore()
        googleMap.setOnInfoWindowClickListener { marker ->
            val reporte = marker.tag as? Reporte
            reporte?.let {
                val intent = Intent(requireContext(), ReporteDetailActivity::class.java).apply {
                    putExtra("titulo", it.titulo)
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
        FirebaseFirestore.getInstance("catradar").collection("reportes")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val reporte = document.toObject(Reporte::class.java)
                    val posicion = LatLng(reporte.latitud, reporte.longitud)

                    val marcador = googleMap.addMarker(
                        MarkerOptions()
                            .position(posicion)
                            .title(reporte.titulo)
                            .snippet(reporte.descripcion)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                    )
                    marcador?.tag = reporte

                }

                // Opcional: centrar el mapa si hay reportes
                if (result.documents.isNotEmpty()) {
                    val first = result.documents.first().toObject(Reporte::class.java)
                    first?.let {
                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    it.latitud,
                                    it.longitud
                                ), 13f
                            )
                        )
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

    inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {

        override fun getInfoWindow(marker: Marker): View? = null
        override fun getInfoContents(marker: Marker): View {
            val view = LayoutInflater.from(context).inflate(R.layout.info_window_layout, null)

            val titulo = view.findViewById<TextView>(R.id.textViewTituloInfo)
            val image = view.findViewById<ImageView>(R.id.imageViewInfo)

            val reporte = marker.tag as? Reporte ?: return view

            titulo.text = reporte.titulo
            image.setImageResource(R.drawable.ic_profile) // o la que uses por defecto

            // Cargar imagen asíncrona sin redibujar directamente
            context?.let {
                Glide.with(it)
                    .load(reporte.fotoUrl)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            image.setImageDrawable(resource)

                            // Postpone redibujado para evitar conflicto de vista duplicada
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (marker.isInfoWindowShown) {
                                    marker.hideInfoWindow()
                                    marker.showInfoWindow()
                                }
                            }, 10000)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            image.setImageDrawable(placeholder)
                        }
                    })
            }

            return view
        }

    }
}

