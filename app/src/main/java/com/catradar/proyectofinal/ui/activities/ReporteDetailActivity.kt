package com.catradar.proyectofinal.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.catradar.proyectofinal.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.net.toUri
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response


class ReporteDetailActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var reporteId: String          // ID del documento
    private lateinit var dueñoUid: String           // UID del autor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_detail)

        db = FirebaseFirestore.getInstance("catradar")

        // ▼ Referencias de vistas
        val img = findViewById<ImageView>(R.id.imageViewDetalle)
        val tvTitulo = findViewById<TextView>(R.id.textViewTitulo)
        val tvEstado = findViewById<TextView>(R.id.textViewEstado)
        val tvDesc = findViewById<TextView>(R.id.textViewDescripcion)
        val tvUbi = findViewById<TextView>(R.id.textViewUbicacion)
        val tvFecha = findViewById<TextView>(R.id.textViewFecha)
        val btnEncontrado = findViewById<Button>(R.id.buttonMarcarEncontrado)
        val btnCorreo = findViewById<Button>(R.id.buttonEnviarCorreo)
        val btnRegresar = findViewById<Button>(R.id.buttonRegresar)


        // ▼ Datos recibidos
        val bundle = intent.extras ?: return
        tvTitulo.text      = bundle.getString("titulo")
        tvDesc.text        = bundle.getString("descripcion")
        val fotoUrl        = bundle.getString("fotoUrl") ?: ""
        val lat            = bundle.getDouble("latitud")
        val lng            = bundle.getDouble("longitud")
        dueñoUid           = bundle.getString("uid") ?: ""
        reporteId = bundle.getString("reporteId") ?: ""


        tvUbi.text   = "Ubicación: $lat, $lng"
        tvFecha.text = bundle.getString("fecha") ?: ""

        Glide.with(this).load(fotoUrl).placeholder(R.drawable.ic_profile).into(img)

        // ▼ Marcar ENCONTRADO
        btnEncontrado.setOnClickListener {
            db.collection("reportes").document(reporteId)
                .update("estado", "encontrado")
                .addOnSuccessListener {
                    tvEstado.text = "Estado: encontrado"
                    Toast.makeText(this, "Marcado como encontrado", Toast.LENGTH_SHORT).show()
                    enviarNotificacionAReportante()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        }

        // ▼ Enviar correo al autor
        btnCorreo.setOnClickListener {
            obtenerCorreoDeUsuario { email ->
                if (email != null) {
                    val correo = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:$email".toUri()
                        putExtra(Intent.EXTRA_SUBJECT, "Sobre tu reporte en CatRadar")
                        putExtra(Intent.EXTRA_TEXT, "¡Hola! Quería comentarte sobre el gato que reportaste…")
                    }
                    startActivity(Intent.createChooser(correo, "Enviar correo con…"))
                } else {
                    Toast.makeText(this, "No se pudo obtener correo", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnRegresar.setOnClickListener { finish() }
    }

    /** Obtiene el e-mail del usuario dueño del reporte */
    private fun obtenerCorreoDeUsuario(onResult: (String?) -> Unit) {
        if (dueñoUid.isBlank()) return onResult(null)
        db.collection("usuarios").document(dueñoUid).get()
            .addOnSuccessListener { doc ->
                onResult(doc.getString("email"))
            }
            .addOnFailureListener { onResult(null) }
    }

    /** Envía una notificación FCM al autor (requiere Cloud Function o servidor propio) */
    private fun enviarNotificacionAReportante() {
        if (dueñoUid.isBlank()) return
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Paso 1: Obtener el token FCM del usuario dueño del reporte
        db.collection("usuarios").document(dueñoUid).get()
            .addOnSuccessListener { doc ->
                val token = doc.getString("fcmToken")
                if (!token.isNullOrBlank()) {
                    // Paso 2: Enviar notificación HTTP a la Cloud Function
                    val url = "https://us-central1-catradar-7f937.cloudfunctions.net/enviarNotificacion"

                    val json = """
                    {
                        "token": "$token",
                        "title": "¡Tu gato ha sido encontrado!",
                        "body": "Alguien ha marcado tu reporte como encontrado."
                    }
                """.trimIndent()

                    val requestBody = okhttp3.RequestBody.create(
                        "application/json".toMediaTypeOrNull(), json
                    )

                    val request = okhttp3.Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                    val client = okhttp3.OkHttpClient()
                    client.newCall(request).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: IOException) {
                            runOnUiThread {
                                Toast.makeText(this@ReporteDetailActivity, "Error al enviar notificación", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            runOnUiThread {
                                if (response.isSuccessful) {
                                    Toast.makeText(this@ReporteDetailActivity, "Notificación enviada", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@ReporteDetailActivity, "Falló envío: ${response.code}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
                } else {
                    Toast.makeText(this, "Usuario no tiene token FCM", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo obtener token del usuario", Toast.LENGTH_SHORT).show()
            }
    }

}
