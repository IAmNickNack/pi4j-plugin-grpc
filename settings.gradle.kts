rootProject.name = "pi4j-plugin-grpc"

// GRPC
include(":pi4j-plugin-grpc")
include(":pi4j-plugin-grpc-proto")
include(":pi4j-plugin-grpc-server")

project(":pi4j-plugin-grpc")
project(":pi4j-plugin-grpc-proto")
project(":pi4j-plugin-grpc-server")

// Examples
include(":gpio-events")
include(":basic-i2c")
include(":seven-segment")

project(":gpio-events").projectDir = file("examples/gpio-events")
project(":basic-i2c").projectDir = file("examples/basic-i2c")
project(":seven-segment").projectDir = file("examples/seven-segment")

includeBuild("build-logic")
