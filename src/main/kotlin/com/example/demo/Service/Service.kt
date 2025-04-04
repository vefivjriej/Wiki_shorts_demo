package com.example.demo.Service

import com.example.demo.DTO.WikiArticle
import jakarta.annotation.PostConstruct
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.URL

@Service
class WikipediaDeepseekService {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val client = OkHttpClient()
    private val mediaType = "application/json".toMediaType()

    @Value("\${deepseek.api.key:}")
    private lateinit var apiKey: String

    @PostConstruct
    fun validateConfig() {
        if (apiKey.isBlank()) {
            logger.error("DeepSeek API key is not configured!")
            throw IllegalStateException("DeepSeek API key is required")
        }
    }

    fun fetchRandomArticle(): WikiArticle {
        logger.info("Starting article processing pipeline")

        return try {
            val title = fetchTitleWithRetry(3) ?: throw ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Could not fetch Wikipedia title"
            )

            logger.debug("Obtained article title: $title")

            val (content, categories) = fetchArticleContentWithRetry(title, 3)
            if (content.isBlank()) {
                throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Article content is empty"
                )
            }

            logger.debug("Fetched article content (${content.length} chars)")

            val summary = try {
                summarizeContent(content)
            } catch (e: Exception) {
                logger.warn("Using fallback summary due to DeepSeek error", e)
                content.take(200) + "..." // Fallback
            }

            WikiArticle(
                Summerizearticle = summary,
                topics = categories,
                Url = URL("https://en.wikipedia.org/wiki/${title.sanitizeForUrl()}"),
                originalTitle = title
            )
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            logger.error("Article processing failed", e)
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Article processing failed: ${e.message}"
            )
        }
    }

    private fun fetchTitleWithRetry(maxAttempts: Int): String? {
        var attempt = 0
        var lastError: Exception? = null

        while (attempt < maxAttempts) {
            try {
                val request = Request.Builder()
                    .url("https://en.wikipedia.org/api/rest_v1/page/random/title")
                    .header("Accept", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        logger.warn("Wikipedia title API responded with ${response.code}")
                        return null
                    }

                    val body = response.body?.string() ?: return null
                    return JSONObject(body).optString("title").takeIf { it.isNotBlank() }
                }
            } catch (e: Exception) {
                lastError = e
                logger.warn("Attempt ${attempt + 1} failed to fetch title", e)
                Thread.sleep(1000 * (attempt + 1).toLong())
            }
            attempt++
        }

        logger.error("Failed to fetch title after $maxAttempts attempts")
        lastError?.let { throw it }
        return null
    }

    private fun fetchArticleContentWithRetry(title: String, maxAttempts: Int): Pair<String, List<String>> {
        var attempt = 0
        var lastError: Exception? = null

        while (attempt < maxAttempts) {
            try {
                val url = "https://en.wikipedia.org/w/api.php?" +
                        "action=query&format=json&titles=${title.sanitizeForUrl()}" +
                        "&prop=extracts|categories&explaintext=1"

                val request = Request.Builder()
                    .url(url)
                    .header("Accept", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw ResponseStatusException(
                            HttpStatus.BAD_GATEWAY,
                            "Wikipedia API error: ${response.code}"
                        )
                    }

                    val body = response.body?.string()
                        ?: throw IllegalStateException("Empty Wikipedia response")

                    return parseWikipediaResponse(body)
                }
            } catch (e: Exception) {
                lastError = e
                logger.warn("Attempt ${attempt + 1} failed to fetch content", e)
                Thread.sleep(1000 * (attempt + 1).toLong())
            }
            attempt++
        }

        lastError?.let { throw it }
            ?: throw IllegalStateException("Failed to fetch content after $maxAttempts attempts")
    }

    private fun parseWikipediaResponse(json: String): Pair<String, List<String>> {
        return try {
            val root = JSONObject(json)
            val pages = root.getJSONObject("query").getJSONObject("pages")
            val pageId = pages.keys().next()
            val page = pages.getJSONObject(pageId)

            val content = page.optString("extract", "")
            val categories = page.optJSONArray("categories")?.let { cats ->
                (0 until cats.length()).mapNotNull { i ->
                    cats.optJSONObject(i)?.optString("title")?.removePrefix("Category:")
                }
            } ?: emptyList()

            content to categories
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Failed to parse Wikipedia response: ${e.message}"
            )
        }
    }

    private fun summarizeContent(text: String): String {
        return try {
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
                .url("https://api.deepseek.com/chat/completions")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $apiKey")
                .post(requestBody.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "DeepSeek API error: ${response.code}"
                    )
                }

                val body = response.body?.string()
                    ?: throw ResponseStatusException(
                        HttpStatus.NO_CONTENT,
                        "Empty DeepSeek response"
                    )

                parseDeepSeekResponse(body)
            }
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "DeepSeek service unavailable: ${e.message}"
            )
        }
    }

    private fun parseDeepSeekResponse(json: String): String {
        return try {
            JSONObject(json)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        } catch (e: Exception) {
            throw ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Invalid DeepSeek response format"
            )
        }
    }

    private fun String.sanitizeForUrl(): String {
        return this.replace(" ", "_")
    }
}