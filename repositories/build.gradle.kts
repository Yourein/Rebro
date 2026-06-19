plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "net.yourein.rebro.repositories"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":model"))
    implementation(project(":interfaces"))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.retrofit.core)
    implementation(libs.okhttp)
    implementation(libs.xmlutil.serialization)
}
