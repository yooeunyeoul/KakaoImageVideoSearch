package com.example.myapplication.data.mapper

import com.example.myapplication.data.model.Item
import com.example.myapplication.domain.model.Item as DomainItem

fun Item.toDomain() = DomainItem(
    id = id,
    name = name,
    description = description
)

fun List<Item>.toDomain() = map { it.toDomain() } 