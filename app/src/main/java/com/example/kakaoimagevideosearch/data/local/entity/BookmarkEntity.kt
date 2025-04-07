package com.example.kakaoimagevideosearch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.kakaoimagevideosearch.data.local.converter.SearchResultTypeConverter
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.model.SearchResultType

/**
 * 사용자가 북마크한 검색 결과를 저장하는 Entity
 */
@Entity(tableName = "bookmarks")
@TypeConverters(SearchResultTypeConverter::class)
data class BookmarkEntity(
    @PrimaryKey
    val id: String,
    val thumbnailUrl: String,
    val title: String,
    val source: String,
    val datetime: String,
    val type: SearchResultType,
    val bookmarkedAt: Long = System.currentTimeMillis() // 북마크한 시간
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
        type = type
    )

    companion object {
        /**
         * 도메인 모델을 Entity로 변환
         */
        fun fromDomain(searchResult: SearchResult): BookmarkEntity = BookmarkEntity(
            id = searchResult.id,
            thumbnailUrl = searchResult.thumbnailUrl,
            title = searchResult.title,
            source = searchResult.source,
            datetime = searchResult.datetime,
            type = searchResult.type
        )
    }
} 