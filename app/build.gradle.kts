plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.zygy.roadlegends"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.zygy.roadlegends"
        minSdk = 23
        targetSdk = 35
        versionCode = 4
        versionName = "2.0"
    }
}
