package com.example.kakaoimagevideosearch.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.kakaoimagevideosearch.data.api.KakaoSearchApi
import com.example.kakaoimagevideosearch.data.local.dao.SearchDao
import com.example.kakaoimagevideosearch.data.local.entity.SearchCacheInfoEntity
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 캐시 인식 PagingSource
 * 각 페이지별로 캐시를 확인하고, 캐시가 없거나 유효하지 않으면 API를 호출
 * Flow를 통해 DB 변경사항(좋아요 상태 등)을 실시간으로 감지하고 UI에 반영
 * 
 * 주의: 초기 로드 후에는 DB 변경사항이 PagingData에 자동으로 반영되지 않음
 * 좋아요 상태 변경 시, UI에서 변경사항을 별도로 관찰하거나 특정 항목만 업데이트해야 함
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CachingAwarePagingSource @Inject constructor(
    private val api: KakaoSearchApi,
    private val searchDao: SearchDao,
    private val query: String,
    private val onPageLoaded: (List<SearchResult>, Int) -> Unit
) : PagingSource<Int, SearchResult>() {
    
    companion object {
        private const val PAGE_SIZE = 20
        private const val TAG = "CachingPagingSource"
    }
    
    override fun getRefreshKey(state: PagingState<Int, SearchResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResult> {
        val page = params.key ?: 1
        Log.d(TAG, "load 호출: 쿼리='$query', 페이지=$page")
        
        try {
            // 전체 캐시가 유효한지 확인
            val isCacheValid = try {
                val cacheInfo = searchDao.getSearchCacheInfo(query)
                cacheInfo != null && SearchCacheInfoEntity.isValid(cacheInfo)
            } catch (e: Exception) {
                Log.e(TAG, "캐시 유효성 확인 중 오류 발생", e)
                false
            }
            
            // 캐시가 유효하고 해당 페이지가 캐시에 있는지 확인
            if (isCacheValid) {
                try {
                    val isPageCached = searchDao.isPageCached(query, page)
                    
                    if (isPageCached) {
                        Log.d(TAG, "캐시된 데이터 확인: 쿼리='$query', 페이지=$page")
                        
                        // 해당 페이지의 결과만 Flow로 가져옴
                        // 단, first()를 호출하면 Flow 구독이 종료되므로 DB 변경사항은 이후에 감지되지 않음
                        // ViewModel에서 별도의 Flow를 구독해야 함
                        val cachedResults = searchDao.getSearchResultsByPageFlow(query, page).first()
                        
                        // 캐시된 결과가 있고 비어있지 않은 경우에만 사용
                        if (cachedResults.isNotEmpty()) {
                            Log.d(TAG, "캐시된 데이터 사용: 쿼리='$query', 페이지=$page, 결과 수=${cachedResults.size}")
                            
                            // 항상 다음 페이지로 이동할 수 있도록 nextKey 설정
                            // API에서 추가 데이터를 가져올 가능성이 있으므로 페이지네이션이 계속되어야 함
                            val nextKey = page + 1
                            
                            return LoadResult.Page(
                                data = cachedResults.map { it.toDomain() },
                                prevKey = if (page == 1) null else page - 1,
                                nextKey = nextKey
                            )
                        } else {
                            Log.d(TAG, "캐시된 데이터가 비어 있음: 쿼리='$query', 페이지=$page")
                        }
                    } else {
                        Log.d(TAG, "페이지가 캐시되지 않음: 쿼리='$query', 페이지=$page")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "캐시된 데이터 조회 중 오류 발생", e)
                    // 오류 발생 시 API 호출로 대체
                }
            } else {
                Log.d(TAG, "캐시가 유효하지 않거나 없음: 쿼리='$query'")
            }
            
            // 캐시가 없거나 유효하지 않거나, 페이지가 캐시되지 않았거나, 캐시된 결과가 비어 있는 경우
            // API 호출
            Log.d(TAG, "API 호출: 쿼리='$query', 페이지=$page")
            val apiPagingSource = SearchPagingSource(api, query)
            
            // SearchPagingSource의 load 메서드에 page 정보 전달
            val result = apiPagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = page,
                    loadSize = params.loadSize,
                    placeholdersEnabled = false
                )
            )
            
            // 결과가 성공적으로 로드되면 캐시 업데이트를 위한 콜백 호출
            if (result is LoadResult.Page && result.data.isNotEmpty()) {
                onPageLoaded(result.data, page)
            }
            
            return result
        } catch (e: Exception) {
            Log.e(TAG, "로드 중 오류 발생: 쿼리='$query', 페이지=$page", e)
            return LoadResult.Error(e)
        }
    }
} 