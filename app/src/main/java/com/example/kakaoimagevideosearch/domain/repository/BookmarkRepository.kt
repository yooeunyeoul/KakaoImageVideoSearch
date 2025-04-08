package com.example.kakaoimagevideosearch.domain.repository

import com.example.kakaoimagevideosearch.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

/**
 * 북마크 저장소 인터페이스
 */
interface BookmarkRepository {
    /**
     * 모든 북마크 가져오기
     * @return 북마크된 검색 결과 Flow
     */
    fun getAllBookmarks(): Flow<List<SearchResult>>
    
    /**
     * 검색 결과 북마크 추가
     * @param searchResult 북마크할 검색 결과
     */
    suspend fun addBookmark(searchResult: SearchResult)
    
    /**
     * ID로 북마크 제거
     * @param id 검색 결과 ID
     */
    suspend fun removeBookmarkById(id: String)

    
    /**
     * 북마크 개수 가져오기
     * @return 북마크 개수 Flow
     */
    fun getBookmarkCount(): Flow<Int>
} 