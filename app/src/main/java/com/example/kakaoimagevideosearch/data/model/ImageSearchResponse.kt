package com.example.kakaoimagevideosearch.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImageSearchResponse(
    @SerialName("documents")
    val documents: List<ImageDocument> = emptyList(),
    @SerialName("meta")
    val meta: ImageMeta = ImageMeta()
) 