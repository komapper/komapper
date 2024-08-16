plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    val springVersion: String by project
    implementation("org.springframework:spring-jdbc:$springVersion")
    implementation(project(":komapper-jdbc"))
    implementation(project(":komapper-spring"))
    testImplementation(project(":komapper-annotation"))
    testImplementation(project(":komapper-slf4j"))
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.7")
    testImplementation(project(":komapper-dialect-h2-jdbc"))
    kspTest(project(":komapper-processor"))
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
