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
        applicationId = "net.yourein.rebro"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "DATABASE_NAME", "\"rebro.db\"")
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
    buildFeatures {
        buildConfig = true   // buildConfigField を使うのに必須
    }
}

dependencies {
    implementation(project(":core:application"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
}
