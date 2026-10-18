
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.diyanetakademisi.widget"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.diyanetakademisi.widget"
        minSdk = 26
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"
    }
}
