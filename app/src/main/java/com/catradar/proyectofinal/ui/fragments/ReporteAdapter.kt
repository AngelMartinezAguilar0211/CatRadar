package com.catradar.proyectofinal.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.catradar.proyectofinal.R
import com.catradar.proyectofinal.model.Reporte
import java.text.SimpleDateFormat
import java.util.*

class ReporteAdapter(
    private val reportes: MutableList<Reporte>,
    private val onDelete: (Reporte) -> Unit
) : RecyclerView.Adapter<ReporteAdapter.ReporteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReporteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reporte, parent, false)
        return ReporteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReporteViewHolder, position: Int) {
        holder.bind(reportes[position])
        holder.itemView.findViewById<ImageButton>(R.id.buttonEliminar).setOnClickListener {
            onDelete(reportes[position])
        }

    }

    override fun getItemCount(): Int = reportes.size

    class ReporteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foto: ImageView = itemView.findViewById(R.id.imageViewFoto)
        private val descripcion: TextView = itemView.findViewById(R.id.textViewDescripcion)
        private val fecha: TextView = itemView.findViewById(R.id.textViewFecha)
        private val titulo: TextView = itemView.findViewById(R.id.textViewTitulo)


        fun bind(reporte: Reporte) {
            descripcion.text = reporte.descripcion
            titulo.text = reporte.titulo
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            fecha.text = "Fecha: ${sdf.format(Date(reporte.fecha))}"
            Glide.with(itemView).load(reporte.fotoUrl).into(foto)
        }
    }
}
