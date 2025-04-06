package com.example.kakaoimagevideosearch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoDocument(
    @SerialName("author")
    val author: String,
    @SerialName("datetime")
    val datetime: String,
    @SerialName("display_sitename")
    val displaySitename: String,
    @SerialName("doc_url")
    val docUrl: String,
    @SerialName("play_time")
    val playTime: Int,
    @SerialName("thumbnail")
    val thumbnail: String,
    @SerialName("title")
    val title: String,
    @SerialName("url")
    val url: String
) 