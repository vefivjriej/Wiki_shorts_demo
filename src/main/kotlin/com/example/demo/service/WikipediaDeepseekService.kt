package com.example.demo.service

import com.example.demo.dto.WikiArticle
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.springframework.stereotype.Service
import java.net.URL

@Service
class WikipediaDeepseekService {
    private val client = OkHttpClient()

    fun fetchRandomArticle(): WikiArticle {
        // Получаем случайный заголовок статьи
        val randomTitleUrl = "https://en.wikipedia.org/api/rest_v1/page/random/title"
        val titleRequest = Request.Builder().url(randomTitleUrl).build()
        val titleResponse = client.newCall(titleRequest).execute()

        // Проверяем, что тело ответа не null
        val titleResponseBody = titleResponse.body?.string()
            ?: throw RuntimeException("Пустое тело ответа от API")

        // Парсим JSON
        val titleJson = JSONObject(titleResponseBody)

        // Извлекаем заголовок статьи
        val title = titleJson.getString("title")

        // Формируем URL статьи
        val articleApiUrl = "https://en.wikipedia.org/w/api.php?action =query&format=json&titles=$title&prop=extracts&explaintext"
        val articleRequest = Request.Builder().url(articleApiUrl).build()
        val articleResponse = client.newCall(articleRequest).execute()

        // Проверяем, что тело ответа не null
        val articleResponseBody = articleResponse.body?.string()
            ?: throw RuntimeException("Пустое тело ответа от API")

        // Парсим данные
        val articleJson = JSONObject(articleResponseBody)
        val pages = articleJson.getJSONObject("query").getJSONObject("pages")
        val pageId = pages.keys().next().toString()
        val pageContent = pages.getJSONObject(pageId).getString("extract")

        // Формируем объект WikiArticle
        return WikiArticle(
            summarizedText = pageContent,
            url = URL("https://en.wikipedia.org/wiki/$title"),
            originalTitle = title
        )
    }
}