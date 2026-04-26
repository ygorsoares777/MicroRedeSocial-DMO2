package br.com.ygor.microredesocialygor.dao

import com.google.firebase.firestore.FirebaseFirestore
import br.com.ygor.microredesocialygor.model.User

class UserDAO {
    private val db = FirebaseFirestore.getInstance()
    private val COLLECTION = "usuarios"

    fun salvar(user: User, callback: (Boolean, String?) -> Unit) {
        db.collection(COLLECTION)
            .document(user.email)
            .set(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun buscarPorEmail(email: String, callback: (User?, String?) -> Unit) {
        db.collection(COLLECTION)
            .document(email)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.toObject(User::class.java)
                    callback(user, null)
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }
}