package com.example.demo.Controller
import com.example.demo.Service.WikipediaDeepseekService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URL

@RestController
@RequestMapping("api/articles")
class GetAnArticle(
    val wikipediaDeepseekService: WikipediaDeepseekService
){
    @GetMapping("/summarize")
    fun summarizetext(): String {
        return wikipediaDeepseekService.fetchRandomArticle().summarizedtext
    }
}