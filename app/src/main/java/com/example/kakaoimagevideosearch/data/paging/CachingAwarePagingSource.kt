package com.example.kakaoimagevideosearch.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.kakaoimagevideosearch.data.api.KakaoSearchApi
import com.example.kakaoimagevideosearch.data.local.dao.SearchDao
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        private const val TAG = "CachingPagingSource"
        private const val PAGE_SIZE = 20 // 페이지 당 아이템 수 (CachedSearchRepository와 동일하게 유지)
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
            // 한 번의 쿼리로 유효한 캐시 페이지 데이터 조회
            try {
                // 캐시 유효성과 페이지 데이터를 한 번에 조회하는 쿼리 실행
                val cachedResults = searchDao.getValidCachePageResults(query, page)

                // 캐시에 유효한 데이터가 있으면 사용
                if (cachedResults.isNotEmpty()) {
                    Log.d(TAG, "유효한 캐시 데이터 사용: 쿼리='$query', 페이지=$page, 결과 수=${cachedResults.size}")

                    // 다음 페이지 키 설정 - 결과 크기가 페이지 크기보다 작으면 마지막 페이지로 간주
                    val nextKey = if (cachedResults.size < PAGE_SIZE) {
                        Log.d(TAG, "페이지 $page 결과가 $PAGE_SIZE 미만(${cachedResults.size}개)이므로 마지막 페이지로 간주")
                        null // 마지막 페이지
                    } else {
                        page + 1 // 다음 페이지 있음
                    }

                    return LoadResult.Page(
                        data = cachedResults.map { it.toDomain() },
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = nextKey
                    )
                } else {
                    Log.d(TAG, "유효한 캐시 데이터 없음: 쿼리='$query', 페이지=$page - API 호출 필요")
                }
            } catch (e: Exception) {
                Log.e(TAG, "캐시 데이터 조회 중 오류 발생", e)
                // 오류 발생 시 API 호출로 대체
            }

            // 유효한 캐시가 없는 경우 API 호출
            Log.d(TAG, "API 호출: 쿼리='$query', 페이지=$page")
            val apiPagingSource = SearchPagingSource(api, query)

            // SearchPagingSource의 load 메서드에 page 정보 전달
            val result = apiPagingSource.load(
                LoadParams.Refresh(
                    key = page,
                    loadSize = params.loadSize,
                    placeholdersEnabled = false
                )
            )

            // 결과가 성공적으로 로드되면 캐시 업데이트를 위한 콜백 호출
            if (result is LoadResult.Page) {
                if (result.data.isNotEmpty()) {
                    Log.d(TAG, "API 호출 성공, 데이터 캐싱: 쿼리='$query', 페이지=$page, 결과 수=${result.data.size}")
                    onPageLoaded(result.data, page)
                    
                    // 마지막 페이지 여부 확인 로그 추가
                    if (result.nextKey == null) {
                        Log.d(TAG, "API 호출 결과가 마지막 페이지로 확인됨: 쿼리='$query', 페이지=$page")
                    }
                } else {
                    Log.d(TAG, "API 호출 성공했으나 결과 없음: 쿼리='$query', 페이지=$page")
                }
            }

            return result
        } catch (e: Exception) {
            Log.e(TAG, "로드 중 오류 발생: 쿼리='$query', 페이지=$page", e)
            return LoadResult.Error(e)
        }
    }
} 