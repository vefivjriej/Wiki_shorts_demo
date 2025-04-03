package Service
import Entities.WikiArticle
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.*
import java.net.URL


@Service
class WikipediaDeepseekService {
    private val client = OkHttpClient()
    private val deepSeekApiUrl = "https://api.deepseek.com/chat/completions"
    private val mediaType = "application/json".toMediaType()

    @Value("\${deepseek.api.key}")
    private lateinit var apiKey: String
    fun fetchRandomArticle(): WikiArticle {
        val randomTitleUrl = "https://en.wikipedia.org/api/rest_v1/page/random/title"
        val titleRequest = Request.Builder().url(randomTitleUrl).build()
        val titleResponse = client.newCall(titleRequest).execute()
        val titleJson = JSONObject(titleResponse.body?.string())
        val title = titleJson.getString("title")

        // 2. Формируем URL статьи
        val articleUrl_demo= "https://en.wikipedia.org/wiki/${title.replace(" ", "_")}"
        val articleUrl = URL(articleUrl_demo)

        // 3. Получаем текст и категории статьи
        val articleApiUrl =
            "https://en.wikipedia.org/w/api.php?action=query&format=json&titles=$title&prop=extracts|categories&explaintext"
        val articleRequest = Request.Builder().url(articleApiUrl).build()
        val articleResponse = client.newCall(articleRequest).execute()
        val articleJson = JSONObject(articleResponse.body?.string())

        // Парсим данные
        val pages = articleJson.getJSONObject("query").getJSONObject("pages")
        val pageId = pages.keys().next().toString()
        val pageData = pages.getJSONObject(pageId)
        val fullText = pageData.getString("extract")

        // Парсим категории
        val categories = pageData.optJSONArray("categories")?.let { jsonArray ->
            (0 until jsonArray.length()).map { i ->
                jsonArray.getJSONObject(i).getString("title").removePrefix("Category:")
            }
        } ?: emptyList()
        val text = fullText
        val requestBody = """
            {
                "model": "deepseek-chat",
                "messages": [
                    {
                        "role": "system",
                        "content": "Ты помощник, который сокращает текст до 170 слов, сохраняя суть. Не добавляй лишней информации."
                    },
                    {
                        "role": "user",
                        "content": "Сократи этот текст до максимум 170 слов:\n\n$text"
                    }
                ],
                "temperature": 0.4,
                "max_tokens": 300,
                "stream": false
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(deepSeekApiUrl)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody(mediaType))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw RuntimeException("DeepSeek API не ответил")

        // Парсим ответ
        val content = try {
            JSONObject(responseBody)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        } catch (e: Exception) {
            "Ошибка парсинга: ${e.message}"
        }
        return WikiArticle(
            Summerizearticle = content,
            topics = categories,
            Url = articleUrl,
            originalTitle = title
        )
    }
}
