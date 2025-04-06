package com.example.kakaoimagevideosearch.data.mapper

import com.example.kakaoimagevideosearch.data.model.Item
import com.example.kakaoimagevideosearch.domain.model.Item as DomainItem

fun Item.toDomain() = DomainItem(
    id = id,
    name = name,
    description = description
)

fun List<Item>.toDomain() = map { it.toDomain() } 