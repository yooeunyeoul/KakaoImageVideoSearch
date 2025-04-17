package com.example.kakaoimagevideosearch.data.model

import com.google.gson.annotations.SerializedName

data class VideoDocument(
    @SerializedName("author")
    val author: String = "",
    @SerializedName("datetime")
    val datetime: String = "",
    @SerializedName("play_time")
    val playTime: Int = 0,
    @SerializedName("thumbnail")
    val thumbnail: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("url")
    val url: String = ""
) 