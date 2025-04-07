package com.example.kakaoimagevideosearch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoDocument(
    @SerialName("author")
    val author: String = "",
    @SerialName("datetime")
    val datetime: String = "",
    @SerialName("play_time")
    val playTime: Int = 0,
    @SerialName("thumbnail")
    val thumbnail: String = "",
    @SerialName("title")
    val title: String = "",
    @SerialName("url")
    val url: String = ""
) 