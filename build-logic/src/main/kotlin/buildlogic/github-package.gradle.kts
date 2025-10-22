package buildlogic

plugins {
    `maven-publish`
}

publishing {
    repositories {
        mavenLocal()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/iamnicknack/pi4j-plugin-grpc")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            groupId = "io.github.iamnicknack.pi4j"
            artifactId = project.name
            version = "0.0.1-SNAPSHOT"
            // Only publish the standard JAR. No shadow jars
            artifact(tasks.named<Jar>("jar").get())
        }
    }
}