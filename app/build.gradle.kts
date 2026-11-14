plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}
android {
    namespace = "com.iosexperience.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.iosexperience.app"
        minSdk = 31      // Android 12: RenderEffect/blur de janela; POCO F7 roda Android 15 (HyperOS 2)
        targetSdk = 35
        versionCode = 2; versionName = "0.2.0"
    }
    buildTypes { release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt")) } }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}
dependencies {
    implementation(project(":core-design"))
    implementation(project(":core-data"))
    implementation(project(":feature-launcher"))
    implementation(project(":feature-notifications"))
    implementation(project(":feature-control-center"))
    implementation(project(":feature-lockscreen"))
    implementation(project(":feature-ai"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
}
