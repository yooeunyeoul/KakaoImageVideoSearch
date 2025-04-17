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
 *
 *  Moshi가 Gson보다 좋은 이유:
Kotlin의 null 안전성(nullable/non-nullable)을 더 정확하게 반영하여 앱 안정성을 높입니다.
즉, 개발 의도대로 필드가 절대 null이면 안 되는 경우에 확실히 에러를 발생시켜 문제를 조기에 발견할 수 있습니다.
코드젠이 리플렉션보다 좋은 이유:
정확히 말씀하신 대로, 리플렉션은 런타임에 매번 객체 구조를 분석하지만, 코드젠은 컴파일 시점에 미리 파싱 코드를 생성해두기 때문에 더 빠릅니다.
특히 데이터가 많을수록 성능 차이가 커집니다.
면접에서는 이 두 가지 장점을 명확히 설명하시면 Moshi를 선택한 이유를 잘 전달할 수 있을 것 같습니다!
 *
 * - "네트워크 통신을 위해 Retrofit을 선택하신 이유는 무엇인가요? 다른 대안(예: Ktor, Volley 등)과 비교했을 때 어떤 이점이 있나요?"
 * 
 * Ktor 대신 Retrofit을 선택한 이유는, Ktor가 멀티플랫폼 지원을 핵심 강점으로 하고 있고 매우 유연한 커스터마이징 기능을 제공하는 훌륭한 라이브러리지만, 제가 진행했던 프로젝트는 안드로이드 전용이었고 표준적인 REST API 통신 외에 HTTP 통신을 깊이 있게 커스터마이징할 필요성은 낮았기 때문입니다.

//따라서 해당 프로젝트의 요구사항과 개발 환경을 고려했을 때, Retrofit의 검증된 안정성, 풍부한 자료, 그리고 표준 API 구현의 간결함이 더 적합하다고 판단했습니다."
//단순한 토큰 헤더 추가 수준을 넘어서, 여러 단계의 Handshake가 필요한 OAuth 플로우를 클라이언트 내에서 직접 처리해야 하거나, 요청 본문의 특정 데이터를 기반으로 동적으로 암호화된 서명을 생성하여 헤더에 포함시켜야 하는 등의 복잡한 인증 방식을 구현해야 할 때, Ktor의 Feature 시스템을 사용하면 관련 로직을 깔끔하게 모듈화하여 적용하기 용이할 수 있습니다.
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
     */
    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
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


