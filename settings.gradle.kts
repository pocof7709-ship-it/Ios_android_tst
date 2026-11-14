pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "IOSExperienceAI"
include(
    ":app", ":core-data", ":core-design",
    ":feature-launcher", ":feature-notifications", ":feature-control-center",
    ":feature-lockscreen", ":feature-ai",
)
