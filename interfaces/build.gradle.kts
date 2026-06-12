plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "net.yourein.rebro.interfaces"
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
    implementation(libs.kotlinx.coroutines.core)
}
