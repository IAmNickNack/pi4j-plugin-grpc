rootProject.name = "pi4j-plugin-grpc-examples"

pluginManagement {
    includeBuild("../build-logic") // if you have convention plugins
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

val pluginVersion: String = settings.providers.gradleProperty("pi4j-plugin-grpc.version").get()

println("Using pi4j-plugin-grpc version: $pluginVersion")

includeBuild("..") {
    dependencySubstitution {
        substitute(module("io.github.iamnicknack:pi4j-plugin-grpc:$pluginVersion"))
            .using(project(":pi4j-plugin-grpc"))
    }
}

include(
    "basic-i2c",
    "gpio-events",
    "seven-segment"
)

