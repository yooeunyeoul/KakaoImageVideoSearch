package com.example.kakaoimagevideosearch.data.model

import com.google.gson.annotations.SerializedName

data class ImageMeta(
    @SerializedName("is_end")
    val isEnd: Boolean = false,
    @SerializedName("pageable_count")
    val pageableCount: Int = 0,
    @SerializedName("total_count")
    val totalCount: Int = 0
) 