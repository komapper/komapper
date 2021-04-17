pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "komapper"

include("komapper-core")
include("komapper-annotation")
include("komapper-processor")
include("komapper-template")

include("komapper-jdbc-h2")
include("komapper-jdbc-postgresql")

include("example")
include("integration-test")
