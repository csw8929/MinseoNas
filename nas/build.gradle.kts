plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "com.csw8929.minseo.nas"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.csw8929.minseo"
            artifactId = "nas"
            version = "0.1.0"
            afterEvaluate { from(components["release"]) }
        }
    }
}

dependencies {
    testImplementation(libs.junit)
}
