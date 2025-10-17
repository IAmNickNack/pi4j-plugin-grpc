package buildlogic.test

import buildlogic.withVersionCatalog

plugins {
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-core")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    withVersionCatalog {
        testImplementation(libs.kotlin.coroutines.test)
        testImplementation(libs.assertk)
    }
}