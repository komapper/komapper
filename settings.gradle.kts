pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
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
include("komapper-starter-jdbc")
include("komapper-tx-jdbc")

// R2DBC
include("komapper-datetime-r2dbc")
include("komapper-r2dbc")
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

// SPRING
include("komapper-spring")
include("komapper-spring-jdbc")
include("komapper-spring-r2dbc")
include("komapper-spring-boot-autoconfigure-jdbc")
include("komapper-spring-boot-autoconfigure-r2dbc")
include("komapper-spring-boot-starter-jdbc")
include("komapper-spring-boot-starter-r2dbc")
include("komapper-spring-boot-starter-test-jdbc")
include("komapper-spring-boot-starter-test-r2dbc")
include("komapper-spring-boot-test-autoconfigure-jdbc")
include("komapper-spring-boot-test-autoconfigure-r2dbc")

// OPTIONAL
include("komapper-template")
include("komapper-codegen")
include("komapper-slf4j")
include("komapper-sqlcommenter")

// PLATFORM
include("komapper-platform")

// GRADLE PLUGIN
include("gradle-plugin")

// EXPERIMENTAL
include("komapper-quarkus-jdbc")
include("komapper-quarkus-jdbc-deployment")
include("komapper-tx-context-jdbc")
include("komapper-tx-context-r2dbc")
include("komapper-exposed")
include("komapper-exposed-jdbc")

// EXAMPLE
include("example-basic-jdbc")
include("example-basic-r2dbc")
include("example-spring-boot-jdbc")
include("example-spring-boot-r2dbc")
include("example-starter-jdbc")
include("example-starter-r2dbc")

// TEST
include("integration-test-core")
include("integration-test-jdbc")
include("integration-test-r2dbc")

include("komapper-exposed-r2dbc")
