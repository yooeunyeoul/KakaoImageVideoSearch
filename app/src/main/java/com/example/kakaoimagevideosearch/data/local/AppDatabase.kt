package com.example.kakaoimagevideosearch.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kakaoimagevideosearch.data.local.converter.SearchResultTypeConverter
import com.example.kakaoimagevideosearch.data.local.dao.BookmarkDao
import com.example.kakaoimagevideosearch.data.local.dao.SearchDao
import com.example.kakaoimagevideosearch.data.local.entity.BookmarkEntity
import com.example.kakaoimagevideosearch.data.local.entity.SearchCacheInfoEntity
import com.example.kakaoimagevideosearch.data.local.entity.SearchResultEntity

@Database(
    entities = [
        SearchResultEntity::class,
        SearchCacheInfoEntity::class,
        BookmarkEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(SearchResultTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun searchDao(): SearchDao
    abstract fun bookmarkDao(): BookmarkDao
    
    companion object {
        private const val DATABASE_NAME = "kakao_search_app.db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // 스키마 버전 변경 시 데이터베이스 초기화 (개발 중에만 사용)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 