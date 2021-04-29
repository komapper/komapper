pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "komapper"

// CORE
include("komapper-core")

// ANNOTATION & PROCESSOR
include("komapper-annotation")
include("komapper-processor")

// OPTIONAL
include("komapper-template")
include("komapper-transaction")

// JDBC
include("komapper-jdbc-h2")
include("komapper-jdbc-mysql")
include("komapper-jdbc-postgresql")

// EXTENSION
include("komapper-ext-slf4j")
include("komapper-ext-spring-boot-autoconfigure")
include("komapper-ext-spring-boot-starter")
include("komapper-ext-sqlcommenter")

// STARTER
include("komapper-starter")

// PLATFORM
include("komapper-platform")

// EXAMPLE
include("example-basic")
include("example-spring-boot")

// TEST
include("integration-test")
