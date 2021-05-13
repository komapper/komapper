pluginManagement {
    val kspVersion: String by settings
    repositories {
        gradlePluginPortal()
        google()
    }
    plugins {
        id("com.google.devtools.ksp") version kspVersion
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
include("komapper-codegen")

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

// GRADLE PLUGIN
include("gradle-plugin")

// EXAMPLE
include("example-minimum")
include("example-spring-boot")
include("example-starter")

// TEST
include("integration-test")
