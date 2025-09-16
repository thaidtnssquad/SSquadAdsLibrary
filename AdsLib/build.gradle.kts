import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.snake.squad.adslib"
    compileSdk = 35

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
                groupId = "com.snake.squad.adslib"
                artifactId = "AdsLib"
                version = "1.5.4"
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Ads
    implementation("com.applovin:applovin-sdk:13.2.0")
    implementation("com.google.android.gms:play-services-ads:24.6.0")

    implementation("androidx.lifecycle:lifecycle-process:2.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime:2.9.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.3")
    annotationProcessor("androidx.lifecycle:lifecycle-compiler:2.9.3")

    implementation("com.airbnb.android:lottie:6.6.7")
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    //Adjust
    implementation("com.adjust.sdk:adjust-android:5.4.1")
    implementation("com.adjust.sdk:adjust-android-webbridge:5.4.1")
    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.google.android.gms:play-services-ads-identifier:18.2.0")

    //cmp
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")

    //facebook sdk
    implementation("com.facebook.android:facebook-android-sdk:18.1.3")

    //mediation admob
    implementation("com.google.ads.mediation:pangle:7.5.0.4.0")
    implementation("com.google.ads.mediation:applovin:13.4.0.0")
    implementation("com.google.ads.mediation:facebook:6.20.0.0")
    implementation("com.google.ads.mediation:vungle:7.5.1.0")
    implementation("com.google.ads.mediation:mintegral:16.9.91.1")
    implementation("com.google.ads.mediation:ironsource:8.11.1.0")
    implementation("com.unity3d.ads:unity-ads:4.16.1")
    implementation("com.google.ads.mediation:unity:4.16.1.0")

    //rating
    implementation("com.github.ome450901:SimpleRatingBar:1.5.1")
    implementation ("com.google.code.gson:gson:2.13.2")

    //Solar Engine
    implementation("com.reyun.solar.engine.oversea:solar-engine-core:1.3.0.5")

}