package com.catradar.proyectofinal.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.catradar.proyectofinal.R
import com.catradar.proyectofinal.model.Reporte
import com.google.firebase.firestore.FirebaseFirestore

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val listaReportes = mutableListOf<Reporte>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewHistorial)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ReporteAdapter(listaReportes) { reporte ->
            eliminarReporte(reporte)
        }

        cargarReportes()
    }

    private fun cargarReportes() {
        FirebaseFirestore.getInstance().collection("reportes")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                listaReportes.clear()
                for (document in result) {
                    val reporte = document.toObject(Reporte::class.java).copy(id = document.id)
                    listaReportes.add(reporte)
                }

                recyclerView.adapter?.notifyDataSetChanged()
            }
    }
    private fun eliminarReporte(reporte: Reporte) {
        FirebaseFirestore.getInstance().collection("reportes")
            .document(reporte.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Reporte eliminado", Toast.LENGTH_SHORT).show()
                listaReportes.remove(reporte)
                recyclerView.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al eliminar reporte", Toast.LENGTH_SHORT).show()
            }
    }

}
