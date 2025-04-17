plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    kotlin("kapt")
}

android {
    namespace = "com.example.kakaoimagevideosearch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kakaoimagevideosearch"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        
        // Room Schema 경로 설정
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // 🔷 AndroidX Core & Lifecycle 🔷
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // 🔷 Jetpack Compose 🔷
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // 🔷 Navigation & Hilt 🔷
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // 🔷 Mavericks (MVI & State Management) 🔷
    implementation(libs.mavericks)
    implementation(libs.mavericks.compose)
    implementation(libs.mavericks.hilt)
    implementation(libs.mavericks.navigation)

    // 🔷 Network & Retrofit 🔷
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp3.logging.interceptor)
    // implementation(libs.retrofit2.kotlinx.serialization.converter)
    // implementation(libs.kotlinx.serialization.json)
    
    // Gson 의존성 추가
    implementation(libs.gson)
    implementation(libs.converter.gson)

    // 🔷 Room Database 🔷
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    kapt(libs.androidx.room.compiler)

    // 🔷 Image Loading 🔷
    implementation(libs.coil.compose)

    // 🔷 Paging 3 🔷
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)

    // 🔷 Compose Accompanist 🔷
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")

    // 🔷 Unit Testing Dependencies 🔷
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.mavericks.testing)
    testImplementation(libs.mockwebserver)

    // 🔷 Android Instrumentation Tests 🔷
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.mockwebserver)

    // 🔷 Annotation Processors & KAPT 🔷
    kaptTest(libs.hilt.compiler)
    kaptAndroidTest(libs.hilt.compiler)
    androidTestAnnotationProcessor(libs.hilt.compiler)
    testAnnotationProcessor(libs.hilt.compiler)
}