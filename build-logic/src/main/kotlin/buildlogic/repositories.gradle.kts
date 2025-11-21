package buildlogic

repositories {
    mavenCentral()
    mavenLocal()

    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        name = "SonatypeSnapshots"
    }
}
