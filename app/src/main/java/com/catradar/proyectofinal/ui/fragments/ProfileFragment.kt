package com.catradar.proyectofinal.ui.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.catradar.proyectofinal.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.edit
import com.catradar.proyectofinal.auth.AuthManager
import java.io.File
import java.io.FileOutputStream
import com.bumptech.glide.Glide
import com.catradar.proyectofinal.ui.activities.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private lateinit var editTextNombre: EditText
    private lateinit var textViewFecha: TextView
    private lateinit var textViewEstadisticas: TextView
    private lateinit var buttonGuardar: Button
    private lateinit var tvEmail: TextView

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
            prefs.edit { putString("foto_path", file.absolutePath) }
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
        ): View {
            val view = inflater.inflate(R.layout.fragment_profile, container, false)

            imageViewPerfil = view.findViewById(R.id.imageViewPerfil)
            editTextNombre = view.findViewById(R.id.editTextNombre)
            tvEmail = view.findViewById(R.id.tvEmail)

            cargarPerfil()
            val btnCerrarSesion = view.findViewById<Button>(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            AuthManager.logout(requireContext()) {
                val prefs = requireContext().getSharedPreferences("perfil", Context.MODE_PRIVATE)
                prefs.edit { clear() }

                FirebaseAuth.getInstance().signOut()
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }
        }

        return view
    }

    private fun cargarPerfil() {
        AuthManager.getUserProfile { data ->
            if (data != null) {
                val nombre = data["nombre"] as? String ?: ""
                editTextNombre.setText(nombre)

                val correo = data["email"] as? String ?: ""
                tvEmail.text = correo

                val fotoUrl = data["fotoUrl"] as? String
                if (!fotoUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(fotoUrl)
                        .into(imageViewPerfil)
                }
            } else {
                Toast.makeText(requireContext(), "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show()
            }
        }
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
            val nombre = editTextNombre.text.toString()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            val prefs = requireActivity().getSharedPreferences("perfil", Context.MODE_PRIVATE)
            val fotoPath = prefs.getString("foto_path", null)

            if (fotoPath != null) {
                val file = File(fotoPath)
                if (file.exists()) {
                    val uri = Uri.fromFile(file)
                    val storageRef = FirebaseStorage.getInstance().reference
                    val imageRef = storageRef.child("perfiles/$userId.jpg")

                    imageRef.putFile(uri)
                        .continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let { throw it }
                            }
                            imageRef.downloadUrl
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val fotoUrl = task.result.toString()
                                val perfil = mapOf(
                                    "nombre" to nombre,
                                    "fotoUrl" to fotoUrl
                                )
                                FirebaseFirestore.getInstance("catradar")
                                    .collection("usuarios")
                                    .document(userId)
                                    .set(perfil, SetOptions.merge())
                                    .addOnSuccessListener {
                                        // Mostrar imagen nueva directamente con Glide
                                        Glide.with(this)
                                            .load(fotoUrl)
                                            .into(imageViewPerfil)

                                        Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                // Si no hay imagen nueva, solo guarda el nombre
                val perfil = mapOf(
                    "nombre" to nombre
                )
                FirebaseFirestore.getInstance("catradar")
                    .collection("usuarios")
                    .document(userId)
                    .set(perfil, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
                    }
            }
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance("catradar").collection("reportes")
            .whereEqualTo("uid", userId)
            .get()
            .addOnSuccessListener { result ->
                textViewEstadisticas.text = "Reportes enviados: ${result.size()}"
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al contar reportes", Toast.LENGTH_SHORT).show()
            }
    }

}
