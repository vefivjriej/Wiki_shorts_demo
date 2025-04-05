package com.example.demo.controller

import com.example.demo.dto.WikiArticle
import com.example.demo.service.DeepSeekService
import com.example.demo.service.WikipediaDeepseekService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class MyController @Autowired constructor(
    private val deepSeekService: DeepSeekService,
    private val wikipediaDeepseekService: WikipediaDeepseekService
) {

    @GetMapping("/ai/shorten")
    fun shortenText(@RequestParam(value = "text") text: String): Map<String, String> {
        val shortenedText = deepSeekService.shortenText(text)
        return mapOf("shortenedText" to shortenedText)
    }

    @GetMapping("/article/random")
    fun getRandomArticle(): WikiArticle {
        return wikipediaDeepseekService.fetchRandomArticle()
    }
}