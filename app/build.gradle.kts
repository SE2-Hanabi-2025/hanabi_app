plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("jacoco")
    id("org.sonarqube") version "5.1.0.4882"

    kotlin("plugin.serialization") version "2.1.20" // Replace with the latest version
}

android {
    namespace = "se2.hanabi.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "se2.hanabi.app"
        minSdk = 30
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    //
    testOptions {
        unitTests {
            all {
                it.useJUnitPlatform()
                it.finalizedBy(tasks.named("jacocoTestReport"))
            }
        }
    }
    tasks.register<JacocoReport>("jacocoTestReport") {
        group = "verification"
        description = "Generates code coverage report for the test task."
        dependsOn("testDebugUnitTest")

        reports {
            xml.required.set(true)
            xml.outputLocation.set(file("${project.projectDir}/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"))
        }

        val fileFilter = listOf(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*"
        )

        val debugTree =
            fileTree("${project.layout.buildDirectory.get().asFile}/tmp/kotlin-classes/debug") {
                exclude(fileFilter)
            }

        val javaDebugTree =
            fileTree("${project.layout.buildDirectory.get().asFile}/intermediates/javac/debug") {
                exclude(fileFilter)
            }

        val mainSrc = listOf(
            "${project.projectDir}/src/main/java",
            "${project.projectDir}/src/main/kotlin"
        )

        sourceDirectories.setFrom(files(mainSrc))
        classDirectories.setFrom(files(debugTree, javaDebugTree))
        executionData.setFrom(fileTree(project.layout.buildDirectory.get().asFile) {
            include("jacoco/testDebugUnitTest.exec")
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        })
    }
}
sonar {
    properties {
        property("sonar.projectKey", "SE2-Hanabi-2025_hanabi_app")
        property("sonar.organization", "se2-hanabi-2025")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.java.coveragePlugin", "jacoco")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${project.projectDir}/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml"
        )
        // Exclude LobbyActivity.kt and StartMenue.kt from SonarQube quality gate
        property("sonar.exclusions", "**/se2/hanabi/app/**")
    }
}


dependencies {

    implementation(libs.ktor.client.content.negotiation)
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation(libs.kotlinx.serialization.json.v180)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.firebase.firestore.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter.api)  // HINZUFÜGEN
    testRuntimeOnly(libs.junit.jupiter.engine)  // HINZUFÜGEN
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)

    implementation(libs.kotlinx.serialization.json.v180)
    implementation("io.ktor:ktor-client-websockets:2.3.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
}
