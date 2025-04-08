# Kakao Image Video Search

카카오 이미지/비디오 검색 API를 활용한 안드로이드 애플리케이션입니다.

## 프로젝트 구조

```
app/src/main/java/com/example/kakaoimagevideosearch/
├── base/                           # 기본 클래스
├── data/                           # 데이터 계층
│   ├── api/                        # API 관련 클래스
│   │   ├── KakaoSearchApi.kt      # 카카오 검색 API 인터페이스
│   │   └── ApiConstants.kt        # API 상수
│   ├── local/                      # 로컬 데이터 관련 클래스
│   │   ├── dao/                   # Data Access Objects
│   │   ├── entity/                # 데이터베이스 엔티티
│   │   ├── converter/             # Room 타입 컨버터
│   │   └── AppDatabase.kt        # Room 데이터베이스
│   ├── mapper/                     # 매퍼 클래스
│   ├── model/                      # 데이터 모델
│   ├── paging/                     # 페이징 관련 클래스
│   └── repository/                 # 리포지토리 구현체
│       ├── CachedSearchRepository.kt    # 캐시 지원 검색 리포지토리
│       ├── BookmarkRepositoryImpl.kt    # 북마크 리포지토리 구현
│       └── KakaoSearchRepositoryImpl.kt # 카카오 검색 리포지토리 구현
├── di/                             # 의존성 주입
├── domain/                         # 도메인 계층
│   ├── model/                      # 도메인 모델
│   └── repository/                 # 리포지토리 인터페이스
│       ├── SearchRepository.kt     # 검색 리포지토리 인터페이스
│       ├── BookmarkRepository.kt   # 북마크 리포지토리 인터페이스
│       └── KakaoSearchRepository.kt # 카카오 검색 리포지토리 인터페이스
├── presentation/                   # 프레젠테이션 계층
│   ├── bookmark/                   # 북마크 관련 화면
│   ├── common/                     # 공통 컴포넌트
│   └── search/                     # 검색 관련 화면
├── ui/                             # UI 관련 클래스
├── utils/                          # 유틸리티 클래스
├── MainActivity.kt                 # 메인 액티비티
└── MyApplication.kt                # 애플리케이션 클래스
```

## 아키텍처

이 프로젝트는 Clean Architecture 패턴을 따르며, 다음과 같은 계층으로 구성되어 있습니다:

### 1. 프레젠테이션 계층 (Presentation Layer)
- 사용자 입력 처리 및 화면 표시
- MvRx를 사용한 상태 관리

### 2. 도메인 계층 (Domain Layer)
- 리포지토리 인터페이스
- 도메인 모델

### 3. 데이터 계층 (Data Layer)
- 리포지토리 구현
- API 통신 (KakaoSearchApi)
- 로컬 데이터베이스 (Room)
- 데이터 모델 및 매핑

## 주요 기능

1. 이미지/비디오 검색
   - 카카오 API를 통한 검색
   - 페이징 처리
   - 캐싱 지원

2. 북마크 관리
   - 검색 결과 북마크
   - 북마크 목록 조회
   - 북마크 삭제

3. 오프라인 지원
   - Room 데이터베이스를 통한 로컬 캐싱
   - 캐시 만료 시간 관리

## 사용된 기술

- Kotlin
- Android Jetpack
- Hilt (의존성 주입)
- Room (로컬 데이터베이스)
- Retrofit (네트워크 통신)
- MvRx (상태 관리)
- Paging3 (페이징 처리)
- Coroutines (비동기 처리)
- Flow (반응형 프로그래밍)

## 데이터 흐름

1. 검색 요청
   ```
   UI -> ViewModel -> CachedSearchRepository -> KakaoSearchApi
   ```

2. 캐시 처리
   ```
   CachedSearchRepository -> Room Database
   ```

3. 북마크 처리
   ```
   UI -> ViewModel -> BookmarkRepository -> Room Database
   