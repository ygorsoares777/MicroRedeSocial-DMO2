package br.com.ygor.microredesocialygor.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import br.com.ygor.microredesocialygor.databinding.ActivityProfileBinding
import br.com.ygor.microredesocialygor.util.Base64Converter

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    private val galeria = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.imgFotoPerfil.setImageURI(uri)
        } else {
            Toast.makeText(this, "Nenhuma foto selecionada", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        carregarDadosAtuais()
        setupListeners()
    }

    private fun carregarDadosAtuais() {
        val email = firebaseAuth.currentUser?.email ?: return

        db.collection("usuarios")
            .document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    if (data != null) {
                        binding.edtUsername.setText(data["username"].toString())
                        binding.edtNomeCompleto.setText(data["nomeCompleto"].toString())
                    }
                }
            }
    }

    private fun setupListeners() {
        binding.btnAlterarFoto.setOnClickListener {
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSalvar.setOnClickListener {
            salvarPerfil()
        }

        binding.btnSair.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun salvarPerfil() {
        val email = firebaseAuth.currentUser?.email
        val username = binding.edtUsername.text.toString().trim()
        val nomeCompleto = binding.edtNomeCompleto.text.toString().trim()

        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "Erro: usuário não logado", Toast.LENGTH_LONG).show()
            return
        }

        if (username.isEmpty() || nomeCompleto.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val fotoPerfilString = try {
            Base64Converter.drawableToString(binding.imgFotoPerfil.drawable)
        } catch (e: Exception) {
            ""
        }

        val dados = hashMapOf(
            "username" to username,
            "nomeCompleto" to nomeCompleto,
            "fotoPerfil" to fotoPerfilString,
            "email" to email
        )

        db.collection("usuarios")
            .document(email)
            .set(dados)
            .addOnSuccessListener {
                Toast.makeText(this, "Perfil salvo com sucesso!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}