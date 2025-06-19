package com.catradar.proyectofinal.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.catradar.proyectofinal.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var editTextNombre: EditText
    private lateinit var textViewFecha: TextView
    private lateinit var textViewEstadisticas: TextView
    private lateinit var buttonGuardar: Button

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
