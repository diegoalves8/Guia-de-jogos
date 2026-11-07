import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.guiadejogos.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.guiadejogos.app"
        minSdk = 26          // Android 8.0+ (celulares, tablets como o Galaxy Tab S6 Lite e multimídia BYD DiLink 3.0+/Android 9)
        targetSdk = 35
        // No GitHub Actions cada build ganha um número novo (usado pela atualização dentro do app).
        val buildNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 0
        versionCode = 100 + buildNumber
        versionName = "1.4.$buildNumber"

        // URL base do conteúdo remoto (atualização sem gerar novo APK).
        // Deixe "" para usar somente o conteúdo embutido no app.
        // Veja docs/ATUALIZAR_CONTEUDO.md.
        // Repositório usado para procurar versões novas do app (GitHub Releases).
        buildConfigField("String", "UPDATE_REPO", "\"diegoalves8/Guia-de-jogos\"")
        buildConfigField(
            "String",
            "REMOTE_CONTENT_BASE_URL",
            "\"https://raw.githubusercontent.com/diegoalves8/Guia-de-jogos/main/app/src/main/assets/content/\""
        )
    }

    // Assinatura fixa: o mesmo certificado em todo build, para permitir atualizar por cima.
    val keyProps = Properties().apply {
        val f = rootProject.file("keystore/keystore.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }
    signingConfigs {
        create("fixed") {
            storeFile = file(keyProps.getProperty("storeFile", "../keystore/guia.jks"))
            storePassword = keyProps.getProperty("storePassword")
            keyAlias = keyProps.getProperty("keyAlias")
            keyPassword = keyProps.getProperty("keyPassword")
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("fixed")
            applicationIdSuffix = ".debug"
        }
        release {
            // Código otimizado e APK menor: roda melhor em centrais multimídia mais modestas.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("fixed")
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
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose) // carregamento e cache de imagens
    debugImplementation(libs.androidx.ui.tooling)
}
