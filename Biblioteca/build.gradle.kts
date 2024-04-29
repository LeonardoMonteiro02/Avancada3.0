plugins {
    alias(libs.plugins.androidLibrary)
    id ("maven-publish")
}

android {
    namespace = "com.example.biblioteca"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
publishing {
    publications {
        register("Avancada3.0", MavenPublication::class) {
            groupId = "com.example"
            artifactId = "biblioteca"
            version = "1.0"
            // Aqui você deve especificar o artefato que deseja publicar.
            // Por exemplo, se estiver publicando um AAR, você pode fazer algo assim:
            artifact("$buildDir/outputs/aar/Biblioteca-debug.aar")
        }
    }

    repositories {
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/LeonardoMonteiro02/Avancada3.0/tree/Leonardo")
            credentials {
                username = project.findProperty("usuario") as String? ?: ""
                password = project.findProperty("token") as String? ?: ""
            }
        }
    }
}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}