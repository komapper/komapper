@file:Suppress("UnstableApiUsage")

plugins {
    `jvm-test-suite`
    id("com.google.devtools.ksp")
}

dependencies {
    api(project(":integration-test-core"))
    api(project(":komapper-tx-jdbc"))
    api(project(":komapper-datetime-jdbc"))
    api(project(":komapper-annotation"))
    ksp(project(":komapper-processor"))
    api(project(":komapper-codegen"))
    api(platform("org.testcontainers:testcontainers-bom:1.19.3"))
    api("org.jetbrains.kotlin:kotlin-test-junit5")
}

ksp {
    arg("komapper.enableEntityMetamodelListing", "true")
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
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs += listOf(
            "-opt-in=org.komapper.annotation.KomapperExperimentalAssociation",
            "-Xcontext-receivers",
        )
    }
}

testing {
    suites {
        register("h2", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project())
                implementation
                runtimeOnly(project(":komapper-dialect-h2-jdbc"))
            }
        }

        register("mariadb", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project())
                runtimeOnly("org.testcontainers:mariadb")
                runtimeOnly(project(":komapper-dialect-mariadb-jdbc"))
            }
        }

        register("mysql", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project())
                runtimeOnly("org.testcontainers:mysql")
                runtimeOnly(project(":komapper-dialect-mysql-jdbc"))
            }
        }

        register("mysql5", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project())
                runtimeOnly("org.testcontainers:mysql")
                implementation(project(":komapper-dialect-mysql-jdbc"))
            }
        }

        register("oracle", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project())
                runtimeOnly("org.testcontainers:oracle-xe")
                runtimeOnly(project(":komapper-dialect-oracle-jdbc"))
            }
        }

        register("postgresql", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation.add(project())
                runtimeOnly("org.testcontainers:postgresql")
                implementation(project(":komapper-dialect-postgresql-jdbc"))
            }
        }

        register("sqlserver", JvmTestSuite::class) {
            setup(name)
            dependencies {
                implementation(project())
                runtimeOnly("org.testcontainers:mssqlserver")
                runtimeOnly(project(":komapper-dialect-sqlserver-jdbc"))
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
