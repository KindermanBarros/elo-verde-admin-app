plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose"); id("com.google.gms.google-services") }

val releaseVersion = providers.gradleProperty("releaseVersion").orElse("0.0.0").get()
val releaseCode = providers.gradleProperty("releaseCode").orElse("1").get().toInt()

android { namespace = "com.eloverde.admin"; compileSdk = 35
    defaultConfig { applicationId = "com.eloverde.admin"; minSdk = 26; targetSdk = 35
        versionCode = releaseCode
        versionName = releaseVersion
    }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("com.google.firebase:firebase-auth-ktx:23.2.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.1")
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}