pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "komapper"
include("example")
include("komapper-core")
include("komapper-jdbc-h2")
// include("komapper-jdbc-postgresql")
include("komapper-processor")
