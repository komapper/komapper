dependencies {
    implementation(project(":integration-test-core"))
    implementation(project(":komapper-tx-r2dbc"))
    runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
    runtimeOnly(project(":komapper-dialect-mariadb-r2dbc"))
    runtimeOnly(project(":komapper-dialect-mysql-r2dbc"))
    runtimeOnly(project(":komapper-dialect-postgresql-r2dbc"))
    implementation("org.testcontainers:mariadb")
    implementation("org.testcontainers:mysql")
    implementation("org.testcontainers:postgresql")
    implementation("org.testcontainers:r2dbc")
    runtimeOnly("mysql:mysql-connector-java:8.0.26")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:2.7.3")
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
