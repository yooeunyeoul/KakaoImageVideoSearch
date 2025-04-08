package com.example.kakaoimagevideosearch.data.repository

import android.util.Log
import com.example.kakaoimagevideosearch.data.local.dao.BookmarkDao
import com.example.kakaoimagevideosearch.data.local.entity.BookmarkEntity
import com.example.kakaoimagevideosearch.domain.model.SearchResult
import com.example.kakaoimagevideosearch.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao
) : BookmarkRepository {
    
    companion object {
        private const val TAG = "BookmarkRepository"
    }

    override fun getAllBookmarks(): Flow<List<SearchResult>> {
        return bookmarkDao.getAllBookmarks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addBookmark(searchResult: SearchResult) {
        Log.d(TAG, "북마크 추가: id=${searchResult.id}, title=${searchResult.title}")
        bookmarkDao.insertBookmark(BookmarkEntity.fromDomain(searchResult))
    }

    override suspend fun removeBookmarkById(id: String) {
        Log.d(TAG, "북마크 제거 (ID로): id=$id")
        bookmarkDao.deleteBookmarkById(id)
    }

    override fun getBookmarkCount(): Flow<Int> {
        return bookmarkDao.getBookmarkCount()
    }
} 