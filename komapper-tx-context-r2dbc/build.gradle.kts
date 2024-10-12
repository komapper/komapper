plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    api(project(":komapper-r2dbc"))
    api(project(":komapper-tx-r2dbc"))
    testImplementation(project(":komapper-annotation"))
    testImplementation(project(":komapper-slf4j"))
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.10")
    testImplementation(project(":komapper-dialect-h2-r2dbc"))
    kspTest(project(":komapper-processor"))
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-receivers")
        }
    }
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
