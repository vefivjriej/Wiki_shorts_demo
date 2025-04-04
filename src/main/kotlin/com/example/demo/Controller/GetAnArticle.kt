import com.example.demo.Service.WikipediaDeepseekService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.ai.chat.OpenAiChatModel
import org.springframework.ai.chat.ChatResponse
import org.springframework.ai.chat.Prompt
import org.springframework.ai.chat.UserMessage
import reactor.core.publisher.Flux

@RestController
class MyController @Autowired constructor(
    private val chatModel: OpenAiChatModel,
    private val wikipediaDeepseekService: WikipediaDeepseekService // Добавляем новую зависимость
) {

    @GetMapping("/ai/shorten")
    fun shortenText(@RequestParam(value = "text") text: String): Map<String, String> {
        // Формируем запрос для модели
        val promptText = "Сократи следующий текст до 170 слов: $text"
        val prompt = Prompt(UserMessage(promptText))

        // Получаем ответ от модели
        val response: ChatResponse = chatModel.call(prompt)

        // Возвращаем сокращенный текст
        return mapOf("shortenedText" to response.message)
    }

    @GetMapping("/ai/shortenStream")
    fun shortenTextStream(@RequestParam(value = "text") text: String): Flux<ChatResponse> {
        // Формируем запрос для модели
        val promptText = "Сократи следующий текст до 170 слов: $text"
        val prompt = Prompt(UserMessage(promptText))

        // Возвращаем поток ответов от модели
        return chatModel.stream(prompt)
    }
}