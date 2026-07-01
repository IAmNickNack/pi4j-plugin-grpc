 package buildlogic

import com.github.benmanes.gradle.versions.VersionsPlugin
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

 /**
 * Basic dependency update check to be added to the root project.
 */
plugins {
    id("buildlogic.repositories")
}

allprojects {
    apply<VersionsPlugin>()

    tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
        checkForGradleUpdate = false

        val endings = listOf("alpha", "beta", "rc", "cr", "m", "preview")

        rejectVersionIf {
            endings.any { candidate.version.contains(it, ignoreCase = true) }
        }

        timeout = java.time.Duration.ofSeconds(5)
    }
}
