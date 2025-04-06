package com.example.kakaoimagevideosearch.data.repository

import com.example.kakaoimagevideosearch.data.api.KakaoSearchApi
import com.example.kakaoimagevideosearch.data.mapper.toDomain
import com.example.kakaoimagevideosearch.data.mapper.toImageDomain
import com.example.kakaoimagevideosearch.data.mapper.toVideoDomain
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.repository.KakaoSearchRepository
import com.example.kakaoimagevideosearch.utils.mapResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class KakaoSearchRepositoryImpl @Inject constructor(
    private val api: KakaoSearchApi
) : KakaoSearchRepository {
    override fun searchImage(
        query: String,
        sort: String?,
        page: Int?,
        size: Int?
    ): Flow<List<SearchResult>> =
        api.searchImage(query, sort, page, size).mapResult { response ->
            response.documents.toImageDomain()
        }

    override fun searchVideo(
        query: String,
        sort: String?,
        page: Int?,
        size: Int?
    ): Flow<List<SearchResult>> =
        api.searchVideo(query, sort, page, size).mapResult { response ->
            response.documents.toVideoDomain()
        }
} 