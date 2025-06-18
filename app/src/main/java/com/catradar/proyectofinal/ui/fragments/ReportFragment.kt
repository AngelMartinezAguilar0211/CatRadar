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
import com.catradar.proyectofinal.model.Reporte
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

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
        val descripcion = view?.findViewById<EditText>(R.id.editTextDescription)?.text.toString()

        if (imageUri == null || latitud == null || longitud == null || descripcion.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("reportes/${System.currentTimeMillis()}.jpg")

        fileRef.putFile(imageUri!!)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val reporte = Reporte(
                        descripcion = descripcion,
                        fotoUrl = uri.toString(),
                        latitud = latitud!!,
                        longitud = longitud!!
                    )
                    FirebaseFirestore.getInstance()
                        .collection("reportes")
                        .add(reporte)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Reporte guardado correctamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Error al guardar reporte", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveImageToGallery(bitmap: Bitmap): Uri {
        val path = MediaStore.Images.Media.insertImage(requireActivity().contentResolver, bitmap, "ReporteGato", null)
        return Uri.parse(path)
    }
}
