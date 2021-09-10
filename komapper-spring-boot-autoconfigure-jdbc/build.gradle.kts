dependencies {
    val springVersion: String by project
    val springBootVersion: String by project
    implementation("org.springframework:spring-jdbc:$springVersion")
    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation(project(":komapper-jdbc"))
    testImplementation(project(":komapper-slf4j"))
    testImplementation(project(":komapper-dialect-h2-jdbc"))
    testImplementation("ch.qos.logback:logback-classic:1.2.6")
    testImplementation("com.zaxxer:HikariCP:4.0.3")
}
