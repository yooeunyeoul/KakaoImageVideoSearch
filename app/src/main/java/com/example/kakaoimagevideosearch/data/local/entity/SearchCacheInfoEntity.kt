package com.example.kakaoimagevideosearch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 검색 캐시 정보를 저장하는 Entity
 * 각 검색어마다 마지막 검색 시간과 캐시 만료 시간을 관리
 */
@Entity(tableName = "search_cache_info")
data class SearchCacheInfoEntity(
    @PrimaryKey
    val query: String,
    val lastSearchTime: Long = System.currentTimeMillis(),
    val expirationTimeMs: Long = System.currentTimeMillis() + CACHE_DURATION_MS
) {
    companion object {
        // 캐시 유효 기간: 5분
        const val CACHE_DURATION_MS = 5 * 60 * 1000L
        
        /**
         * 캐시가 유효한지 확인
         * @return 현재 시간이 만료 시간보다 작으면 true(유효함), 아니면 false(만료됨)
         */
        fun isValid(entity: SearchCacheInfoEntity): Boolean {
            return System.currentTimeMillis() < entity.expirationTimeMs
        }
    }
} 