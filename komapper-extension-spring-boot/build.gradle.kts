dependencies {
    implementation("org.springframework:spring-jdbc:5.3.6")
    implementation("org.springframework.boot:spring-boot-autoconfigure:2.4.5")
    implementation(project(":komapper-core"))
    implementation(project(":komapper-logging-slf4j"))
    testImplementation(project(":komapper-jdbc-h2"))
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("com.zaxxer:HikariCP:4.0.3")
}
