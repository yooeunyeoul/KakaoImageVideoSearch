package com.example.kakaoimagevideosearch.data.model

import com.google.gson.annotations.SerializedName

data class VideoSearchResponse(
    @SerializedName("documents")
    val documents: List<VideoDocument> = emptyList(),
    @SerializedName("meta")
    val meta: VideoMeta = VideoMeta()
) 