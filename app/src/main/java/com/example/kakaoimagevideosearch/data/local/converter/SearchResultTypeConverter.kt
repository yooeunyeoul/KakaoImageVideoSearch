package com.example.kakaoimagevideosearch.data.local.converter

import androidx.room.TypeConverter
import com.example.kakaoimagevideosearch.domain.model.SearchResultType

/**
 * SearchResultType enum과 String 간의 변환을 처리하는 TypeConverter
 */
class SearchResultTypeConverter {
    
    @TypeConverter
    fun fromSearchResultType(type: SearchResultType): String {
        return type.name
    }
    
    @TypeConverter
    fun toSearchResultType(value: String): SearchResultType {
        return try {
            SearchResultType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            SearchResultType.IMAGE // 기본값
        }
    }
} 