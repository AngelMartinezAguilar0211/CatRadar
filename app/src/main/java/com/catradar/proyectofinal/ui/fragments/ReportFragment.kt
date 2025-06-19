package com.catradar.proyectofinal.ui.fragments

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.catradar.proyectofinal.R
import com.catradar.proyectofinal.model.Reporte
import com.catradar.proyectofinal.ui.activities.SeleccionarUbicacionActivity
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
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

    private val launcherUbicacion = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val data = it.data
            latitud = data?.getDoubleExtra("latitud", 0.0)
            longitud = data?.getDoubleExtra("longitud", 0.0)
            view?.findViewById<TextView>(R.id.textViewLocation)?.text =
                "Ubicación: $latitud, $longitud"
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
        view.findViewById<Button>(R.id.buttonElegirUbicacion).setOnClickListener {
            val intent = Intent(requireContext(), SeleccionarUbicacionActivity::class.java)
            launcherUbicacion.launch(intent)
        }

    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1002)
        }
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
        val titulo = view?.findViewById<EditText>(R.id.editTextTitulo)?.text.toString()
        val descripcion = view?.findViewById<EditText>(R.id.editTextDescription)?.text.toString()

        if (titulo.isBlank() || descripcion.isBlank() || imageUri == null || latitud == null || longitud == null) {
            Toast.makeText(requireContext(), "Completa todos los campos antes de guardar", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("reportes/${System.currentTimeMillis()}.jpg")

        fileRef.putFile(imageUri!!)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val reporte = Reporte(
                        titulo = titulo,
                        descripcion = descripcion,
                        fotoUrl = uri.toString(),
                        latitud = latitud!!,
                        longitud = longitud!!
                    )

                    FirebaseFirestore.getInstance()
                        .collection("reportes")
                        .add(reporte)
                        .addOnSuccessListener {
                            if (isAdded && view != null) {
                                Toast.makeText(requireContext(), "Reporte guardado exitosamente", Toast.LENGTH_SHORT).show()

                                view?.findViewById<EditText>(R.id.editTextTitulo)?.text?.clear()
                                view?.findViewById<EditText>(R.id.editTextDescription)?.text?.clear()
                                view?.findViewById<TextView>(R.id.textViewLocation)?.text = "Ubicación: (lat, long)"
                                view?.findViewById<ImageView>(R.id.imageViewPreview)?.setImageDrawable(null)

                                imageUri = null
                                latitud = null
                                longitud = null

                                Log.d("Reporte", "Guardado en Firestore con éxito y campos limpiados")
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Error al guardar reporte", Toast.LENGTH_SHORT).show()
                            Log.e("Reporte", "Error guardando en Firestore: ${it.message}")
                        }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al obtener URL de imagen", Toast.LENGTH_SHORT).show()
                    Log.e("Reporte", "Error obteniendo URL: ${it.message}")
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                Log.e("Reporte", "Error subiendo imagen: ${it.message}")
            }
    }



    private fun saveImageToGallery(bitmap: Bitmap): Uri {
        val path = MediaStore.Images.Media.insertImage(requireActivity().contentResolver, bitmap, "ReporteGato", null)
        return Uri.parse(path)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

}
