dependencies {
    val springVersion: String by project
    val springBootVersion: String by project
    api("org.springframework:spring-jdbc:$springVersion")
    api("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    api(project(":komapper-jdbc"))
    api(project(":komapper-spring-jdbc"))
    testImplementation(project(":komapper-slf4j"))
    testImplementation(project(":komapper-dialect-h2-jdbc"))
    testImplementation("ch.qos.logback:logback-classic:1.5.0")
    testImplementation("com.zaxxer:HikariCP:4.0.3")
}
