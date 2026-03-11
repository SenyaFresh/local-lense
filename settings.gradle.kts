pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.google.com")
        maven {
            url = uri("https://maven.pkg.github.com/yandex/mapkit-android-sdk")
        }
        maven {
            url = uri("https://artifactory.yandex.net/artifactory/public")
        }
    }
}

rootProject.name = "Local Lense"
include(":app")

include(":core")
include(":core:common")
include(":core:common-impl")
include(":core:presentation")
include(":core:components")

include(":geoar")

include(":features")
include(":features:placemarks")
include(":features:ar")
include(":data")
include(":data:placemarks")
