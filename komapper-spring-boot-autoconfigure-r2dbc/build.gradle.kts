dependencies {
    val springVersion: String by project
    val springBootVersion: String by project
    api("org.springframework:spring-r2dbc:$springVersion")
    api("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    api(project(":komapper-r2dbc"))
    api(project(":komapper-spring-r2dbc"))
    testImplementation(project(":komapper-slf4j"))
    testImplementation(project(":komapper-dialect-h2-r2dbc"))
    testImplementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    testImplementation("ch.qos.logback:logback-classic:1.4.5")
}
