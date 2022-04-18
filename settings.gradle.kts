pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    val micronautApplicationVersion: String by settings
    val quarkusVersion: String by settings
    val springBootVersion: String by settings
    repositories {
        mavenLocal()
        maven { url = uri("https://repo.spring.io/release") }
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        id("com.google.devtools.ksp") version kspVersion
        id("io.micronaut.application") version micronautApplicationVersion
        id("io.quarkus") version quarkusVersion
        id("io.quarkus.extension") version quarkusVersion
        id("org.springframework.boot") version springBootVersion
    }
}

rootProject.name = "komapper"

// CORE
include("komapper-core")
include("komapper-tx-core")

// ANNOTATION & PROCESSOR
include("komapper-annotation")
include("komapper-processor")

// JDBC
include("komapper-datetime-jdbc")
include("komapper-jdbc")
include("komapper-micronaut-autoconfigure-jdbc")
include("komapper-micronaut-jdbc")
include("komapper-micronaut-starter-jdbc")
include("komapper-quarkus-jdbc")
include("komapper-quarkus-jdbc-deployment")
include("komapper-spring-boot-autoconfigure-jdbc")
include("komapper-spring-boot-starter-jdbc")
include("komapper-spring-jdbc")
include("komapper-spring-native-jdbc")
include("komapper-starter-jdbc")
include("komapper-tx-jdbc")

// R2DBC
include("komapper-datetime-r2dbc")
include("komapper-r2dbc")
include("komapper-spring-boot-autoconfigure-r2dbc")
include("komapper-spring-boot-starter-r2dbc")
include("komapper-spring-native-r2dbc")
include("komapper-spring-r2dbc")
include("komapper-starter-r2dbc")
include("komapper-tx-r2dbc")

// DIALECT
include("komapper-dialect-h2")
include("komapper-dialect-h2-jdbc")
include("komapper-dialect-h2-r2dbc")
include("komapper-dialect-mariadb")
include("komapper-dialect-mariadb-jdbc")
include("komapper-dialect-mariadb-r2dbc")
include("komapper-dialect-mysql")
include("komapper-dialect-mysql-jdbc")
include("komapper-dialect-mysql-r2dbc")
include("komapper-dialect-oracle")
include("komapper-dialect-oracle-jdbc")
include("komapper-dialect-oracle-r2dbc")
include("komapper-dialect-postgresql")
include("komapper-dialect-postgresql-r2dbc")
include("komapper-dialect-postgresql-jdbc")
include("komapper-dialect-sqlserver")
include("komapper-dialect-sqlserver-jdbc")
include("komapper-dialect-sqlserver-r2dbc")

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
include("example-micronaut-jdbc")
include("example-spring-boot-jdbc")
include("example-spring-boot-r2dbc")
include("example-starter-jdbc")
include("example-starter-r2dbc")

// TEST
include("integration-test-core")
include("integration-test-jdbc")
include("integration-test-r2dbc")
