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
        
        register("composition", JvmTestSuite::class) {
            setup("oracle", includeTags = arrayOf(name))
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("literal", JvmTestSuite::class) {
            setup("oracle", includeTags = arrayOf(name))
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("operator", JvmTestSuite::class) {
            setup("oracle", includeTags = arrayOf(name))
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("quote", JvmTestSuite::class) {
            setup("oracle", includeTags = arrayOf(name))
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("dataType", JvmTestSuite::class) {
            setup("oracle", includeTags = arrayOf(name))
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("schema", JvmTestSuite::class) {
            setup("oracle", includeTags = arrayOf(name))
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("script", JvmTestSuite::class) {
            setup("oracle", includeTags = arrayOf(name))
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("valueClass", JvmTestSuite::class) {
            setup("oracle", includeTags = arrayOf(name))
            dependencies {
                implementation(project)
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("suspicious", JvmTestSuite::class) {
            setup("oracle", includeTags = arrayOf(name))
            dependencies {
                implementation(project)
                implementation("com.oracle.database.jdbc.debug:ojdbc11_g:21.3.0.0")
                runtimeOnly(project(":komapper-dialect-oracle-r2dbc"))
            }
        }

        register("trustworthy", JvmTestSuite::class) {
            setup("oracle", excludeTags = arrayOf(name))
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
                systemProperty("oracle.jdbc.Trace", true)
                val path = project.file("oracleLog.properties").path
                println(path)
                systemProperty("java.util.logging.config.file", path)
                useJUnitPlatform {
                    includeTags(*includeTags)
                    excludeTags(*excludeTags)
                }
            }
        }
    }
}
