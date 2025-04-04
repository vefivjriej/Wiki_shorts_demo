package com.example.demo.Controller

class Registration {
    //раф пиши
}
//        val requestBody = """
//            {
//                "model": "deepseek-chat",
//                "messages": [
//                    {
//                        "role": "system",
//                        "content": "Ты помощник, который сокращает текст до 170 слов, сохраняя суть. Не добавляй лишней информации."
//                    },
//                    {
//                        "role": "user",
//                        "content": "Сократи этот текст до максимум 170 слов:\n\n$text"
//                    }
//                ],
//                "temperature": 0.4,
//                "max_tokens": 300,
//                "stream": false
//            }
//        """.trimIndent()
//
//        val request = Request.Builder()
//            .url(deepSeekApiUrl)
//            .addHeader("Content-Type", "application/json")
//            .addHeader("Authorization", "Bearer $apiKey")
//            .post(requestBody.toRequestBody(mediaType))
//            .build()
//
//        val response = client.newCall(request).execute()
//        val responseBody = response.body?.string() ?: throw RuntimeException("DeepSeek API не ответил")
//
//        // Парсим ответ
//        val content = try {
//            JSONObject(responseBody)
//                .getJSONArray("choices")
//                .getJSONObject(0)
//                .getJSONObject("message")
//                .getString("content")
//        } catch (e: Exception) {
//            "Ошибка парсинга: ${e.message}"
//        }