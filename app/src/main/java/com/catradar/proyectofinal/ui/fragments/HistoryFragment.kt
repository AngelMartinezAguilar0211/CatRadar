package com.catradar.proyectofinal.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.catradar.proyectofinal.R
import com.catradar.proyectofinal.model.Reporte
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val listaReportes = mutableListOf<Reporte>()
    private lateinit var textoSinReportes: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewHistorial)
        textoSinReportes = view.findViewById(R.id.textViewSinReportes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ReporteAdapter(listaReportes) { reporte ->
            eliminarReporte(reporte)
        }

        cargarReportes()
    }

    private fun cargarReportes() {
        val usuarioActual = FirebaseAuth.getInstance().currentUser
        if (usuarioActual == null) {
            Toast.makeText(requireContext(), "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance("catradar").collection("reportes")
            .whereEqualTo("uid", usuarioActual.uid)
            .get()
            .addOnSuccessListener { result ->
                listaReportes.clear()
                for (document in result) {
                    val reporte = document.toObject(Reporte::class.java).copy(id = document.id)
                    listaReportes.add(reporte)
                }
                if(listaReportes.isEmpty()) {
                    textoSinReportes.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    textoSinReportes.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }

                recyclerView.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar reportes", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarReporte(reporte: Reporte) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("¿Eliminar reporte?")
        builder.setMessage("¿Estás segura(o) de que deseas eliminar este reporte? Esta acción no se puede deshacer.")
        builder.setPositiveButton("Eliminar") { dialog, _ ->
            FirebaseFirestore.getInstance("catradar").collection("reportes")
                .document(reporte.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Reporte eliminado", Toast.LENGTH_SHORT).show()
                    listaReportes.remove(reporte)
                    recyclerView.adapter?.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al eliminar el reporte", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }


}
