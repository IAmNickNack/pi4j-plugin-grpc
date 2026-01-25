package buildlogic

plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    coordinates(
        project.group as String,
        project.name,
        project.version as String
    )

    pom {
        name = "Pi4j gRPC Plugin"
        description = "A gRPC plugin for Pi4J"
        url = "https://github.com/IAmNickNack/pi4j-plugin-grpc"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "iamnicknack"
                name = "I Am Nicknack"
                email = "62-opinion.leopard@icloud.com"
            }
        }
        scm {
            connection = "scm:git:git@github.com:IAmNickNack/pi4j-plugin-grpc.git"
            developerConnection = "scm:git:git@github.com:IAmNickNack/pi4j-plugin-grpc.git"
            url = "https://github.com/IAmNickNack/pi4j-plugin-grpc"
        }
    }

    signAllPublications()
    publishToMavenCentral()
}