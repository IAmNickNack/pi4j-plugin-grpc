package buildlogic

plugins {
    java
}

dependencies {
    withVersionCatalog {
        testImplementation(libs.slf4j.simple)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}
