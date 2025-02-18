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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://packagecloud.io/biopassid/dlibwrapper/maven2")
        maven(url = "https://packagecloud.io/biopassid/FingerprintSDKAndroid/maven2")
    }
}

rootProject.name = "Fingerprint Android Demo"
include(":app")
