dependencies {
    api(libs.spring.boot.autoconfigure)
    api(libs.spring.boot.jdbc)
    api(project(":komapper-jdbc"))
    api(project(":komapper-spring-jdbc"))
    testImplementation(project(":komapper-slf4j"))
    testImplementation(project(":komapper-dialect-h2-jdbc"))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.logback.classic)
    testImplementation(libs.hikariCP)
}
