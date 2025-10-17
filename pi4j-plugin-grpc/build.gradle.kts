plugins {
    id("buildlogic.repositories")
    id("buildlogic.grpc.grpc-java")
    id("buildlogic.test.test-java")
    `java-library`
    `maven-publish`

}

dependencies {
    implementation(libs.bundles.pi4j)

    testImplementation(project(":pi4j-plugin-grpc-server"))
    testImplementation(libs.grpc.testing)
    testImplementation(libs.kotlin.coroutines.core)
    testImplementation(libs.protobuf.kotlin)
    testImplementation(libs.grpc.kotlin.stub)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.iamnicknack.pi4j"
            artifactId = "pi4j-plugin-grpc"
            version = "0.0.1"
            from(components["java"])
        }
    }
}
