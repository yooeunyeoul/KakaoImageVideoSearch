package com.example.kakaoimagevideosearch.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.kakaoimagevideosearch.data.api.KakaoSearchApi
import com.example.kakaoimagevideosearch.data.local.dao.SearchDao
import com.example.kakaoimagevideosearch.data.local.entity.SearchCacheInfoEntity
import com.example.kakaoimagevideosearch.data.local.entity.SearchResultEntity
import com.example.kakaoimagevideosearch.data.paging.CachingAwarePagingSource
import com.example.kakaoimagevideosearch.data.paging.SearchPagingSource
import com.example.kakaoimagevideosearch.di.ApplicationScope
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.repository.SearchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CachedSearchRepository @Inject constructor(
    private val api: KakaoSearchApi,
    private val searchDao: SearchDao,
    @ApplicationScope private val externalScope: CoroutineScope
) : SearchRepository {

    companion object {
        private const val PAGE_SIZE = 20
        private const val TAG = "CachedSearchRepository"
    }

    override fun getSearchResults(query: String): Flow<PagingData<SearchResult>> {
        Log.d(TAG, "getSearchResults 호출: 쿼리='$query'")
        
        // 빈 쿼리인 경우 빠르게 처리
        if (query.isBlank()) {
            return Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    enablePlaceholders = false
                ),
                pagingSourceFactory = { SearchPagingSource(api, query) }
            ).flow
        }
        
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                maxSize = PAGE_SIZE * 4,
                prefetchDistance = 10
            ),
            pagingSourceFactory = { 
                CachingAwarePagingSource(
                    api = api,
                    searchDao = searchDao,
                    query = query,
                    onPageLoaded = { results, page -> saveResultsToCache(query, results, page) }
                )
            }
        ).flow.also {
            Log.d(TAG, "Pager.flow 반환 완료: 쿼리='$query'")
        }
    }
    
    /**
     * 검색 결과를 캐시에 저장
     * 
     * @param query 검색어
     * @param results 저장할 검색 결과
     * @param page 페이지 번호
     */
    private fun saveResultsToCache(query: String, results: List<SearchResult>, page: Int) {
        externalScope.launch {
            try {
                Log.d(TAG, "검색 결과 캐싱: 쿼리='$query', 페이지=$page, 결과 수=${results.size}")
                
                // 첫 페이지인 경우에만 캐시 정보를 업데이트 또는 생성
                if (page == 1) {
                    // 캐시 정보 조회 또는 생성
                    val cacheInfo = searchDao.getSearchCacheInfo(query) ?: SearchCacheInfoEntity(query = query)
                    
                    // 캐시 정보 업데이트 (시간 갱신)
                    searchDao.insertSearchCacheInfo(cacheInfo.copy(
                        lastSearchTime = System.currentTimeMillis(),
                        expirationTimeMs = System.currentTimeMillis() + SearchCacheInfoEntity.CACHE_DURATION_MS
                    ))
                    
                    // 첫 페이지가 로드될 때 만료된 캐시 정리 (백그라운드에서 수행)
                    searchDao.clearExpiredCache()
                }
                
                // 이 페이지의 결과를 캐시에 저장
                val entities = results.map { SearchResultEntity.fromDomain(it, query, page) }
                searchDao.insertSearchResults(entities)
                
            } catch (e: Exception) {
                Log.e(TAG, "캐시 저장 중 오류 발생: 쿼리='$query', 페이지=$page", e)
            }
        }
    }
    
    /**
     * 특정 검색어에 대한 캐시된 결과를 Flow로 제공
     * (SearchViewModel에서 필요한 경우 사용)
     */
    override fun getCachedSearchResultsFlow(query: String): Flow<List<SearchResult>> {
        return searchDao.getSearchResultsListFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * 캐시된 모든 검색어 정보 Flow 제공
     */
    fun getCachedSearchQueries(): Flow<List<String>> {
        return searchDao.getAllSearchCacheInfo().map { cacheInfoList ->
            cacheInfoList.map { it.query }
        }
    }

    /**
     * 특정 검색 결과의 좋아요 상태를 토글
     * DB 변경 후 Flow가 자동으로 새 데이터를 발행
     */
    override suspend fun toggleFavorite(resultId: String) {
        try {
            // DB에서 검색 결과 항목 가져오기
            val result = searchDao.getSearchResultById(resultId) ?: return
            
            // 좋아요 상태 토글
            val updatedResult = result.copy(isFavorite = !result.isFavorite)
            
            // 업데이트된 항목 저장
            searchDao.updateSearchResult(updatedResult)
            
            Log.d(TAG, "좋아요 상태 토글: ID=$resultId, 새 상태=${updatedResult.isFavorite}")
        } catch (e: Exception) {
            Log.e(TAG, "좋아요 상태 토글 중 오류 발생: ID=$resultId", e)
        }
    }
} 