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
    test {
        val driver: Any by project
        val urlKey = "$driver.url"
        val url = project.findProperty(urlKey) ?: ""
        systemProperty("driver", driver)
        systemProperty("url", url)
    }
}
