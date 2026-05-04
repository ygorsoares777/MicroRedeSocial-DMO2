package br.com.ygor.microredesocialygor.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import br.com.ygor.microredesocialygor.databinding.ActivityHomeBinding
import br.com.ygor.microredesocialygor.model.Post
import br.com.ygor.microredesocialygor.ui.adapter.PostAdapter
import br.com.ygor.microredesocialygor.util.Base64Converter

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    private lateinit var adapter: PostAdapter
    private val postsList = mutableListOf<Post>()
    private var lastDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    private var isLoading = false
    private var isSearching = false
    private var searchCity = ""
    private val POSTS_PER_PAGE = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        setupRecyclerView()
        setupSearch()
        carregarDadosPerfil()
        carregarPosts()

        binding.btnAdicionarPost.setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }

        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnSair.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(postsList)
        binding.recyclerViewFeed.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFeed.adapter = adapter

        binding.recyclerViewFeed.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isSearching) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

                    if (!isLoading && lastVisiblePosition >= totalItemCount - 1 && totalItemCount >= POSTS_PER_PAGE) {
                        carregarPosts()
                    }
                }
            }
        })
    }

    private fun setupSearch() {
        binding.btnBuscar.setOnClickListener {
            val cidade = binding.edtBuscarCidade.text.toString().trim()
            if (cidade.isNotEmpty()) {
                searchCity = cidade
                isSearching = true
                buscarPostsPorCidade(cidade)
            } else {
                Toast.makeText(this, "Digite uma cidade", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLimparBusca.setOnClickListener {
            binding.edtBuscarCidade.text.clear()
            isSearching = false
            searchCity = ""
            limparFeed()
            carregarPosts()
            Toast.makeText(this, "Feed atualizado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buscarPostsPorCidade(cidade: String) {
        mostrarProgresso(true)
        postsList.clear()
        adapter.clear()
        lastDocument = null


        val cidadeBusca = cidade.lowercase().trim()

        db.collection("posts")
            .whereEqualTo("cidade", cidadeBusca)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(POSTS_PER_PAGE.toLong())
            .get()
            .addOnSuccessListener { documents ->
                val novosPosts = documents.toObjects(Post::class.java)
                if (novosPosts.isNotEmpty()) {
                    lastDocument = documents.documents.lastOrNull()
                    adapter.addPosts(novosPosts)
                    Toast.makeText(this, "${novosPosts.size} posts encontrados", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Nenhum post encontrado em $cidade", Toast.LENGTH_SHORT).show()
                }
                mostrarProgresso(false)
            }
            .addOnFailureListener {exception ->
                mostrarProgresso(false)
                Log.e("BUSCA_POSTS", "Erro na busca", exception)
                Toast.makeText(this, "Erro na busca", Toast.LENGTH_SHORT).show()
            }
    }

    private fun carregarPosts() {
        if (isLoading) return
        isLoading = true
        mostrarProgresso(true)

        var query = db.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(POSTS_PER_PAGE.toLong())

        lastDocument?.let {
            query = query.startAfter(it)
        }

        query.get()
            .addOnSuccessListener { documents ->
                val novosPosts = documents.toObjects(Post::class.java)
                if (novosPosts.isNotEmpty()) {
                    lastDocument = documents.documents.last()
                    adapter.addPosts(novosPosts)
                }
                isLoading = false
                mostrarProgresso(false)
            }
            .addOnFailureListener {
                isLoading = false
                mostrarProgresso(false)
                Toast.makeText(this, "Erro ao carregar posts", Toast.LENGTH_SHORT).show()
            }
    }

    private fun carregarDadosPerfil() {
        val email = firebaseAuth.currentUser?.email ?: return

        db.collection("usuarios")
            .document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    if (data != null) {
                        val nomeCompleto = data["nomeCompleto"].toString()
                        val username = data["username"].toString()

                        binding.txtNomeCompleto.text = nomeCompleto
                        binding.txtUsername.text = username

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
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar perfil", Toast.LENGTH_LONG).show()
            }
    }

    private fun limparFeed() {
        postsList.clear()
        adapter.clear()
        lastDocument = null
    }

    private fun mostrarProgresso(mostrar: Boolean) {
        binding.progressBar.visibility = if (mostrar) android.view.View.VISIBLE else android.view.View.GONE
    }

    override fun onResume() {
        super.onResume()
        if (!isSearching) {
            limparFeed()
            carregarPosts()
        }
        carregarDadosPerfil()
    }
}