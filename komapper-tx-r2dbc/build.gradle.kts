plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    api(project(":komapper-r2dbc"))
    testImplementation(project(":komapper-annotation"))
    testImplementation(project(":komapper-slf4j"))
    testRuntimeOnly(libs.logback.classic)
    testImplementation(project(":komapper-dialect-h2-r2dbc"))
    kspTest(project(":komapper-processor"))
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
