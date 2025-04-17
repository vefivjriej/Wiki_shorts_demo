package com.example.demo.Service
import com.example.demo.DTO.WikiArticle
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

        // Проверяем наличие массива "items"
        if (!titleJson.has("items")) {
            throw RuntimeException("Ключ 'items' не найден в ответе API: $titleResponseBody")
        }
        // Извлекаем массив "items"
        val itemsArray = titleJson.getJSONArray("items")

        // Проверяем, что массив не пустой
        if (itemsArray.length() == 0) {
            throw RuntimeException("Массив 'items' пуст в ответе API: $titleResponseBody")
        }

        // Извлекаем первый элемент массива
        val firstItem = itemsArray.getJSONObject(0)
        // Проверяем наличие ключа "title" в первом элементе
        if (!firstItem.has("title")) {
            throw RuntimeException("Ключ 'title' не найден в первом элементе массива 'items': $firstItem")
        }

        val title = firstItem.getString("title")

        // Формируем URL статьи
        val articleApiUrl = "https://en.wikipedia.org/w/api.php?action=query&format=json&titles=$title&prop=extracts&explaintext"
        val articleRequest = Request.Builder().url(articleApiUrl).build()
        val articleResponse = client.newCall(articleRequest).execute()

        // Проверяем, что тело ответа не null
        val articleResponseBody = articleResponse.body?.string()
            ?: throw RuntimeException("Пустое тело ответа от API")
        // Парсим данные
        val articleJson = JSONObject(articleResponseBody)
        val pages = articleJson.getJSONObject("query").getJSONObject("pages")
        val pageId = pages.keys().next().toString()
        val pageData = pages.getJSONObject(pageId)
        // Проверяем наличие ключа "extract"
        if (!pageData.has("extract")) {
            throw RuntimeException("Ключ 'extract' не найден в ответе API: $articleResponseBody")
        }
        val fullText = pageData.getString("extract")
        println(pageData)
        // Формируем обычную ссылку на статью
        val articleUrl = URL("https://en.wikipedia.org/wiki/${title.replace(" ", "_")}")
        val content = fullText
        return WikiArticle(content, articleUrl, title)
    }
}//