package com.example.kakaoimagevideosearch.data.model

import com.google.gson.annotations.SerializedName

data class ImageSearchResponse(
    @SerializedName("documents")
    val documents: List<ImageDocument> = emptyList(),
    @SerializedName("meta")
    val meta: ImageMeta = ImageMeta()
) 