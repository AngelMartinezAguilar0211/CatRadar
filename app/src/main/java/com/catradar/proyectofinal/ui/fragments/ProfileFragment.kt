package com.catradar.proyectofinal.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.catradar.proyectofinal.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.edit
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private lateinit var editTextNombre: EditText
    private lateinit var textViewFecha: TextView
    private lateinit var textViewEstadisticas: TextView
    private lateinit var buttonGuardar: Button

    private lateinit var imageViewPerfil: ImageView
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().filesDir, "foto_perfil.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            imageViewPerfil.setImageURI(Uri.fromFile(file))

            val prefs = requireActivity().getSharedPreferences("perfil", Context.MODE_PRIVATE)
            prefs.edit().putString("foto_path", file.absolutePath).apply()
        }
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        editTextNombre = view.findViewById(R.id.editTextNombre)
        textViewFecha = view.findViewById(R.id.textViewFechaRegistro)
        textViewEstadisticas = view.findViewById(R.id.textViewEstadisticas)
        buttonGuardar = view.findViewById(R.id.buttonGuardar)

        val prefs = requireActivity().getSharedPreferences("perfil", Context.MODE_PRIVATE)
        val nombre = prefs.getString("nombre", "")
        val fechaRegistro = prefs.getLong("fecha", System.currentTimeMillis())

        editTextNombre.setText(nombre)
        textViewFecha.text = "Fecha de registro: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fechaRegistro))}"

        buttonGuardar.setOnClickListener {
            val nuevoNombre = editTextNombre.text.toString()
            prefs.edit().putString("nombre", nuevoNombre).apply()
            Toast.makeText(requireContext(), "Nombre guardado", Toast.LENGTH_SHORT).show()
        }

        imageViewPerfil = view.findViewById(R.id.imageViewPerfil)

        // Cargar imagen guardada si existe
        val fotoPath = prefs.getString("foto_path", null)
        fotoPath?.let {
            val file = File(it)
            if (file.exists()) {
                imageViewPerfil.setImageURI(Uri.fromFile(file))
            }
        }


        // Evento: al hacer clic, abrir galería
        imageViewPerfil.setOnClickListener {
            pickImage.launch("image/*")
        }
        val buttonEliminarFoto = view.findViewById<Button>(R.id.buttonEliminarFoto)

        buttonEliminarFoto.setOnClickListener {
            imageViewPerfil.setImageResource(R.drawable.ic_photo) // Imagen predeterminada
            val file = File(requireContext().filesDir, "foto_perfil.jpg")
            if (file.exists()) file.delete()
            prefs.edit { remove("foto_path") }
            imageViewPerfil.setImageResource(R.drawable.ic_profile)

            Toast.makeText(requireContext(), "Foto eliminada", Toast.LENGTH_SHORT).show()
        }


        contarReportes()
    }

    private fun contarReportes() {
        FirebaseFirestore.getInstance().collection("reportes")
            .get()
            .addOnSuccessListener { result ->
                textViewEstadisticas.text = "Reportes enviados: ${result.size()}"
            }
    }
}
