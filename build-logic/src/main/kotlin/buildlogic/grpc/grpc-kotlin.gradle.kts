package buildlogic.grpc

import buildlogic.withVersionCatalog

plugins {
    kotlin("jvm")
    id("com.google.protobuf")
    id("buildlogic.grpc.grpc")
}

dependencies {
    withVersionCatalog {
        implementation(libs.kotlin.coroutines.core)
        implementation(libs.protobuf.kotlin)
        implementation(libs.grpc.kotlin.stub)
    }
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
    plugins {
        withVersionCatalog {
            create("grpc") {
                artifact = libs.protoc.gen.grpc.java.get().toString()
            }
            create("grpckt") {
                artifact = libs.protoc.gen.grpc.kotlin.get().toString() + ":jdk8@jar"
            }
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                create("grpc")
                create("grpckt")
            }
            it.builtins { create("kotlin") }
        }
    }
}