package com.catradar.proyectofinal.ui.fragments

import android.os.Bundle
import android.view.*
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
        recyclerView.adapter = ReporteAdapter(listaReportes)

        cargarReportes()
    }

    private fun cargarReportes() {
        FirebaseFirestore.getInstance().collection("reportes")
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                listaReportes.clear()
                for (document in result) {
                    val reporte = document.toObject(Reporte::class.java)
                    listaReportes.add(reporte)
                }
                recyclerView.adapter?.notifyDataSetChanged()
            }
    }
}
