package buildlogic.grpc

import buildlogic.withVersionCatalog

plugins {
    java
}

dependencies {
    withVersionCatalog {
        runtimeOnly(libs.grpc.netty)
        implementation(libs.protobuf.java)
        implementation(libs.grpc.stub)
        implementation(libs.grpc.protobuf)
        implementation(libs.grpc.services)
        testImplementation(libs.grpc.testing)
        testImplementation(libs.grpc.inprocess)
    }
}
