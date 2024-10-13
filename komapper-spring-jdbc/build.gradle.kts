plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.spring.jdbc)
    implementation(project(":komapper-jdbc"))
    implementation(project(":komapper-spring"))
    testImplementation(project(":komapper-annotation"))
    testImplementation(project(":komapper-slf4j"))
    testRuntimeOnly(libs.logback.classic)
    testImplementation(project(":komapper-dialect-h2-jdbc"))
    kspTest(project(":komapper-processor"))
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
