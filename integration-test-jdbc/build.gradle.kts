dependencies {
    implementation(project(":integration-test-core"))
    implementation(project(":komapper-tx-jdbc"))
    implementation("org.postgresql:postgresql:42.2.22")
    implementation(project(":komapper-dialect-h2-jdbc"))
    runtimeOnly(project(":komapper-dialect-mysql-jdbc"))
    runtimeOnly(project(":komapper-dialect-postgresql-jdbc"))
}

tasks {
    test {
        val driver: Any by project
        val urlKey = "$driver.url"
        val userKey = "$driver.user"
        val passwordKey = "$driver.password"
        val url = project.property(urlKey) ?: throw GradleException("The $urlKey property is not found.")
        val user = project.property(userKey) ?: throw GradleException("The $userKey property is not found.")
        val password = project.property(passwordKey) ?: throw GradleException("The $passwordKey property is not found.")
        systemProperty("driver", driver)
        systemProperty("url", url)
        systemProperty("user", user)
        systemProperty("password", password)
    }
}
