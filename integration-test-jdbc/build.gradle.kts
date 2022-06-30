@file:Suppress("UnstableApiUsage")

plugins {
    `jvm-test-suite`
    idea
    id("com.google.devtools.ksp")
}

dependencies {
    api(project(":integration-test-core"))
    api(project(":komapper-tx-jdbc"))
    api(project(":komapper-datetime-jdbc"))
    api(project(":komapper-annotation"))
    ksp(project(":komapper-processor"))
    api(project(":komapper-codegen"))
    api(platform("org.testcontainers:testcontainers-bom:1.17.3"))
    api("org.jetbrains.kotlin:kotlin-test-junit5")
}

idea {
    module {
        sourceDirs = sourceDirs + file("build/generated/ksp/main/kotlin")
        testSourceDirs = testSourceDirs + file("build/generated/ksp/test/kotlin")
        generatedSourceDirs = generatedSourceDirs + file("build/generated/ksp/main/kotlin") + file("build/generated/ksp/test/kotlin")
    }
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

        register("oracle", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project)
                runtimeOnly("org.testcontainers:oracle-xe")
                runtimeOnly(project(":komapper-dialect-oracle-jdbc"))
            }
        }

        register("postgresql", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project)
                implementation("org.postgresql:postgresql:42.4.0")
                runtimeOnly("org.testcontainers:postgresql")
                implementation(project(":komapper-dialect-postgresql-jdbc"))
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
            setSrcDirs(listOf("src/test/kotlin", "build/generated/ksp/$driver/kotlin"))
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
            }
        }
    }
}
