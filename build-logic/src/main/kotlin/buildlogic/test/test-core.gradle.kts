package buildlogic.test

import buildlogic.withVersionCatalog

plugins {
    id("buildlogic.java-core")
}

dependencies {
    withVersionCatalog {
        testImplementation(platform(libs.junit.bom))
        testImplementation("org.junit.jupiter:junit-jupiter-engine")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
