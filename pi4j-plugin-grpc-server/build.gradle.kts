import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("buildlogic.repositories")
    id("buildlogic.grpc.grpc-kotlin")
    id("buildlogic.kotlin-core")
    id("buildlogic.test.test-kotlin")
    id("buildlogic.github-package")
    alias(libs.plugins.shadow)
    application
}

dependencies {
    implementation(project(":pi4j-plugin-grpc"))
    implementation(libs.pi4j.core)
    implementation(libs.pi4j.plugin.ffm)
    implementation(libs.pi4j.plugin.mock)
    runtimeOnly(libs.logback.classic)
    testImplementation(libs.grpc.testing)
}

tasks.named<JavaExec>("run") {
    mainClass = "io.github.iamnicknack.pi4j.grpc.server.Pi4jGrpcKt"
    jvmArgs(
        "--sun-misc-unsafe-memory-access=allow",
        "--enable-native-access=ALL-UNNAMED"
    )
}

application {
    mainClass.set("io.github.iamnicknack.pi4j.grpc.server.Pi4jGrpcKt")
    applicationDefaultJvmArgs = listOf(
        // currently required for grpc-netty until https://github.com/netty/netty/issues/14942
        // is incorporated into the grpc-netty artefact
        "--sun-misc-unsafe-memory-access=allow",
        // required for Pi4J
        "--enable-native-access=ALL-UNNAMED"
    )
}

tasks.withType<CreateStartScripts>().configureEach {
    // `defaultJvmOpts` is what the generated bin/* scripts will use
    defaultJvmOpts = application.applicationDefaultJvmArgs
}

tasks.withType<ShadowJar> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
}
