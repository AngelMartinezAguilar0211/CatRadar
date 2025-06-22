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
        val reporte = reportes[position]
        holder.bind(reporte)
        holder.eliminarButton.setOnClickListener {
            onDelete(reporte)
        }
    }

    override fun getItemCount(): Int = reportes.size

    class ReporteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foto: ImageView = itemView.findViewById(R.id.imageViewFoto)
        private val descripcion: TextView = itemView.findViewById(R.id.textViewDescripcion)
        private val fecha: TextView = itemView.findViewById(R.id.textViewFecha)
        private val titulo: TextView = itemView.findViewById(R.id.textViewTitulo)
        private val estado: TextView = itemView.findViewById(R.id.textViewEstado)
        val eliminarButton: ImageButton = itemView.findViewById(R.id.buttonEliminar)

        fun bind(reporte: Reporte) {
            titulo.text = reporte.titulo
            descripcion.text = reporte.descripcion
            estado.text = "Estado: ${reporte.estado}"

            // Formato de fecha
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fechaTexto = reporte.fecha?.toDate()?.let { sdf.format(it) } ?: "Fecha desconocida"
            fecha.text = "Fecha: $fechaTexto"

            // Cargar imagen con Glide
            Glide.with(itemView)
                .load(reporte.fotoUrl)
                .placeholder(R.drawable.ic_profile)
                .into(foto)
        }
    }
}
