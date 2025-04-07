package com.example.kakaoimagevideosearch.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.kakaoimagevideosearch.data.api.KakaoSearchApi
import com.example.kakaoimagevideosearch.data.mapper.toDomain
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.utils.NetworkResult
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SearchPagingSource @Inject constructor(
    private val api: KakaoSearchApi,
    private val query: String
) : PagingSource<Int, SearchResult>() {

    companion object {
        private const val STARTING_PAGE_INDEX = 1
        // 각 API별로 가져올 크기. 한 페이지 로드 시 최대 2 * PAGE_SIZE 만큼의 아이템이 로드될 수 있음.
        private const val PAGE_SIZE = 20 // CachedSearchRepository와 일치
        private const val TAG = "SearchPagingSource"
    }

    // 각 API의 마지막 페이지 도달 여부를 추적하는 상태 변수
    private var isImageEndReached = false
    private var isVideoEndReached = false
    
    // 검색 결과 로드 콜백
    private var onPagesLoadedCallback: ((List<SearchResult>) -> Unit)? = null
    
    // 콜백 등록 함수
    fun registerOnPagesLoadedCallback(callback: (List<SearchResult>) -> Unit) {
        this.onPagesLoadedCallback = callback
    }

    override fun getRefreshKey(state: PagingState<Int, SearchResult>): Int? {
        // 기본 구현 유지 또는 필요에 따라 조정
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResult> {
        Log.d(TAG, "Load requested by Paging library with params: $params") // <-- 이 로그 필수!
        if (query.isBlank()) {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        }

        val page = params.key ?: STARTING_PAGE_INDEX
        Log.d(TAG, "로드 시작: 페이지=$page, 쿼리=$query, isImageEnd=$isImageEndReached, isVideoEnd=$isVideoEndReached")

        return try {
            // 이미지와 비디오 검색을 병렬로 실행 (단, 해당 API가 아직 끝나지 않은 경우에만)
            coroutineScope {
                val imageDeferred = if (!isImageEndReached) {
                    async {
                        Log.d(TAG, "이미지 검색 시도: page=$page")
                        try {
                            api.searchImage(
                                query = query,
                                sort = "recency",
                                page = page,
                                size = PAGE_SIZE // 각 API별 사이즈
                            ).first()
                        } catch (e: Exception) {
                            Log.e(TAG, "이미지 검색 API 오류: page=$page", e)
                            NetworkResult.Error(400, e.message ?: "이미지 검색 중 오류 발생")
                        }
                    }
                } else {
                    // 이미 끝난 API는 null deferred 반환 (결과 없음을 의미)
                    Log.d(TAG, "이미지 검색 건너뜀 (이미 마지막 페이지): page=$page")
                    null
                }

                val videoDeferred = if (!isVideoEndReached) {
                    async {
                        Log.d(TAG, "비디오 검색 시도: page=$page")
                        try {
                            api.searchVideo(
                                query = query,
                                sort = "recency",
                                page = page,
                                size = PAGE_SIZE // 각 API별 사이즈
                            ).first()
                        } catch (e: Exception) {
                            Log.e(TAG, "비디오 검색 API 오류: page=$page", e)
                            NetworkResult.Error(400, e.message ?: "비디오 검색 중 오류 발생")
                        }
                    }
                } else {
                    // 이미 끝난 API는 null deferred 반환
                    Log.d(TAG, "비디오 검색 건너뜀 (이미 마지막 페이지): page=$page")
                    null
                }

                // 두 결과를 기다림 (null일 수 있음)
                val imageResult = imageDeferred?.await()
                val videoResult = videoDeferred?.await()

                // 결과 처리 및 상태 업데이트
                val combinedResults = mutableListOf<SearchResult>()
                var currentImageIsEnd = isImageEndReached // 기본값은 이전 상태 유지
                var currentVideoIsEnd = isVideoEndReached // 기본값은 이전 상태 유지

                if (imageResult is NetworkResult.Success) {
                    combinedResults.addAll(imageResult.data.documents.map { it.toDomain() })
                    currentImageIsEnd = imageResult.data.meta.isEnd
                    Log.d(TAG, "이미지 결과 수신: page=$page, count=${imageResult.data.documents.size}, isEnd=${currentImageIsEnd}")
                    Log.d(TAG, "  -> currentImageIsEnd 업데이트됨: $currentImageIsEnd")
                } else if (imageResult != null) { // Error or NetworkError, 하지만 호출은 시도했음
                    Log.w(TAG, "이미지 결과 오류 또는 없음: page=$page, result=$imageResult")
                    // 이미지 API 실패 시, 다음 로드 시도를 위해 isEnd를 false로 유지하거나,
                    // 혹은 에러가 계속되면 더 이상 시도하지 않도록 true로 설정할 수 있음.
                    // 여기서는 일단 false로 유지하여 재시도 가능성을 열어둠 (네트워크 문제 등)
                    // 만약 4xx 에러 등으로 더 이상 호출 의미가 없다면 true로 설정 고려.
                    // currentImageIsEnd = true; // 필요시 주석 해제
                }

                if (videoResult is NetworkResult.Success) {
                    combinedResults.addAll(videoResult.data.documents.map { it.toDomain() })
                    currentVideoIsEnd = videoResult.data.meta.isEnd
                    Log.d(TAG, "비디오 결과 수신: page=$page, count=${videoResult.data.documents.size}, isEnd=${currentVideoIsEnd}")
                } else if (videoResult != null) { // Error or NetworkError
                    Log.w(TAG, "비디오 결과 오류 또는 없음: page=$page, result=$videoResult")
                    // 비디오 API 실패 시 처리 (위 이미지와 유사)
                    // currentVideoIsEnd = true; // 필요시 주석 해제
                }

                // 클래스 멤버 변수(상태) 업데이트
                isImageEndReached = currentImageIsEnd
                isVideoEndReached = currentVideoIsEnd

                // 결과가 없고, 호출을 시도했던 API들이 모두 실패한 경우 에러 반환
                if (combinedResults.isEmpty() && imageDeferred != null && videoDeferred != null &&
                    (imageResult !is NetworkResult.Success) && (videoResult !is NetworkResult.Success)) {
                    val errorMessage = "이미지와 비디오 검색 모두 실패했습니다." // 간단한 에러 메시지
                    Log.e(TAG, "$errorMessage - Image Error: ${imageResult?.toString()}, Video Error: ${videoResult?.toString()}")
                    return@coroutineScope LoadResult.Error(Exception(errorMessage))
                }
                // 결과가 없고, 호출 시도조차 안 한 경우 (이미 둘 다 end 상태)는 아래 nextKey 로직에서 처리됨

                // 결과를 datetime 기준으로 정렬
                val sortedResults = combinedResults.sortedByDescending { it.datetime }

                // 첫 페이지인 경우 캐시 콜백 호출 (첫 페이지만 캐싱)
                if (page == STARTING_PAGE_INDEX && sortedResults.isNotEmpty()) {
                    Log.d(TAG, "첫 페이지 로드 완료 - 캐싱 콜백 호출: 결과 수=${sortedResults.size}")
                    onPagesLoadedCallback?.invoke(sortedResults)
                }

                // 다음 페이지 키 계산: 두 API가 *모두* 마지막 페이지에 도달했을 때만 null
                val nextKey = if (isImageEndReached && isVideoEndReached) {
                    Log.d(TAG, "다음 페이지 없음: 이미지와 비디오 모두 마지막 페이지 도달 (page=$page)")
                    null
                } else {
                    Log.d(TAG, "다음 페이지 존재: page=$page -> ${page + 1}")
                    page + 1
                }

                val prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1
                Log.d(TAG, "페이지 결과: page=$page, prevKey=$prevKey, nextKey=$nextKey, 결과 수=${sortedResults.size}, isImageEnd=$isImageEndReached, isVideoEnd=$isVideoEndReached")

                LoadResult.Page(
                    data = sortedResults,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "로드 중 예외 발생: page=${params.key ?: STARTING_PAGE_INDEX}", e)
            LoadResult.Error(e)
        }
    }
}