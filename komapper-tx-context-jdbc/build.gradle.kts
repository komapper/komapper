plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    api(project(":komapper-jdbc"))
    api(project(":komapper-tx-jdbc"))
    testImplementation(project(":komapper-annotation"))
    testImplementation(project(":komapper-slf4j"))
    testRuntimeOnly(libs.logback.classic)
    testImplementation(project(":komapper-dialect-h2-jdbc"))
    kspTest(project(":komapper-processor"))
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
