package com.example.myapplication.data.api

import com.example.myapplication.data.model.Item
import com.example.myapplication.utils.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ItemApiServiceImpl @Inject constructor() : ItemApiService {
    override fun getItems(page: Int, limit: Int): Flow<NetworkResult<List<Item>>> = flow {
        // 임시 데이터 반환
        emit(NetworkResult.Success(listOf(
            Item(1, "아이템 1", "설명 1"),
            Item(2, "아이템 2", "설명 2"),
            Item(3, "아이템 3", "설명 3")
        )))
    }

    override fun getItem(id: Int): Flow<NetworkResult<Item>> = flow {
        // 임시 데이터 반환
        emit(NetworkResult.Success(Item(id, "아이템 $id", "설명 $id")))
    }
} 