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

        register("oracle", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("reproduction1", JvmTestSuite::class) {
            setup("oracle", includeTags = arrayOf(name))
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("reproduction2", JvmTestSuite::class) {
            setup("oracle", includeTags = arrayOf(name))
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
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
