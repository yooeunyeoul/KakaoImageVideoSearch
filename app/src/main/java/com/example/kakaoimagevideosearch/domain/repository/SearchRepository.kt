package com.example.kakaoimagevideosearch.domain.repository

import androidx.paging.PagingData
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    /**
     * 검색 결과를 페이징된 Flow로 가져옴
     */
    fun getSearchResults(query: String): Flow<PagingData<SearchResult>>
    
    /**
     * 좋아요 상태 토글
     */
    suspend fun toggleFavorite(resultId: String)
    
    /**
     * 캐시된 검색 결과를 Flow로 제공
     */
    fun getCachedSearchResultsFlow(query: String): Flow<List<SearchResult>>
} 