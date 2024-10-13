plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.spring.r2dbc)
    implementation(project(":komapper-r2dbc"))
    implementation(project(":komapper-spring"))
    testImplementation(project(":komapper-annotation"))
    testImplementation(project(":komapper-slf4j"))
    testRuntimeOnly(libs.logback.classic)
    testImplementation(project(":komapper-dialect-h2-r2dbc"))
    kspTest(project(":komapper-processor"))
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
