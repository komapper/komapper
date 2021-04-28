pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "komapper"

// CORE
include("komapper-core")

// KSP
include("komapper-processor")

// OPTIONAL
include("komapper-annotation")
include("komapper-template")
include("komapper-transaction")
include("komapper-logging-slf4j")

// JDBC
include("komapper-jdbc-h2")
include("komapper-jdbc-mysql")
include("komapper-jdbc-postgresql")

// EXTENSION
include("komapper-ext-spring-boot")
include("komapper-ext-spring-boot-starter")

// EXAMPLE
include("example-basic")
include("example-spring-boot")

// TEST
include("integration-test")
include("komapper-ext-sqlcommenter")
