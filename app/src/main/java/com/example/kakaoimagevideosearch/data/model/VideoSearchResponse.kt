package com.example.kakaoimagevideosearch.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoSearchResponse(
    @Json(name = "documents")
    val documents: List<VideoDocument> = emptyList(),
    @Json(name = "meta")
    val meta: VideoMeta = VideoMeta()
) 