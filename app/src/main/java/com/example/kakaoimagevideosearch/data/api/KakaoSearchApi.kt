package com.example.kakaoimagevideosearch.data.api

import com.example.kakaoimagevideosearch.data.model.ImageSearchResponse
import com.example.kakaoimagevideosearch.data.model.VideoSearchResponse
import com.example.kakaoimagevideosearch.utils.NetworkResult
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface KakaoSearchApi {
    @Headers("Authorization: ${ApiConstants.AUTHORIZATION_HEADER}")
    @GET("/v2/search/image")
    fun searchImage(
        @Query("query") query: String,
        @Query("sort") sort: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Flow<NetworkResult<ImageSearchResponse>>

    @Headers("Authorization: ${ApiConstants.AUTHORIZATION_HEADER}")
    @GET("/v2/search/vclip")
    fun searchVideo(
        @Query("query") query: String,
        @Query("sort") sort: String? = null,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ): Flow<NetworkResult<VideoSearchResponse>>
}