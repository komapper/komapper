@file:Suppress("UnstableApiUsage")

plugins {
    `jvm-test-suite`
    alias(libs.plugins.ksp)
}

dependencies {
    api(project(":integration-test-core"))
    api(project(":komapper-tx-r2dbc"))
    api(project(":komapper-datetime-r2dbc"))
    api(project(":komapper-annotation"))
    ksp(project(":komapper-processor"))
    api(libs.r2dbc.pool)
    api(platform(libs.testcontainers.bom))
    api("org.jetbrains.kotlin:kotlin-test-junit5")
    api("org.testcontainers:testcontainers-r2dbc")
}

ksp {
    arg("komapper.namingStrategy", "lower_snake_case")
}

tasks {
    test {
        enabled = false
    }
    check {
        dependsOn(testing.suites.named("h2"))
    }
    register("checkAll") {
        dependsOn(
            testing.suites.named("h2"),
            testing.suites.named("mariadb"),
            testing.suites.named("mysql"),
            testing.suites.named("mysql5"),
            testing.suites.named("oracle"),
            testing.suites.named("postgresql"),
            testing.suites.named("sqlserver"),
        )
    }
}

testing {
    suites {
        register("h2", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project())
                runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
            }
        }

        register("mariadb", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project())
                implementation(libs.testcontainers.mariadb)
                implementation(project(":komapper-dialect-mariadb-r2dbc"))
                runtimeOnly(libs.jdbc.mariadb)
            }
        }

        register("mysql", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project())
                implementation(libs.testcontainers.mysql)
                implementation(project(":komapper-dialect-mysql-r2dbc"))
                runtimeOnly(libs.jdbc.mysql)
            }
        }

        register("mysql5", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project())
                implementation(libs.testcontainers.mysql)
                implementation(project(":komapper-dialect-mysql-r2dbc"))
                runtimeOnly(libs.jdbc.mysql)
            }
        }

        register("oracle", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project())
                implementation(libs.testcontainers.oracle)
                implementation(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("postgresql", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project())
                implementation(libs.testcontainers.postgresql)
                implementation(project(":komapper-dialect-postgresql-r2dbc"))
            }
        }

        register("sqlserver", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project())
                implementation(libs.testcontainers.sqlserver)
                implementation(project(":komapper-dialect-sqlserver-r2dbc"))
                runtimeOnly(libs.jdbc.sqlserver)
            }
        }
    }
}

fun JvmTestSuite.setup(identifier: String) {
    sources {
        java {
            setSrcDirs(listOf("src/test/kotlin", "build/generated/ksp/$identifier/kotlin"))
        }
        resources {
            setSrcDirs(listOf("src/test/resources"))
        }
    }
    targets {
        all {
            testTask.configure {
                val urlKey = "$identifier.url"
                val url = project.property(urlKey) ?: throw GradleException("The $urlKey property is not found.")
                systemProperty("identifier", identifier)
                systemProperty("url", url)
            }
        }
    }
}
