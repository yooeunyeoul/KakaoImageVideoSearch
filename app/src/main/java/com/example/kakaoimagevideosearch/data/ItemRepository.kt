package com.example.kakaoimagevideosearch.data

import com.example.kakaoimagevideosearch.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getItems(): Flow<List<Item>>
    fun getItem(id: Int): Flow<Item>
} 