package buildlogic

plugins {
    kotlin("jvm")
    id("buildlogic.java-core")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }

    jvmToolchain(25)
}

dependencies {
    withVersionCatalog {
        implementation(platform(libs.kotlin.bom))
        implementation("org.jetbrains.kotlin:kotlin-stdlib") {
            version {
                // force stdlib to avoid confusion with transitive dependencies
                strictly(libs.versions.kotlin.asProvider().get())
            }
        }
    }
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}