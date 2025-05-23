package com.example.kakaoimagevideosearch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.kakaoimagevideosearch.data.local.entity.SearchCacheInfoEntity
import com.example.kakaoimagevideosearch.data.local.entity.SearchResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchDao {
    /**
     * 검색 결과를 삽입하거나 업데이트
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(results: List<SearchResultEntity>)
    
    /**
     * 검색 캐시 정보 저장
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchCacheInfo(cacheInfo: SearchCacheInfoEntity)
    
    /**
     * 특정 검색어에 대한 캐시 정보 조회 (일회성 조회)
     */
    @Query("SELECT * FROM search_cache_info WHERE `query` = :query")
    suspend fun getSearchCacheInfo(query: String): SearchCacheInfoEntity?

    
    /**
     * 특정 검색어에 대한 캐시된 검색 결과 조회 (Flow로 반환)
     */
    @Query("SELECT * FROM search_results WHERE `query` = :query ORDER BY datetime DESC")
    fun getSearchResultsListFlow(query: String): Flow<List<SearchResultEntity>>
    
    /**
     * 특정 검색어의 캐시 데이터 삭제
     */
    @Query("DELETE FROM search_results WHERE `query` = :query")
    suspend fun deleteSearchResultsByQuery(query: String)
    
    /**
     * 특정 검색어의 캐시 정보 삭제
     */
    @Query("DELETE FROM search_cache_info WHERE `query` = :query")
    suspend fun deleteSearchCacheInfo(query: String)
    
    /**
     * 만료된 모든 캐시 정보 삭제
     */
    @Query("DELETE FROM search_cache_info WHERE expirationTimeMs < :currentTime")
    suspend fun deleteExpiredCacheInfo(currentTime: Long = System.currentTimeMillis())
    
    /**
     * 만료된 모든 캐시 데이터 삭제
     */
    @Query("DELETE FROM search_results WHERE `query` IN (SELECT `query` FROM search_cache_info WHERE expirationTimeMs < :currentTime)")
    suspend fun deleteExpiredSearchResults(currentTime: Long = System.currentTimeMillis())
    
    /**
     * 검색 캐시 데이터 초기화 (트랜잭션)
     */
    @Transaction
    suspend fun clearSearchCache(query: String) {
        deleteSearchResultsByQuery(query)
        deleteSearchCacheInfo(query)
    }
    
    /**
     * 만료된 검색 캐시 정리 (트랜잭션)
     */
    @Transaction
    suspend fun clearExpiredCache() {
        val currentTime = System.currentTimeMillis()
        deleteExpiredSearchResults(currentTime)
        deleteExpiredCacheInfo(currentTime)
    }

    /**
     * 캐시 정보와 검색 결과를 함께 저장 (트랜잭션)
     * 캐시 정보 저장과 검색 결과 저장을 하나의 트랜잭션으로 처리
     */
    @Transaction
    suspend fun saveSearchResultsWithInfo(cacheInfo: SearchCacheInfoEntity, results: List<SearchResultEntity>) {
        insertSearchCacheInfo(cacheInfo)
        if (results.isNotEmpty()) {
            insertSearchResults(results)
        }
    }
    
    /**
     * 유효한 캐시에서 페이지 데이터 조회 (한 번의 쿼리로 유효성과 데이터 확인)
     * 캐시가 유효하지 않거나 해당 페이지 데이터가 없으면 빈 리스트 반환
     */
    @Query("""
        SELECT results.* FROM search_results results
        JOIN search_cache_info info ON results.`query` = info.`query` 
        WHERE results.`query` = :query 
        AND results.page = :page
        AND info.expirationTimeMs > :currentTime
        ORDER BY results.datetime DESC
    """)
    suspend fun getValidCachePageResults(
        query: String, 
        page: Int, 
        currentTime: Long = System.currentTimeMillis()
    ): List<SearchResultEntity>
    
    /**
     * ID로 검색 결과 조회
     */
    @Query("SELECT * FROM search_results WHERE id = :id")
    suspend fun getSearchResultById(id: String): SearchResultEntity?
    
    /**
     * 검색 결과 업데이트 (좋아요 상태 변경 등)
     */
    @Update
    suspend fun updateSearchResult(result: SearchResultEntity)
    
    /**
     * 특정 검색어에 대한 검색 결과들의 썸네일 URL 목록 조회
     */
    @Query("SELECT id, thumbnailUrl FROM search_results WHERE `query` = :query")
    suspend fun getThumbnailUrlsByQuery(query: String): List<ThumbnailResult>?
}

// 썸네일 URL만 가져오기 위한 투영 클래스
data class ThumbnailResult(
    val id: String,
    val thumbnailUrl: String
) 