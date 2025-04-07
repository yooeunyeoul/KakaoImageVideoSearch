package com.example.kakaoimagevideosearch.di

import com.example.kakaoimagevideosearch.data.repository.BookmarkRepositoryImpl
import com.example.kakaoimagevideosearch.data.repository.CachedSearchRepository
import com.example.kakaoimagevideosearch.data.repository.KakaoSearchRepositoryImpl
import com.example.kakaoimagevideosearch.domain.repository.BookmarkRepository
import com.example.kakaoimagevideosearch.domain.repository.KakaoSearchRepository
import com.example.kakaoimagevideosearch.domain.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindKakaoSearchRepository(
        kakaoSearchRepositoryImpl: KakaoSearchRepositoryImpl
    ): KakaoSearchRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        cachedSearchRepository: CachedSearchRepository
    ): SearchRepository
    
    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(
        bookmarkRepositoryImpl: BookmarkRepositoryImpl
    ): BookmarkRepository
} 