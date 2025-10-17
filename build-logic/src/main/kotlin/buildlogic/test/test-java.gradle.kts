package buildlogic.test

import buildlogic.withVersionCatalog

plugins {
    id("buildlogic.test.test-core")
}

dependencies {
    withVersionCatalog {
        testImplementation(libs.assertj)
    }
}