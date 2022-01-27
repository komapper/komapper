dependencies {
    val r2dbcVersion: String by project
    val springVersion: String by project
    val springBootVersion: String by project
    implementation("org.springframework:spring-r2dbc:$springVersion")
    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation(project(":komapper-r2dbc"))
    testImplementation(platform("io.r2dbc:r2dbc-bom:$r2dbcVersion"))
    testImplementation(project(":komapper-slf4j"))
    testImplementation(project(":komapper-dialect-h2-r2dbc"))
    testImplementation("io.r2dbc:r2dbc-h2")
    testImplementation("io.r2dbc:r2dbc-pool")
    testImplementation("ch.qos.logback:logback-classic:1.2.10")
}
