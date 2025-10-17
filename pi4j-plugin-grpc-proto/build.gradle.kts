plugins {
    id("buildlogic.repositories")
    id("buildlogic.grpc.grpc-kotlin")
    id("buildlogic.java-core")
    `java-library`
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.iamnicknack.pi4j"
            artifactId = "pi4j-plugin-grpc-proto"
            version = "0.0.1"
            from(components["java"])
        }
    }
}
