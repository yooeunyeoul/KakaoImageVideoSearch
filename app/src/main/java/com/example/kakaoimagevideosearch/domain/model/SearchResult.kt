package com.example.kakaoimagevideosearch.domain.model

data class SearchResult(
    val id: String,
    val thumbnailUrl: String,
    val title: String,
    val source: String,
    val datetime: String,
    val type: SearchResultType
)

enum class SearchResultType {
    IMAGE,
    VIDEO
} 