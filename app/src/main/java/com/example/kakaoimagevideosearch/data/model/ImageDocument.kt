package com.example.kakaoimagevideosearch.data.model

import com.google.gson.annotations.SerializedName

data class ImageDocument(
    @SerializedName("collection")
    val collection: String = "",
    @SerializedName("datetime")
    val datetime: String = "",
    @SerializedName("display_sitename")
    val displaySitename: String = "",
    @SerializedName("doc_url")
    val docUrl: String = "",
    @SerializedName("height")
    val height: Int = 0,
    @SerializedName("image_url")
    val imageUrl: String = "",
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String = "",
    @SerializedName("width")
    val width: Int = 0
) 