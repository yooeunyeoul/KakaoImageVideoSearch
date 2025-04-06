package com.example.myapplication.data

import com.example.myapplication.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getItems(): Flow<List<Item>>
    fun getItem(id: Int): Flow<Item>
} 