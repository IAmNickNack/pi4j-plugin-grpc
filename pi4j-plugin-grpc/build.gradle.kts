plugins {
    id("buildlogic.repositories")
    id("buildlogic.grpc.grpc-java")
    id("buildlogic.test.test-java")
    id("buildlogic.github-package")
    `java-library`
}

dependencies {
    implementation(libs.bundles.pi4j)
    implementation(libs.jakarta.annotation)

    testImplementation(project(":pi4j-plugin-grpc-server"))
    testImplementation(libs.grpc.testing)
    testImplementation(libs.kotlin.coroutines.core)
    testImplementation(libs.protobuf.kotlin)
    testImplementation(libs.grpc.kotlin.stub)
}
