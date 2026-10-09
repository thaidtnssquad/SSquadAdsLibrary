import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    namespace = "com.snake.squad.adslib"
    compileSdk = 37

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
    publishing {
        singleVariant("release")
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
                groupId = "com.snake.squad.adslib"
                artifactId = "AdsLib"
                version = "1.7.4"
            }
        }
    }
}

//noinspection UseTomlInstead
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Ads
    implementation("com.google.android.gms:play-services-ads:25.5.0")

    implementation("androidx.lifecycle:lifecycle-process:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime:2.11.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.11.0")
    annotationProcessor("androidx.lifecycle:lifecycle-compiler:2.11.0")
    annotationProcessor("androidx.lifecycle:lifecycle-common-java8:2.11.0")

    implementation("com.airbnb.android:lottie:6.7.1")
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.google.android.gms:play-services-ads-identifier:18.3.0")

    //cmp
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")

    //facebook sdk
    implementation("com.facebook.android:facebook-android-sdk:18.3.0")

    //mediation admob
    implementation("com.google.ads.mediation:pangle:8.3.0.4.0")
    implementation("com.google.ads.mediation:applovin:13.6.4.2")
    implementation("com.google.ads.mediation:facebook:6.22.0.1")
    implementation("com.google.ads.mediation:vungle:7.7.8.1")
    implementation("com.google.ads.mediation:mintegral:17.1.81.1")
    implementation("com.google.ads.mediation:ironsource:9.6.1.0")
    implementation("com.unity3d.ads:unity-ads:4.21.0")
    implementation("com.google.ads.mediation:unity:4.21.0.0")
    implementation("com.google.ads.mediation:inmobi:11.5.0.0")

    //rating
    implementation("com.github.ome450901:SimpleRatingBar:1.5.1")
    implementation ("com.google.code.gson:gson:2.14.0")

    // Billing
    implementation("com.android.billingclient:billing-ktx:9.1.0")

    //Solar Engine
    implementation("com.reyun.solar.engine.oversea:solar-engine-core:1.3.3")

    //Tenjin
    implementation("com.tenjin:android-sdk:2.0.0")

}