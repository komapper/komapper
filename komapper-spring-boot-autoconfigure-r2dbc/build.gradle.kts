dependencies {
    val r2dbcBomVersion: String by project
    val springVersion: String by project
    val springBootVersion: String by project
    api("org.springframework:spring-r2dbc:$springVersion")
    api("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    api(project(":komapper-r2dbc"))
    api(project(":komapper-spring-r2dbc"))
    testImplementation(platform("io.r2dbc:r2dbc-bom:$r2dbcBomVersion"))
    testImplementation(project(":komapper-slf4j"))
    testImplementation(project(":komapper-dialect-h2-r2dbc"))
    testImplementation("io.r2dbc:r2dbc-h2")
    testImplementation("io.r2dbc:r2dbc-pool")
    testImplementation("ch.qos.logback:logback-classic:1.4.5")
}
