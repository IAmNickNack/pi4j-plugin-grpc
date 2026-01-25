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
    }
}
