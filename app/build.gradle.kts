plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.cs501_classgenie"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cs501_classgenie"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    implementation("com.google.http-client:google-http-client-gson:1.42.3"){
        exclude ("org.apache.httpcomponents")
    }

    implementation ("com.google.api-client:google-api-client-android:2.2.0"){
        exclude ("org.apache.httpcomponents")
    }
    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.30.1"){
        exclude ("org.apache.httpcomponents")
    }
    implementation ("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0"){
        exclude ("org.apache.httpcomponents")
    }

    implementation("com.google.auth:google-auth-library-oauth2-http:1.16.0"){
        exclude ("org.apache.httpcomponents")
    }
    implementation("com.google.android.gms:play-services-auth:20.4.1"){
        exclude ("org.apache.httpcomponents")
    }

    implementation("androidx.credentials:credentials:1.2.0")


    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")

    implementation ("androidx.room:room-runtime:2.4.2")
    implementation ("androidx.room:room-ktx:2.4.2")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    kapt ("androidx.room:room-compiler:2.4.2")
    implementation ("androidx.activity:activity-ktx:1.8.1")
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.databinding:viewbinding:8.1.2")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.1")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")
    implementation("androidx.cardview:cardview:1.0.0")

}

secrets {
    // To add your Maps API key to this project:
    // 1. Open the root project's local.properties file
    // 2. Add this line, where YOUR_API_KEY is your API key:
    //        MAPS_API_KEY=YOUR_API_KEY
    defaultPropertiesFileName = "local.defaults.properties"
}
