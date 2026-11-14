plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}
android {
    namespace = "com.iosexperience.core.data"
    compileSdk = 35
    defaultConfig { minSdk = 31 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = false }
}
dependencies {
    implementation(libs.androidx.core.ktx)
    api(libs.coroutines.android)
    testImplementation(libs.junit)
}
