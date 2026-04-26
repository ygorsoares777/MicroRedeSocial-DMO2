package br.com.ygor.microredesocialygor.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.com.ygor.microredesocialygor.auth.UserAuth
import br.com.ygor.microredesocialygor.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val userAuth = UserAuth()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RF1-4: Auto-redirecionamento se já estiver logado
        if (userAuth.isUserLoggedIn()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            autenticarUsuario()
        }
        binding.btnCriarConta.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun autenticarUsuario() {
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtSenha.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        userAuth.login(email, password) { sucesso ->
            if (sucesso) {
                startActivity(Intent(this, HomeActivity::class.java))
                Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Email ou senha incorretos", Toast.LENGTH_LONG).show()
            }
        }
    }
}