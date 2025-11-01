plugins {
    id("buildlogic.repositories")
    id("buildlogic.grpc.grpc-java")
    id("buildlogic.test.test-java")
    id("buildlogic.github-package")
    `java-library`
}

dependencies {
    implementation(libs.pi4j.core)
    compileOnly(libs.jakarta.annotation)

    testImplementation(project(":pi4j-plugin-grpc-server"))
    testImplementation(libs.pi4j.plugin.mock)
    testImplementation(libs.grpc.testing)
    testImplementation(libs.kotlin.coroutines.core)
    testImplementation(libs.protobuf.kotlin)
    testImplementation(libs.grpc.kotlin.stub)
}
