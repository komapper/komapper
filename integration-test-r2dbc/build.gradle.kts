dependencies {
    implementation(project(":integration-test-core"))
    implementation(project(":komapper-tx-r2dbc"))
    runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
    runtimeOnly(project(":komapper-dialect-mysql-r2dbc"))
    runtimeOnly(project(":komapper-dialect-postgresql-r2dbc"))
}

tasks {
    test {
        val driver: Any by project
        val databaseKey = "$driver.database"
        val userKey = "$driver.user"
        val passwordKey = "$driver.password"
        val database = project.findProperty(databaseKey) ?: ""
        val user = project.findProperty(userKey) ?: ""
        val password = project.findProperty(passwordKey) ?: ""
        systemProperty("driver", driver)
        systemProperty("database", database)
        systemProperty("user", user)
        systemProperty("password", password)
    }
}
