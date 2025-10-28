plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
}

android {
    namespace = "com.movil.saferescue"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.movil.saferescue"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.9-Pre-Alpha"

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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.saveable)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Dependencia para iconos de Material Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Dependencia CRUCIAL para WindowSizeClass
    implementation("androidx.compose.material3:material3-window-size-class:1.1.2") // (Verifica la versión más reciente, pero esta es estable)

    // Dependencia para rememberSaveable
    implementation("androidx.compose.runtime:runtime-saveable")

    // Necesario para que Theme.SplashScreen sea reconocido
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Dentro del bloque dependencies { ... }
    implementation("androidx.appcompat:appcompat:1.6.1") // O la versión más reciente

    // Implementación de Material para compatibilidad de estilos XML
    implementation("com.google.android.material:material:1.12.0")

    //Implementación de libreria para habilitar la navegación en aplicaciones Android que utilizan Jetpack Compose
    implementation("androidx.navigation:navigation-compose:2.9.5")

    //Implementación de libreria para integrar y utilizar ViewModels de la arquitectura de Android con Jetpack Compose.
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")

    // observar y reaccionar a los cambios en el ciclo de vida de Android dentro de tus funciones Composable
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")

    //Implementación de libreria para coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")


    //Implemetación para uso de base de datos y conexion a la misma
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Añade la librería Coil para cargar imágenes desde una URL en Compose
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("org.mindrot:jbcrypt:0.4")

    // Dependencia para servicios de ubicación de Google Play
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Implementación de OpenStreetMap (Librería principal)
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Dependencia para PreferenceManager
    implementation("androidx.preference:preference-ktx:1.2.1")


    //Importante despues sintonizar las librerias
}