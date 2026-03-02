plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "ru.hse.edu.geoar"
    compileSdk {
        version = release(36)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = 26
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    api(libs.kotlinx.coroutines.core)
    api(libs.play.services.location)
    api(libs.ar.core)
    api(libs.arsceneview)
}