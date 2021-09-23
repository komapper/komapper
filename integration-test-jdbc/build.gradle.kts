dependencies {
    implementation(project(":integration-test-core"))
    implementation(project(":komapper-tx-jdbc"))
    implementation("org.postgresql:postgresql:42.2.24")
    runtimeOnly(project(":komapper-dialect-h2-jdbc"))
    runtimeOnly(project(":komapper-dialect-mariadb-jdbc"))
    runtimeOnly(project(":komapper-dialect-mysql-jdbc"))
    runtimeOnly(project(":komapper-dialect-postgresql-jdbc"))
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.16.0"))
    testRuntimeOnly("org.testcontainers:mariadb")
    testRuntimeOnly("org.testcontainers:mysql")
    testRuntimeOnly("org.testcontainers:postgresql")
    testRuntimeOnly("org.testcontainers:r2dbc")
}

tasks {
    fun Test.prepareProperties(driver: String) {
        val urlKey = "$driver.url"
        val url = project.property(urlKey) ?: throw GradleException("The $urlKey property is not found.")
        this.systemProperty("driver", driver)
        this.systemProperty("url", url)
    }

    test {
        val driver: Any by project
        prepareProperties(driver.toString())
    }

    val h2 by registering(Test::class) {
        prepareProperties("h2")
    }

    val mariadb by registering(Test::class) {
        prepareProperties("mariadb")
    }

    val mysql by registering(Test::class) {
        prepareProperties("mysql")
    }

    val postgresql by registering(Test::class) {
        prepareProperties("postgresql")
    }

    register("testAll") {
        dependsOn(h2, mariadb, mysql, postgresql)
    }
}
