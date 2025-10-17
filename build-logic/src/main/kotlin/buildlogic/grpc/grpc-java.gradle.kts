package buildlogic.grpc

import buildlogic.withVersionCatalog

plugins {
    id("com.google.protobuf")
    id("buildlogic.grpc.grpc")
}


withVersionCatalog {
    protobuf {
        protoc {
            artifact = libs.protoc.asProvider().get().toString()
        }
        plugins {
            create("grpc") {
                artifact = libs.protoc.gen.grpc.java.get().toString()
            }
        }
        generateProtoTasks {
            ofSourceSet("main").forEach {
                it.plugins {
                    create("grpc")
                }
            }
        }
    }
}
