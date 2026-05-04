package br.com.ygor.microredesocialygor.ui

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import br.com.ygor.microredesocialygor.databinding.ActivityCreatePostBinding
import br.com.ygor.microredesocialygor.model.Post
import br.com.ygor.microredesocialygor.util.Base64Converter
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var imagemDrawable: Drawable? = null
    private var cidadeAtual: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private val LOCATION_PERMISSION_CODE = 1001

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            binding.imgPost.setImageURI(uri)
            imagemDrawable = binding.imgPost.drawable
        } else {
            Toast.makeText(this, "Nenhuma imagem selecionada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupListeners()
        verificarPermissaoLocalizacao()
    }

    private fun setupListeners() {
        binding.cardImagem.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnPublicar.setOnClickListener {
            publicarPost()
        }

        binding.btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun verificarPermissaoLocalizacao() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        } else {
            obterLocalizacao()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obterLocalizacao()
            } else {
                binding.txtCidade.text = "Localização não permitida"
            }
        }
    }

    private fun obterLocalizacao() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                obterCidadePorCoordenadas(latitude, longitude)
            } else {
                binding.txtCidade.text = "Não foi possível obter localização"
            }
        }
    }

    private fun obterCidadePorCoordenadas(lat: Double, lng: Double) {
        val geocoder = android.location.Geocoder(this, Locale.getDefault())
        try {
            val enderecos = geocoder.getFromLocation(lat, lng, 1)
            if (enderecos != null && enderecos.isNotEmpty()) {
                cidadeAtual = enderecos[0].subAdminArea ?: enderecos[0].locality ?: "Local desconhecido"
                binding.txtCidade.text = cidadeAtual
            } else {
                binding.txtCidade.text = "Cidade não identificada"
            }
        } catch (e: Exception) {
            binding.txtCidade.text = "Cidade não identificada"
        }
    }

    private fun buscarDadosUsuario(callback: (String, String) -> Unit) {
        val email = firebaseAuth.currentUser?.email ?: return

        db.collection("usuarios")
            .document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    if (data != null) {
                        val username = data["username"].toString()
                        val fotoPerfil = data["fotoPerfil"].toString()
                        callback(username, fotoPerfil)
                    } else {
                        callback("Usuário", "")
                    }
                } else {
                    callback("Usuário", "")
                }
            }
            .addOnFailureListener {
                callback("Usuário", "")
            }
    }

    private fun publicarPost() {
        val descricao = binding.edtDescricao.text.toString().trim()

        if (imagemDrawable == null) {
            Toast.makeText(this, "Selecione uma imagem", Toast.LENGTH_SHORT).show()
            return
        }

        if (descricao.isEmpty()) {
            Toast.makeText(this, "Digite uma descrição", Toast.LENGTH_SHORT).show()
            return
        }

        mostrarProgresso(true)

        buscarDadosUsuario { nomeUsuario, fotoUsuario ->
            val imagemBase64 = Base64Converter.drawableToStringPost(imagemDrawable!!)
            val userId = firebaseAuth.currentUser?.uid ?: ""

            salvarPostNoFirestore(imagemBase64, userId, nomeUsuario, fotoUsuario, descricao)
        }
    }

    private fun salvarPostNoFirestore(
        imagemBase64: String,
        userId: String,
        authorName: String,
        authorPhoto: String,
        descricao: String
    ) {
        val postId = UUID.randomUUID().toString()
        val post = Post(
            id = postId,
            userId = userId,
            authorName = authorName,
            authorPhoto = authorPhoto,
            imageUrl = imagemBase64,
            descricao = descricao,
            cidade = cidadeAtual.lowercase(),
            latitude = latitude,
            longitude = longitude,
            createdAt = System.currentTimeMillis()
        )

        db.collection("posts")
            .document(postId)
            .set(post)
            .addOnSuccessListener {
                mostrarProgresso(false)
                Toast.makeText(this, "Post publicado com sucesso!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                mostrarProgresso(false)
                Toast.makeText(this, "Erro ao publicar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun mostrarProgresso(mostrar: Boolean) {
        if (mostrar) {
            binding.progressBar.visibility = android.view.View.VISIBLE
            binding.btnPublicar.isEnabled = false
            binding.btnCancelar.isEnabled = false
        } else {
            binding.progressBar.visibility = android.view.View.GONE
            binding.btnPublicar.isEnabled = true
            binding.btnCancelar.isEnabled = true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}