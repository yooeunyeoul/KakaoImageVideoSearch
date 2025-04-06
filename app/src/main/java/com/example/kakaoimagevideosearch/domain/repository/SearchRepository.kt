package com.example.kakaoimagevideosearch.domain.repository

import androidx.paging.PagingData
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun getSearchResults(query: String): Flow<PagingData<SearchResult>>
} 