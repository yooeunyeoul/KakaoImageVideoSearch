package com.example.kakaoimagevideosearch.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.kakaoimagevideosearch.data.api.KakaoSearchApi
import com.example.kakaoimagevideosearch.data.paging.SearchPagingSource
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchPagingRepository @Inject constructor(
    private val api: KakaoSearchApi
) : SearchRepository {

    companion object {
        private const val PAGE_SIZE = 20
    }

    override fun getSearchResults(query: String): Flow<PagingData<SearchResult>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                maxSize = PAGE_SIZE * 3
            ),
            pagingSourceFactory = { SearchPagingSource(api, query) }
        ).flow
    }
} 