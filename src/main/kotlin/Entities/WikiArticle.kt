package Entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.net.URL

@Entity
data class WikiArticle(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val Summerizearticle:String,
    val topics: List<String>,
    val Url: URL,
    val originalTitle: String,
)
