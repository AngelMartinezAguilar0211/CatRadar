package com.catradar.proyectofinal.auth

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.storage.FirebaseStorage
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessaging


object AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance("catradar")


    fun loginWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onResult(true, null)
                else onResult(false, task.exception?.message)
            }
    }

    fun registerWithEmail(email: String, password: String, nombre: String, onComplete: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid

                    uid?.let {
                        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                            val user = hashMapOf(
                                "email" to email,
                                "fotoUrl" to "",
                                "nombre" to nombre,
                                "preferenciasTema" to "sistema",
                                "fcmToken" to token
                            )
                            db.collection("usuarios").document(it)
                                .set(user)
                                .addOnSuccessListener { onComplete(true) }
                                .addOnFailureListener { onComplete(false) }
                        }
                    } ?: onComplete(false)
                } else {
                    onComplete(false)
                }
            }
    }


    fun loginWithGoogle(account: GoogleSignInAccount, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val docRef = db.collection("usuarios").document(it.uid)
                        docRef.get().addOnSuccessListener { doc ->
                            if (!doc.exists()) {
                                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                    val nuevoUsuario = mapOf(
                                        "nombre" to it.displayName,
                                        "email" to it.email,
                                        "fotoUrl" to (it.photoUrl?.toString() ?: ""),
                                        "preferenciasTema" to "sistema",
                                        "fcmToken" to token
                                    )
                                    docRef.set(nuevoUsuario)
                                }
                            }
                        }
                    }
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun logout(context: Context, onComplete: () -> Unit) {
        auth.signOut()
        clearLocalUserData(context)
        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        onComplete()
    }

    fun getUserProfile(onResult: (Map<String, Any>?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(null)
        db.collection("usuarios").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onResult(document.data)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun clearLocalUserData(context: Context) {
        val prefs = context.getSharedPreferences("CatRadarPrefs", Context.MODE_PRIVATE)
        prefs.edit { clear() }
    }

}
