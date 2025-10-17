package buildlogic

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project

fun Project.withVersionCatalog(block: WithVersionCatalog.() -> Unit) {
    RealWithVersionCatalog(this).block()
}

val Project.librariesForLibs: LibrariesForLibs? get() {
    return (this as org.gradle.api.plugins.ExtensionAware)
        .extensions.getByName("libs") as? LibrariesForLibs
}

interface WithVersionCatalog {
    val project: Project
    val Project.libs: LibrariesForLibs
        get() = librariesForLibs ?: error("Version catalog 'libs' is not available in this project.")
}

private class RealWithVersionCatalog(
    override val project: Project
) : WithVersionCatalog
