pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    val springBootVersion: String by settings
    repositories {
        gradlePluginPortal()
        google()
    }
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion
        id("com.google.devtools.ksp") version kspVersion
        id("org.springframework.boot") version springBootVersion
    }
}

rootProject.name = "komapper"

// CORE
include("komapper-core")

// ANNOTATION & PROCESSOR
include("komapper-annotation")
include("komapper-processor")

// JDBC
include("komapper-jdbc")
include("komapper-jdbc-spring-boot-autoconfigure")
include("komapper-jdbc-spring-boot-starter")
include("komapper-jdbc-starter")
include("komapper-jdbc-tx")

// JDBC DIALECT
include("komapper-jdbc-dialect-h2")
include("komapper-jdbc-dialect-mysql")
include("komapper-jdbc-dialect-postgresql")

// OPTIONAL
include("komapper-template")
include("komapper-codegen")
include("komapper-slf4j")
include("komapper-sqlcommenter")

// PLATFORM
include("komapper-platform")

// GRADLE PLUGIN
include("gradle-plugin")

// EXAMPLE
include("example-jdbc-minimum")
include("example-jdbc-spring-boot")
include("example-jdbc-starter")

// TEST
include("integration-test-jdbc")
