package com.example.demo.Service

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class DeepSeekService {
    private val client = OkHttpClient()
    private val deepSeekApiUrl = "https://api.deepseek.com/chat/completions"
    private val mediaType = "application/json".toMediaType()

    @Value("\${deepseek.api.key}")
    private lateinit var apiKey: String

    fun shortenText(text: String): String {
        val json = JSONObject()
        json.put("model", "deepseek-R1")
        json.put("prompt", "Сократи следующий текст до 170 слов: $text")
        json.put("max_tokens", 150)

        val requestBody = json.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(deepSeekApiUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw RuntimeException("Пустое тело ответа от DeepSeek API")

        val responseJson = JSONObject(responseBody)
        return responseJson.getJSONArray("choices").getJSONObject(0).getString("text").trim()
    }
}