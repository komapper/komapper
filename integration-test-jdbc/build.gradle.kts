@file:Suppress("UnstableApiUsage")

plugins {
    `jvm-test-suite`
}

dependencies {
    api(project(":integration-test-core"))
    api(project(":komapper-tx-jdbc"))
    api("org.postgresql:postgresql:42.3.1")
    api(platform("org.testcontainers:testcontainers-bom:1.16.3"))
    api("org.jetbrains.kotlin:kotlin-test-junit5")
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
                runtimeOnly(project(":komapper-dialect-h2-jdbc"))
            }
        }

        register("mariadb", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project)
                runtimeOnly("org.testcontainers:mariadb")
                runtimeOnly(project(":komapper-dialect-mariadb-jdbc"))
            }
        }

        register("mysql", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project)
                runtimeOnly("org.testcontainers:mysql")
                runtimeOnly(project(":komapper-dialect-mysql-jdbc"))
            }
        }

        register("postgresql", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project)
                runtimeOnly("org.testcontainers:postgresql")
                runtimeOnly(project(":komapper-dialect-postgresql-jdbc"))
            }
        }

        register("sqlserver", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project)
                runtimeOnly("org.testcontainers:mssqlserver")
                runtimeOnly(project(":komapper-dialect-sqlserver-jdbc"))
            }
        }
    }
}

fun JvmTestSuite.setup(driver: String) {
    useJUnitJupiter()
    sources {
        java {
            setSrcDirs(listOf("src/test/kotlin"))
        }
    }
    targets {
        all {
            testTask.configure {
                val urlKey = "$driver.url"
                val url = project.property(urlKey) ?: throw GradleException("The $urlKey property is not found.")
                systemProperty("driver", driver)
                systemProperty("url", url)
            }
        }
    }
}
