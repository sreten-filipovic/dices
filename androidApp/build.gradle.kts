import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.cubes.template"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cubes.template"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BaseUrl", "\"https://jsonplaceholder.typicode.com/\"")
            // TODO: add signingConfig for release builds
        }

        debug {
            applicationIdSuffix = ".debug"
            buildConfigField("String", "BaseUrl", "\"https://jsonplaceholder.typicode.com/\"")
        }

        create("staging") {
            matchingFallbacks += listOf("debug")
            applicationIdSuffix = ".staging"
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("String", "BaseUrl", "\"https://jsonplaceholder.typicode.com/\"")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    bundle {
        language {
            enableSplit = false
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.datastore)
    implementation(libs.datastore.preferences)
}
