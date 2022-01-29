@file:Suppress("UnstableApiUsage")

plugins {
    `jvm-test-suite`
}

dependencies {
    api(project(":integration-test-core"))
    api(project(":komapper-tx-r2dbc"))
    api(platform("org.testcontainers:testcontainers-bom:1.16.3"))
    api("org.jetbrains.kotlin:kotlin-test-junit5")
    api("org.testcontainers:r2dbc")
    api("org.testcontainers:mariadb")
    api("org.testcontainers:mssqlserver")
    api("org.testcontainers:mysql")
    api("org.testcontainers:oracle-xe")
    api("org.testcontainers:postgresql")
}

tasks {
    test {
        enabled = false
    }
    check {
        dependsOn(testing.suites.named("h2"))
    }
}

testing {
    suites {
        register("h2", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
            }
        }

        register("mariadb", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-mariadb-r2dbc"))
                runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.0.3")
            }
        }

        register("mysql", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-mysql-r2dbc"))
                runtimeOnly("mysql:mysql-connector-java:8.0.28")
            }
        }

        register("oracle", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("oracleLowPriority", JvmTestSuite::class) {
            setup("oracle", includeTags = arrayOf("lowPriority"))
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("oracleHighPriority", JvmTestSuite::class) {
            setup("oracle", excludeTags = arrayOf("lowPriority"))
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("postgresql", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-postgresql-r2dbc"))
            }
        }

        register("sqlserver", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-sqlserver-r2dbc"))
                runtimeOnly("com.microsoft.sqlserver:mssql-jdbc:9.4.1.jre11")
            }
        }
    }
}

fun JvmTestSuite.setup(driver: String, includeTags: Array<String> = emptyArray(), excludeTags: Array<String> = emptyArray()) {
    useJUnitJupiter()
    sources {
        java {
            setSrcDirs(listOf("src/test/kotlin"))
        }
        resources {
            setSrcDirs(listOf("src/test/resources"))
        }
    }
    targets {
        all {
            testTask.configure {
                val urlKey = "$driver.url"
                val url = project.property(urlKey) ?: throw GradleException("The $urlKey property is not found.")
                systemProperty("driver", driver)
                systemProperty("url", url)
                useJUnitPlatform {
                    includeTags(*includeTags)
                    excludeTags(*excludeTags)
                }
            }
        }
    }
}
