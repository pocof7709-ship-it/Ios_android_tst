plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}
android {
    namespace = "com.iosexperience.feature.notifications"
    compileSdk = 35
    defaultConfig { minSdk = 31 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}
dependencies {
    implementation(platform(libs.compose.bom))
    implementation(project(":core-design"))
    implementation(project(":core-data"))
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
}
