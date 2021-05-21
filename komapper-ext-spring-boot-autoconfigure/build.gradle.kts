dependencies {
    val springVersion: String by project
    val springBootVersion: String by project
    implementation("org.springframework:spring-jdbc:$springVersion")
    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation(project(":komapper-core"))
    testImplementation(project(":komapper-ext-slf4j"))
    testImplementation(project(":komapper-jdbc-h2"))
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("com.zaxxer:HikariCP:4.0.3")
}
