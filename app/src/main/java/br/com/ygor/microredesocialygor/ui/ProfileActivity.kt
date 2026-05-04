package br.com.ygor.microredesocialygor.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import br.com.ygor.microredesocialygor.auth.UserAuth
import br.com.ygor.microredesocialygor.databinding.ActivityProfileBinding
import br.com.ygor.microredesocialygor.util.Base64Converter

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val userAuth = UserAuth()
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


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

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

                        val fotoString = data["fotoPerfil"].toString()
                        if (fotoString.isNotEmpty()) {
                            try {
                                val bitmap = Base64Converter.stringToBitmap(fotoString)
                                binding.imgFotoPerfil.setImageBitmap(bitmap)
                            } catch (e: Exception) {

                            }
                        }
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
        val senhaAtual = binding.edtSenhaAtual.text.toString().trim()
        val novaSenha = binding.edtNovaSenha.text.toString().trim()
        val confirmarSenha = binding.edtConfirmarSenha.text.toString().trim()

        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "Erro: usuário não logado", Toast.LENGTH_LONG).show()
            return
        }

        if (username.isEmpty() || nomeCompleto.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Se vai alterar senha, precisa da senha atual e reautenticação
        if (novaSenha.isNotEmpty()) {
            if (senhaAtual.isEmpty()) {
                Toast.makeText(this, "Digite a senha atual para alterar", Toast.LENGTH_SHORT).show()
                return
            }
            if (novaSenha != confirmarSenha) {
                Toast.makeText(this, "As novas senhas não coincidem", Toast.LENGTH_SHORT).show()
                return
            }
            if (novaSenha.length < 6) {
                Toast.makeText(this, "A nova senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return
            }

            // Reautenticar antes de trocar a senha
            reautenticarUsuario(senhaAtual) { sucesso ->
                if (sucesso) {
                    userAuth.atualizarSenha(novaSenha) { alterou, erro ->
                        if (alterou) {
                            Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Erro: ${erro ?: "Não foi possível alterar"}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Senha atual incorreta", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Salvar dados no Firestore
        salvarDadosFirestore(email, username, nomeCompleto)
    }

    private fun salvarDadosFirestore(email: String, username: String, nomeCompleto: String) {
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
                Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun reautenticarUsuario(senha: String, callback: (Boolean) -> Unit) {
        val user = firebaseAuth.currentUser
        val email = user?.email ?: return
        val credential = EmailAuthProvider.getCredential(email, senha)

        user.reauthenticate(credential)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    // Botão de voltar da Toolbar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}