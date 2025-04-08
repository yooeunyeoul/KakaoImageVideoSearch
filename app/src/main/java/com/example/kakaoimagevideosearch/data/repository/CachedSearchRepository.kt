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
import com.example.kakaoimagevideosearch.domain.repository.BookmarkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CachedSearchRepository @Inject constructor(
    private val api: KakaoSearchApi,
    private val searchDao: SearchDao,
    private val bookmarkRepository: BookmarkRepository,
    @ApplicationScope private val externalScope: CoroutineScope
) : SearchRepository {

    // 캐시 저장 작업을 동기화하기 위한 Mutex
    private val cacheMutex = Mutex()

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
                maxSize = PAGE_SIZE * 6,
                prefetchDistance = 20
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
                // Mutex로 보호된 코드 블록 - 동시성 이슈 방지
                cacheMutex.withLock {
                    Log.d(TAG, "검색 결과 캐싱: 쿼리='$query', 페이지=$page, 결과 수=${results.size}")
                    
                    // 캐시 정보 조회 또는 생성
                    val cacheInfo = searchDao.getSearchCacheInfo(query) ?: SearchCacheInfoEntity(query = query)
                    
                    // 현재 시간 저장 - 여러 곳에서 사용
                    val currentTime = System.currentTimeMillis()
                    
                    // 캐시 정보 업데이트 (시간 갱신)
                    val updatedCacheInfo = cacheInfo.copy(
                        lastSearchTime = currentTime,
                        expirationTimeMs = currentTime + SearchCacheInfoEntity.CACHE_DURATION_MS
                    )
                    
                    // 만료된 캐시 정리 (백그라운드에서 수행)
                    searchDao.clearExpiredCache()
                    
                    // 기존 검색 결과에서 썸네일 URL 목록 조회
                    val existingThumbnailUrls = searchDao.getThumbnailUrlsByQuery(query)
                        ?.map { it.thumbnailUrl }
                        ?.toSet() ?: emptySet()
                    
                    // 중복되지 않는 결과만 필터링
                    val filteredResults = results.filter { result -> 
                        // 썸네일 URL이 기존 데이터에 없는 경우만 포함
                        result.thumbnailUrl !in existingThumbnailUrls
                    }
                    
                    // 필터링된 항목의 수 로그
                    Log.d(TAG, "중복 제거 후 저장할 결과 수: 원본=${results.size}, 필터링 후=${filteredResults.size}")
                    
                    // 검색 결과를 엔티티로 변환
                    val entities = filteredResults.map { SearchResultEntity.fromDomain(it, query, page) }
                    
                    // 캐시 정보와 검색 결과를 트랜잭션으로 함께 저장
                    searchDao.saveSearchResultsWithInfo(updatedCacheInfo, entities)
                    
                    if (filteredResults.isEmpty()) {
                        Log.d(TAG, "저장할 새로운 결과 없음 (모두 중복)")
                    }
                }
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
     * 특정 검색 결과의 좋아요 상태를 토글
     * DB 변경 후 Flow가 자동으로 새 데이터를 발행
     */
    override suspend fun toggleFavorite(resultId: String) {
        try {
            // DB에서 검색 결과 항목 가져오기
            val result = searchDao.getSearchResultById(resultId) ?: return
            
            // 좋아요 상태 토글
            val isFavorite = !result.isFavorite
            val updatedResult = result.copy(isFavorite = isFavorite)
            
            // 업데이트된 항목 저장
            searchDao.updateSearchResult(updatedResult)
            
            // 북마크 정보도 함께 업데이트
            if (isFavorite) {
                // 좋아요 -> 북마크 추가
                bookmarkRepository.addBookmark(updatedResult.toDomain())
            } else {
                // 좋아요 해제 -> 북마크 제거
                bookmarkRepository.removeBookmarkById(resultId)
            }
            
            Log.d(TAG, "좋아요 상태 토글: ID=$resultId, 새 상태=$isFavorite")
        } catch (e: Exception) {
            Log.e(TAG, "좋아요 상태 토글 중 오류 발생: ID=$resultId", e)
        }
    }
} 