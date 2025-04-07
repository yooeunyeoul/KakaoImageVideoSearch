package com.example.kakaoimagevideosearch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoMeta(
    @SerialName("is_end")
    val isEnd: Boolean = false,
    @SerialName("pageable_count")
    val pageableCount: Int = 0,
    @SerialName("total_count")
    val totalCount: Int = 0
) 