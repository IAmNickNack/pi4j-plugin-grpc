package buildlogic

plugins {
    `maven-publish`
}

val sourceSets = extensions.getByType(SourceSetContainer::class)
val sourcesJar by tasks.registering(org.gradle.jvm.tasks.Jar::class) {
    archiveClassifier.set("sources")
    from(
        fileTree("src/main/java") {
            include("**/*.java")
        },
    )
}

val javadocTask = tasks.named("javadoc", Javadoc::class)
val javadocJar by tasks.registering(org.gradle.jvm.tasks.Jar::class) {
    dependsOn(javadocTask)
    archiveClassifier.set("javadoc")
    from(javadocTask.get().destinationDir)
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

        // Sonatype OSSRH (Maven Central)
        maven {
            name = "MavenCentral"
            val isSnapshot = project.version.toString().endsWith("-SNAPSHOT")
            url = uri(
                if (isSnapshot)
                    "https://oss.sonatype.org/content/repositories/snapshots/"
                else
                    "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            )
            credentials {
                username = project.findProperty("ossrh.username") as String? ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrh.password") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
    publications {
        register<MavenPublication>("maven") {
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String
            from(components["java"])
            artifact(sourcesJar.get())
            artifact(javadocJar.get())
        }
    }
}