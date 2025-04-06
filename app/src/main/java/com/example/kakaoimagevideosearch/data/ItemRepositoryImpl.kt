package com.example.kakaoimagevideosearch.data

import com.example.kakaoimagevideosearch.data.api.ItemApiService
import com.example.kakaoimagevideosearch.data.mapper.toDomain
import com.example.kakaoimagevideosearch.domain.model.Item
import com.example.kakaoimagevideosearch.utils.mapResult
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