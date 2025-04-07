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
     * 모든 북마크 아이템을 북마크한 시간 기준 내림차순으로 조회
     */
    @Query("SELECT * FROM bookmarks ORDER BY bookmarkedAt DESC")
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
     * 북마크 삭제
     */
    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)
    
    /**
     * ID로 북마크 삭제
     */
    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)
    
    /**
     * 특정 ID의 북마크 존재 여부 확인
     */
    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    fun isBookmarked(id: String): Flow<Boolean>
    
    /**
     * 모든 북마크 개수 조회
     */
    @Query("SELECT COUNT(*) FROM bookmarks")
    fun getBookmarkCount(): Flow<Int>
} 