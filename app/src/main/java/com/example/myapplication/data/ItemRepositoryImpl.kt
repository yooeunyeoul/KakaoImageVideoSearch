package com.example.myapplication.data

import com.example.myapplication.data.api.ItemApiService
import com.example.myapplication.data.mapper.toDomain
import com.example.myapplication.domain.model.Item
import com.example.myapplication.utils.mapResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    private val itemApiService: ItemApiService
) : ItemRepository {
    override fun getItems(): Flow<List<Item>> =
        itemApiService.getItems().mapResult { apiItems ->
            apiItems.toDomain()
        }

    override fun getItem(id: Int): Flow<Item> =
        itemApiService.getItem(id).mapResult { it.toDomain() }
} 