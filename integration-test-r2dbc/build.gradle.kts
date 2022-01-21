val driver: Any by project

dependencies {
    implementation(project(":integration-test-core"))
    implementation(project(":komapper-tx-r2dbc"))
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.16.3"))
    testImplementation("org.testcontainers:r2dbc")
    testImplementation("org.testcontainers:mariadb")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:postgresql")
    when (driver) {
        "h2" -> {
            runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
        }
        "mariadb" -> {
            runtimeOnly(project(":komapper-dialect-mariadb-r2dbc"))
            testRuntimeOnly("org.mariadb.jdbc:mariadb-java-client:2.7.5")
        }
        "mysql" -> {
            runtimeOnly(project(":komapper-dialect-mysql-r2dbc"))
            testRuntimeOnly("mysql:mysql-connector-java:8.0.28")
        }
        "postgresql" -> {
            runtimeOnly(project(":komapper-dialect-postgresql-r2dbc"))
        }
        "sqlserver" -> {
            runtimeOnly(project(":komapper-dialect-sqlserver-r2dbc"))
        }
        else -> throw IllegalArgumentException("Unknown driver: $driver")
    }
}

tasks {
    test {
        val urlKey = "$driver.url"
        val url = project.property(urlKey) ?: throw GradleException("The $urlKey property is not found.")
        this.systemProperty("driver", driver)
        this.systemProperty("url", url)
    }
}
