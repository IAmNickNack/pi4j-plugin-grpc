rootProject.name = "pi4j-plugin-grpc"

include(":pi4j-plugin-grpc")
include(":pi4j-plugin-grpc-server")

project(":pi4j-plugin-grpc")
project(":pi4j-plugin-grpc-server")

includeBuild("build-logic")
includeBuild("examples")
