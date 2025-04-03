package Controller

import Service.WikipediaDeepseekService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/articles")
class GetAnArticle(
    val service: WikipediaDeepseekService
) {
    @PostMapping("/summerized")
