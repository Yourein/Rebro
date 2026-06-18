plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "net.yourein.rebro.core.application"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:resources"))
    implementation(project(":core:navigation"))
    implementation(project(":model"))
    implementation(project(":interfaces"))
    implementation(project(":repositories"))   // 本番 Repository の実 Impl を登録するため
    implementation(project(":usecase"))

    implementation(project(":feature:search"))
    implementation(project(":feature:search-top"))
    implementation(project(":feature:register-top"))
    implementation(project(":feature:book-detail"))

    implementation(libs.androidx.room.runtime)  // AppDatabase を構築するため

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
