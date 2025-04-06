package com.example.kakaoimagevideosearch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoSearchResponse(
    @SerialName("documents")
    val documents: List<VideoDocument>,
    @SerialName("meta")
    val meta: VideoMeta
) 