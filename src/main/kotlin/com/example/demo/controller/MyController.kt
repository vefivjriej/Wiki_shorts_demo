package com.example.demo.controller

import com.example.demo.Service.DeepSeekService
import com.example.demo.Service.WikipediaDeepseekService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/skills")
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
    fun getRandomArticle(): String {
        return wikipediaDeepseekService.fetchRandomArticle().summarizedText
    }
}