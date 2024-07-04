plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.snake.squad.adslib"
    compileSdk = 34

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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
                version = "1.0.6"
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
    implementation("com.applovin:applovin-sdk:12.5.0")
    implementation("com.google.android.gms:play-services-ads:23.1.0")

    implementation("androidx.lifecycle:lifecycle-process:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime:2.8.2")

    implementation("com.airbnb.android:lottie:6.4.1")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.orhanobut:hawk:2.0.1")

    //Adjust
    implementation("com.adjust.sdk:adjust-android:4.38.4")
    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.google.android.gms:play-services-ads-identifier:18.1.0")
    implementation("com.adjust.sdk:adjust-android-webbridge:4.38.4")

    //cmp
    implementation("com.google.android.ump:user-messaging-platform:2.2.0")

    //facebook sdk
    implementation("com.facebook.android:facebook-android-sdk:16.0.0")

    //admob mediation
    implementation("com.google.ads.mediation:pangle:6.0.0.7.0")
    implementation("com.google.ads.mediation:applovin:12.5.0.1")
    implementation("com.google.ads.mediation:facebook:6.17.0.0")
    implementation("com.google.ads.mediation:ironsource:8.1.0.0")
    implementation("com.google.ads.mediation:vungle:7.3.2.0")
    implementation("com.google.ads.mediation:mintegral:16.7.61.0")

    //max mediation
    implementation("com.applovin.mediation:bidmachine-adapter:+")
    implementation("com.applovin.mediation:google-ad-manager-adapter:+")
    implementation("com.applovin.mediation:google-adapter:+")
    implementation("com.applovin.mediation:inmobi-adapter:+")
    implementation("com.applovin.mediation:ironsource-adapter:+")
    implementation("com.applovin.mediation:facebook-adapter:+")
    implementation("com.applovin.mediation:mintegral-adapter:+")
    implementation("com.applovin.mediation:bytedance-adapter:+")
    implementation("com.applovin.mediation:vungle-adapter:+")

    //rating
    implementation("com.google.android.play:review:2.0.1")
    implementation("com.github.ome450901:SimpleRatingBar:1.5.1")

}