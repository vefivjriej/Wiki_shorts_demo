package Repositories

import Entities.WikiArticle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface WikiArticleRepository: JpaRepository<WikiArticle, Long>{
    @Query(
        value = "SELECT * FROM wiki_article ORDER BY RANDOM() LIMIT 1",
        nativeQuery = true
    )
    fun findRandomArticle(): WikiArticle?
}