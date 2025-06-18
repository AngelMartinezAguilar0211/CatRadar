package com.catradar.proyectofinal.ui.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.catradar.proyectofinal.R
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class ReportFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var textViewLocation: TextView
    private var imageUri: Uri? = null
    private var latitud: Double? = null
    private var longitud: Double? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val bitmap = it.data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(bitmap)
            imageUri = saveImageToGallery(bitmap)
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageView.setImageURI(it)
            imageUri = it
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_report, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imageView = view.findViewById(R.id.imageViewPreview)
        textViewLocation = view.findViewById(R.id.textViewLocation)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        view.findViewById<Button>(R.id.buttonTakePhoto).setOnClickListener { openCamera() }
        view.findViewById<Button>(R.id.buttonPickGallery).setOnClickListener { openGallery() }
        view.findViewById<Button>(R.id.buttonGetLocation).setOnClickListener { getLocation() }
        view.findViewById<Button>(R.id.buttonSaveReport).setOnClickListener { saveReport() }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == android.content.pm.PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    latitud = it.latitude
                    longitud = it.longitude
                    textViewLocation.text = "Ubicación: ${latitud}, ${longitud}"
                }
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
    }

    private fun saveReport() {
        val desc = view?.findViewById<EditText>(R.id.editTextDescription)?.text.toString()
        Toast.makeText(requireContext(), "Guardado (simulado): $desc", Toast.LENGTH_SHORT).show()
        // Aquí después se integrará con Firebase
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri {
        val path = MediaStore.Images.Media.insertImage(requireActivity().contentResolver, bitmap, "ReporteGato", null)
        return Uri.parse(path)
    }
}
