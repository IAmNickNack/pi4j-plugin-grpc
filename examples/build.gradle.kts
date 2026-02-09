plugins {
    base
}

tasks.named("build") {
    dependsOn(subprojects.map { it.tasks.named("build") })
}

tasks.named("clean") {
    dependsOn(subprojects.map { it.tasks.named("clean") })
}
