package com.example.kakaoimagevideosearch.di

import com.example.kakaoimagevideosearch.data.api.ApiConstants
import com.example.kakaoimagevideosearch.data.api.KakaoSearchApi
import com.example.kakaoimagevideosearch.utils.NetworkResultCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * [면접 예상 질문]
 * 
 * 1. 라이브러리 선택 근거 관련
 * - "Moshi를 사용하신 이유가 무엇인가요? Gson이나 Kotlinx.serialization 같은 다른 대안과 비교했을 때 어떤 장점이 있었나요?"
 * - "네트워크 통신을 위해 Retrofit을 선택하신 이유는 무엇인가요? 다른 대안(예: Ktor, Volley 등)과 비교했을 때 어떤 이점이 있나요?"
 *
 * 2. 아키텍처 및 패턴 관련
 * - "Hilt/Dagger를 사용하여 의존성 주입을 구현한 이유는 무엇인가요? Koin과 같은 서비스 로케이터 패턴과 비교했을 때 어떤 장점이 있었나요?"
 * - "NetworkModule을 별도로 분리한 아키텍처적 이유는 무엇인가요?"
 * - "SingletonComponent를 사용한 이유와 다른 스코프와 비교했을 때의 장단점을 설명해주세요."
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * [면접 예상 질문]
     * - "Moshi를 사용하신 이유가 무엇인가요? Gson이나 Kotlinx.serialization 같은 다른 대안과 비교했을 때 어떤 장점이 있었나요?"
     * - "KotlinJsonAdapterFactory를 사용한 이유는 무엇인가요? 코드젠(moshi-kotlin-codegen)과 비교했을 때 어떤 차이가 있나요?"
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory()) // Kotlin 리플렉션 지원
        .build()

    /**
     * [면접 예상 질문]
     * - "HttpLoggingInterceptor의 로깅 레벨을 BODY로 설정한 이유는 무엇이며, 프로덕션 환경에서는 어떻게 다르게 구성할 계획인가요?"
     * - "이 모듈에서 성능 최적화를 위해 어떤 고려사항들이 있었나요?"
     * - "네트워크 통신에서 발생할 수 있는 병목 현상을 어떻게 모니터링하고 해결할 계획인가요?"
     */
    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    /**
     * [면접 예상 질문]
     * - "NetworkResultCallAdapterFactory는 어떤 기능을 하며, 왜 직접 구현하셨나요?"
     * - "API 호출 실패 시 에러 처리는 어떻게 구현하셨나요? 어떤 패턴을 사용하셨나요?"
     * - "API 응답에 대한 캐싱 전략은 어떻게 설계하셨나요?"
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(NetworkResultCallAdapterFactory())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    /**
     * [면접 예상 질문]
     * - "새로운 API 엔드포인트가 추가될 때 이 모듈은 어떻게 확장할 수 있도록 설계하셨나요?"
     * - "API 응답 구조가 변경될 경우 어떻게 대응할 계획인가요?"
     * - "이 모듈의 테스트 전략은 어떻게 구성하셨나요? 단위 테스트와 통합 테스트는 어떻게 작성하셨나요?"
     */
    @Provides
    @Singleton
    fun provideKakaoSearchApi(retrofit: Retrofit): KakaoSearchApi =
        retrofit.create(KakaoSearchApi::class.java)
}


