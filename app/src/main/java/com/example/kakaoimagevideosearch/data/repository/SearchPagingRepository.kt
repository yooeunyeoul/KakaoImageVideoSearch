package com.example.kakaoimagevideosearch.data.repository

import android.util.Log
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
        private const val TAG = "SearchPagingRepository"
    }

    override fun getSearchResults(query: String): Flow<PagingData<SearchResult>> {
        Log.d(TAG, "getSearchResults 호출: 쿼리='$query'")
        
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                maxSize = PAGE_SIZE * 4,
                prefetchDistance = 20
            ),
            pagingSourceFactory = { 
                Log.d(TAG, "PagingSource 생성: 쿼리='$query'")
                SearchPagingSource(api, query) 
            }
        ).flow.also {
            Log.d(TAG, "Pager.flow 반환 완료: 쿼리='$query'")
        }
    }
} 