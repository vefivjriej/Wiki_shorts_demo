package Service
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.springframework.stereotype.Service

@Service
class WikipediaService {
    private val client = OkHttpClient()

    fun fetchRandomArticle(): Pair<String, String> {
        // 1. Получаем случайное название статьи
        val randomTitleUrl = "https://en.wikipedia.org/api/rest_v1/page/random/title"
        val titleRequest = Request.Builder().url(randomTitleUrl).build()
        val titleResponse = client.newCall(titleRequest).execute()
        val titleJson = JSONObject(titleResponse.body?.string())
        val title = titleJson.getString("title")

        // 2. Получаем текст статьи
        val articleUrl = "https://en.wikipedia.org/w/api.php?action=query&format=json&titles=$title&prop=extracts&explaintext"
        val articleRequest = Request.Builder().url(articleUrl).build()
        val articleResponse = client.newCall(articleRequest).execute()
        val articleJson = JSONObject(articleResponse.body?.string())
        val pages = articleJson.getJSONObject("query").getJSONObject("pages")
        val pageId = pages.keys().next().toString()
        val fullText = pages.getJSONObject(pageId).getString("extract")
        return Pair(title, fullText)
    }
}