import kotlin.io.path.toPath

plugins {
    `kotlin-dsl`
    `version-catalog`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    api(files(libs.javaClass.protectionDomain.codeSource.location.toURI().toPath()))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation(libs.gradle.versions)
    implementation(libs.protobuf.google)
    implementation(libs.publish.vanniktech)
    implementation("org.jetbrains.kotlin:kotlin-stdlib") {
        version {
            // force stdlib to avoid confusion with transitive dependencies
            strictly(libs.versions.kotlin.asProvider().get())
        }
    }
}

kotlin {
    jvmToolchain(25)
}
