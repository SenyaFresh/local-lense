plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.hse.edu.locallense"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.hse.edu.locallense"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val mapkitApiKey: String by rootProject.extra
        buildConfigField("String", "MAPKIT_API_KEY", "\"${mapkitApiKey}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.kotlinx.collections.immutable)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.compose.ui.text.google.fonts)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    implementation(libs.yandex.mapkit)

    implementation(project(":core:common"))
    implementation(project(":core:common-impl"))
    implementation(project(":core:components"))
    implementation(project(":core:presentation"))

    implementation(project(":geoar"))

    implementation(project(":features:placemarks"))
    implementation(project(":features:ar"))

    implementation(project(":data:placemarks"))

}