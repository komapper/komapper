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
