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
    kotlinOptions {
        jvmTarget = "17"
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
                version = "1.5.0"
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
    implementation("com.google.android.gms:play-services-ads:24.4.0")

    implementation("androidx.lifecycle:lifecycle-process:2.9.1")
    implementation("androidx.lifecycle:lifecycle-runtime:2.9.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.1")
    annotationProcessor("androidx.lifecycle:lifecycle-compiler:2.9.1")

    implementation("com.airbnb.android:lottie:6.6.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    //Adjust
    implementation("com.adjust.sdk:adjust-android:5.4.1")
    implementation("com.adjust.sdk:adjust-android-webbridge:5.4.1")
    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.google.android.gms:play-services-ads-identifier:18.2.0")

    //cmp
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")

    //facebook sdk
    implementation("com.facebook.android:facebook-android-sdk:18.0.3")

    //mediation admob
    implementation("com.google.ads.mediation:applovin:+")
    implementation("com.google.ads.mediation:facebook:+")

    //rating
    implementation("com.github.ome450901:SimpleRatingBar:1.5.1")
    implementation ("com.google.code.gson:gson:2.13.1")

}