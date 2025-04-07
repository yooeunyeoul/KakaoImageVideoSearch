package com.example.kakaoimagevideosearch.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.kakaoimagevideosearch.data.local.converter.SearchResultTypeConverter
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.model.SearchResultType
import java.util.Date

/**
 * 검색 결과를 저장하는 Entity
 * 캐싱을 위해 사용됨
 */
@Entity(
    tableName = "search_results",
    indices = [
        Index(value = ["query", "page"]) // 쿼리와 페이지로 빠르게 검색하기 위한 인덱스
    ]
)
@TypeConverters(SearchResultTypeConverter::class)
data class SearchResultEntity(
    @PrimaryKey
    val id: String,
    val thumbnailUrl: String,
    val title: String,
    val source: String,
    val datetime: String,
    val type: SearchResultType,
    val query: String, // 어떤 검색어로 가져온 결과인지 저장
    val page: Int, // 페이지 번호
    val timestamp: Long = System.currentTimeMillis(), // 캐시 타임스탬프
    val isFavorite: Boolean = false // 좋아요 상태
) {
    /**
     * Entity를 도메인 모델로 변환
     */
    fun toDomain(): SearchResult = SearchResult(
        id = id,
        thumbnailUrl = thumbnailUrl,
        title = title,
        source = source,
        datetime = datetime,
        type = type,
        isFavorite = isFavorite
    )

    companion object {
        /**
         * 도메인 모델을 Entity로 변환
         */
        fun fromDomain(searchResult: SearchResult, query: String, page: Int): SearchResultEntity = SearchResultEntity(
            id = searchResult.id,
            thumbnailUrl = searchResult.thumbnailUrl,
            title = searchResult.title,
            source = searchResult.source,
            datetime = searchResult.datetime,
            type = searchResult.type,
            query = query,
            page = page,
            isFavorite = searchResult.isFavorite
        )
    }
} 