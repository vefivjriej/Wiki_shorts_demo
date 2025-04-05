package com.example.demo.DTO
import java.net.URL
data class WikiArticle(
    val summarizedText: String,
    val url: URL,
    val originalTitle: String,
)