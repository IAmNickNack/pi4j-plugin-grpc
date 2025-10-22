import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("buildlogic.repositories")
    id("buildlogic.kotlin-core")
    id("buildlogic.grpc.grpc")
    alias(libs.plugins.shadow)
    application
}

dependencies {
    implementation(project(":pi4j-plugin-grpc"))
    implementation(libs.pi4j.core)
    implementation(libs.pi4j.plugin.mock)
    implementation(libs.pi4j.plugin.ffm)
    implementation(libs.logback.classic)
}

application {
    mainClass.set("examples.events.GpioEventsKt")
    applicationDefaultJvmArgs = listOf(
        // currently required for grpc-netty until https://github.com/netty/netty/issues/14942
        // is incorporated into the grpc-netty artefact
        "--sun-misc-unsafe-memory-access=allow",
        // required for Pi4J
        "--enable-native-access=ALL-UNNAMED"
    )
}

tasks.withType<ShadowJar> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
}