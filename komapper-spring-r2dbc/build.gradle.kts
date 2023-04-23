plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    val kotlinCoroutinesVersion: String by project
    val springVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCoroutinesVersion")
    implementation("org.springframework:spring-r2dbc:$springVersion")
    implementation(project(":komapper-r2dbc"))
    implementation(project(":komapper-spring"))
    testImplementation(project(":komapper-annotation"))
    testImplementation(project(":komapper-slf4j"))
    testRuntimeOnly("ch.qos.logback:logback-classic:1.4.7")
    testImplementation(project(":komapper-dialect-h2-r2dbc"))
    kspTest(project(":komapper-processor"))
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
