package Controller

import Entities.WikiArticle
import Service.WikipediaDeepseekService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/articles")
class GetAnArticle(
    val wikipediaDeepseekService: WikipediaDeepseekService
){
    @GetMapping("/summarize")
    fun summarizetext(): WikiArticle{
        return wikipediaDeepseekService.fetchRandomArticle()
    }
}

