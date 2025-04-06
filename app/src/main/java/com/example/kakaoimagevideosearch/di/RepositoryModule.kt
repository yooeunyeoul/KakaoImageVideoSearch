package com.example.kakaoimagevideosearch.di

import com.example.kakaoimagevideosearch.data.ItemRepository
import com.example.kakaoimagevideosearch.data.ItemRepositoryImpl
import com.example.kakaoimagevideosearch.data.repository.SearchPagingRepository
import com.example.kakaoimagevideosearch.domain.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    @Singleton
    fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository

    @Binds
    @Singleton
    fun bindSearchRepository(impl: SearchPagingRepository): SearchRepository
} 