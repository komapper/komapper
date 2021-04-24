pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "komapper"

// CORE and OPTIONAL PLUGIN
include("komapper-core")
include("komapper-annotation")
include("komapper-template")
include("komapper-transaction")
include("komapper-logging-slf4j")
include("komapper-processor")

// JDBC
include("komapper-jdbc-h2")
include("komapper-jdbc-mysql")
include("komapper-jdbc-postgresql")

// EXTENSION
include("komapper-extension-spring-boot")
include("komapper-extension-spring-boot-starter")

// EXAMPLE
include("example-basic")
include("example-spring-boot")

// TEST
include("integration-test")
