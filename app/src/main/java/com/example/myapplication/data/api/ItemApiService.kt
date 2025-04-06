package com.example.myapplication.data.api

import com.example.myapplication.data.model.Item
import com.example.myapplication.utils.NetworkResult
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ItemApiService {
    @GET("items")
    fun getItems(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Flow<NetworkResult<List<Item>>>

    @GET("items/{id}")
    fun getItem(
        @Path("id") id: Int
    ): Flow<NetworkResult<Item>>
}
