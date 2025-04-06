package com.example.myapplication.di

import com.example.myapplication.data.ItemRepository
import com.example.myapplication.data.ItemRepositoryImpl
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
} 