package com.example.kakaoimagevideosearch.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoMeta(
    @Json(name = "is_end")
    val isEnd: Boolean = false,
    @Json(name = "pageable_count")
    val pageableCount: Int = 0,
    @Json(name = "total_count")
    val totalCount: Int = 0
) 