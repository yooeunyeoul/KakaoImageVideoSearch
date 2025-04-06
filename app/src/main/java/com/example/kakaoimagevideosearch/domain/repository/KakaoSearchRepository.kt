package com.example.kakaoimagevideosearch.domain.repository

import com.example.kakaoimagevideosearch.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface KakaoSearchRepository {
    fun searchImage(
        query: String,
        sort: String? = null,
        page: Int? = null,
        size: Int? = null
    ): Flow<List<SearchResult>>

    fun searchVideo(
        query: String,
        sort: String? = null,
        page: Int? = null,
        size: Int? = null
    ): Flow<List<SearchResult>>
} 