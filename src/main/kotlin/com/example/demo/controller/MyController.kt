package com.example.demo.controller

//import com.example.demo.Service.DeepSeekService
import com.example.demo.Entity.UserEntity
import com.example.demo.Repository.UserRepository
import com.example.demo.Service.RegisterService
import com.example.demo.Service.WikipediaDeepseekService
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("api/skills")
class MyController @Autowired constructor(
    //private val deepSeekService: DeepSeekService,
    private val wikipediaDeepseekService: WikipediaDeepseekService,
    private val chatModel: OpenAiChatModel,
    private val registerService: RegisterService
) {
    @GetMapping("/article/random")
    fun getRandomArticle(): String {
        return wikipediaDeepseekService.fetchRandomArticle().summarizedText
    }
    @GetMapping("/ai/generate")
    fun generate(
        @RequestParam(value = "message", defaultValue = "Tell me a joke") message: String
    ): Map<String, String> {
        return mapOf("generation" to chatModel.call(message))
    }
    @PostMapping("/registration")
    fun register(@RequestBody request: UserEntity):String{
        registerService.register(request)
        return "Пользователь ${request.userName} успешно зарегестрирован"
    }
}
//    @GetMapping("/ai/shorten")
//    fun shortenText(@RequestParam(value = "text") text: String): Map<String, String> {
//        val shortenedText = deepSeekService.shortenText(text)
//        return mapOf("shortenedText" to shortenedText)
//    }