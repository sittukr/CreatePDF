plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.edufun.createpdf"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.edufun.createpdf"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures{
        viewBinding = true
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.itextpdf:itextpdf:5.5.13.2")
    //implementation ("com.itextpdf:itext7-core:7.1.9")

    // Add the Bouncy Castle dependency for lock PDF
    implementation ("org.bouncycastle:bcprov-jdk15on:1.68")

    //for showing photos
    implementation ("com.squareup.picasso:picasso:2.8")
}