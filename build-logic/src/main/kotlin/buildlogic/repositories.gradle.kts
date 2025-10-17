package buildlogic

//plugins {
//    `maven-publish`
//}

repositories {
    mavenCentral()
    mavenLocal()

    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        name = "SonatypeSnapshots"
    }
}
