val driver: Any by project

dependencies {
    implementation(project(":integration-test-core"))
    implementation(project(":komapper-tx-jdbc"))
    implementation("org.postgresql:postgresql:42.3.1")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.16.2"))
    testRuntimeOnly("org.testcontainers:mariadb")
    testRuntimeOnly("org.testcontainers:mysql")
    testRuntimeOnly("org.testcontainers:postgresql")
    when (driver) {
        "h2" -> {
            runtimeOnly(project(":komapper-dialect-h2-jdbc"))
        }
        "mariadb" -> {
            runtimeOnly(project(":komapper-dialect-mariadb-jdbc"))
        }
        "mysql" -> {
            runtimeOnly(project(":komapper-dialect-mysql-jdbc"))
        }
        "postgresql" -> {
            runtimeOnly(project(":komapper-dialect-postgresql-jdbc"))
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
