package com.example.kakaoimagevideosearch.data.paging

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
        private const val PAGE_SIZE = 20
    }

    override fun getRefreshKey(state: PagingState<Int, SearchResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResult> {
        if (query.isBlank()) {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        }
        
        return try {
            val page = params.key ?: STARTING_PAGE_INDEX

            // 이미지와 비디오 검색을 병렬로 실행
            coroutineScope {
                val imageDeferred = async {
                    try {
                        api.searchImage(
                            query = query,
                            sort = "recency",
                            page = page,
                            size = PAGE_SIZE
                        ).first()
                    } catch (e: Exception) {
                        NetworkResult.Error(400, e.message ?: "이미지 검색 중 오류가 발생했습니다.")
                    }
                }

                val videoDeferred = async {
                    try {
                        api.searchVideo(
                            query = query,
                            sort = "recency",
                            page = page,
                            size = PAGE_SIZE
                        ).first()
                    } catch (e: Exception) {
                        NetworkResult.Error(400, e.message ?: "비디오 검색 중 오류가 발생했습니다.")
                    }
                }

                // 두 결과를 기다림
                val imageResult = imageDeferred.await()
                val videoResult = videoDeferred.await()

                // 결과 처리
                val combinedResults = mutableListOf<SearchResult>()
                
                if (imageResult is NetworkResult.Success) {
                    combinedResults.addAll(imageResult.data.documents.map { it.toDomain() })
                }
                
                if (videoResult is NetworkResult.Success) {
                    combinedResults.addAll(videoResult.data.documents.map { it.toDomain() })
                }
                
                // 결과가 없고 둘 다 에러인 경우
                if (combinedResults.isEmpty() && 
                    (imageResult is NetworkResult.Error || imageResult is NetworkResult.NetworkError) && 
                    (videoResult is NetworkResult.Error || videoResult is NetworkResult.NetworkError)) {
                    
                    val errorMessage = when {
                        imageResult is NetworkResult.Error -> imageResult.message
                        videoResult is NetworkResult.Error -> videoResult.message
                        else -> "검색 결과가 없습니다."
                    }
                    
                    return@coroutineScope LoadResult.Error(Exception(errorMessage))
                }
                
                // 결과를 datetime 기준으로 정렬
                val sortedResults = combinedResults.sortedByDescending { it.datetime }
                
                // 다음 페이지 키 계산
                val nextKey = if (sortedResults.isEmpty()) {
                    null
                } else {
                    page + 1
                }

                LoadResult.Page(
                    data = sortedResults,
                    prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1,
                    nextKey = nextKey
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
} 