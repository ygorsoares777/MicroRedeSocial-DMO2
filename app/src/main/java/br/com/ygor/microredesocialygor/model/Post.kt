package br.com.ygor.microredesocialygor.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorPhoto: String = "",
    val imageUrl: String = "",  // ✅ Será Base64
    val descricao: String = "",
    val cidade: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdAt: Long = 0L
)