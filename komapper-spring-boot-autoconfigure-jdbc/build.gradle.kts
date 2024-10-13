dependencies {
    api(libs.spring.jdbc)
    api(libs.spring.boot.autoconfigure)
    api(project(":komapper-jdbc"))
    api(project(":komapper-spring-jdbc"))
    testImplementation(project(":komapper-slf4j"))
    testImplementation(project(":komapper-dialect-h2-jdbc"))
    testImplementation(libs.logback.classic)
    testImplementation(libs.hikariCP)
}
