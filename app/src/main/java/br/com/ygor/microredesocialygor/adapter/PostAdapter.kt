package br.com.ygor.microredesocialygor.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.ygor.microredesocialygor.databinding.ItemPostBinding
import br.com.ygor.microredesocialygor.model.Post
import br.com.ygor.microredesocialygor.util.Base64Converter
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val posts: MutableList<Post>
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    fun addPosts(novosPosts: List<Post>) {
        val posicaoAtual = posts.size
        posts.addAll(novosPosts)
        notifyItemRangeInserted(posicaoAtual, novosPosts.size)
    }

    fun clear() {
        posts.clear()
        notifyDataSetChanged()
    }

    inner class PostViewHolder(
        private val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.txtAuthorName.text = post.authorName
            binding.txtDescricao.text = post.descricao
            binding.txtCidade.text = post.cidade

            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.txtCreatedAt.text = dateFormat.format(Date(post.createdAt))

            if (post.imageUrl.isNotEmpty()) {
                try {
                    val bitmap = Base64Converter.stringToBitmap(post.imageUrl)
                    binding.imgPost.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    binding.imgPost.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }

            if (post.authorPhoto.isNotEmpty()) {
                try {
                    val bitmap = Base64Converter.stringToBitmap(post.authorPhoto)
                    binding.imgAuthorPhoto.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    // Mantém imagem padrão
                }
            }
        }
    }
}