plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.zygy.wallmix"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zygy.wallmix"
        minSdk = 28
        targetSdk = 35
        versionCode = 2
        versionName = "1.0"
    }
}
