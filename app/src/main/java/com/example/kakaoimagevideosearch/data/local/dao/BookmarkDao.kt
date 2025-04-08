package com.example.kakaoimagevideosearch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kakaoimagevideosearch.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    /**
     * 모든 북마크 아이템을 북마크한 시간 기준 오름차순으로 조회
     * (가장 오래된 북마크가 먼저, 최신 북마크가 마지막에 표시)
     */
    @Query("SELECT * FROM bookmarks ORDER BY bookmarkedAt ASC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>
    
    /**
     * 특정 ID의 북마크 조회
     */
    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getBookmarkById(id: String): BookmarkEntity?
    
    /**
     * 북마크 추가
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)
    
    /**
     * ID로 북마크 삭제
     */
    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)
    
    /**
     * 모든 북마크 개수 조회
     */
    @Query("SELECT COUNT(*) FROM bookmarks")
    fun getBookmarkCount(): Flow<Int>
} 