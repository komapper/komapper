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
include("komapper-spring-boot-autoconfigure-jdbc")
include("komapper-spring-boot-starter-jdbc")
include("komapper-starter-jdbc")
include("komapper-tx-jdbc")

// R2DBC
include("komapper-r2dbc")
include("komapper-tx-r2dbc")

// DIALECT
include("komapper-dialect-h2")
include("komapper-dialect-h2-jdbc")
include("komapper-dialect-h2-r2dbc")
include("komapper-dialect-mysql-jdbc")
include("komapper-dialect-postgresql-jdbc")

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
include("example-basic-jdbc")
include("example-basic-r2dbc")
include("example-spring-boot-jdbc")
include("example-starter-jdbc")

// TEST
include("integration-test-jdbc")
include("integration-test-r2dbc")
