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
    }
}

rootProject.name = "Rebro"
include(":app:production")
include(":app:development")
include(":core:application")
include(":core:compose")
include(":model")
include(":interfaces")
include(":repositories")
include(":core:navigation")
include(":core:resources")
include(":feature:search-top")
include(":feature:register-top")
include(":feature:bookshelfs")
include(":feature:authors")
include(":feature:bookshelf-detail")
include(":feature:book-detail")
include(":feature:author-detail")
