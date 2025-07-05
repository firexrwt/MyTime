plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.firexrwtinc.mytime"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.firexrwtinc.mytime"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {
    implementation(libs.androidx.core.ktx) // Основная библиотека Core
    implementation(libs.androidx.activity.compose) // Для ComponentActivity с Compose

    // Зависимости Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx) // <<--- ДОБАВЛЕНО: Важно для LiveData

    // Зависимости Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.runtime.livedata) // Для observeAsState с LiveData

    // Навигация
    implementation(libs.androidx.navigation.compose)
    // implementation(libs.androidx.navigation.fragment) // Если не используете навигацию через фрагменты, можно удалить

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.navigation.fragment)
    kapt(libs.androidx.room.compiler) // Используйте kapt для annotationProcessor если он объявлен так в libs.versions.toml
    // annotationProcessor(libs.androidx.room.compiler) // Если kapt не используется или объявлен отдельно

    // Другие ваши зависимости
    implementation(libs.material) // Google Material Components (для Material 1/2, если нужны)
    // implementation(libs.play.services.gcm) // Устаревшая библиотека, рассмотрите Firebase Cloud Messaging (FCM)

    // Тестовые зависимости
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}