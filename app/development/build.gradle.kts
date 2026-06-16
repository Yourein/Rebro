plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "net.yourein.rebro"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "net.yourein.rebro.dev"   // ← 並存インストールの肝
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        versionNameSuffix = "-dev"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "DATABASE_NAME", "\"rebro-dev.db\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:application"))
    implementation(project(":interfaces"))   // モックが実装するインターフェース（モック追加時に使用）
    implementation(project(":model"))        // モックが扱うエンティティ
    implementation(libs.kotlinx.coroutines.core)  // Flow を返すフェイク用

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
}
