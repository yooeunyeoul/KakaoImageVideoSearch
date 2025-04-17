package com.example.kakaoimagevideosearch.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ImageSearchResponse(
    @Json(name = "documents")
    val documents: List<ImageDocument> = emptyList(),
    @Json(name = "meta")
    val meta: ImageMeta = ImageMeta()
) 