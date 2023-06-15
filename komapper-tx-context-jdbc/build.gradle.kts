plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    api(project(":komapper-jdbc"))
    api(project(":komapper-tx-jdbc"))
    testImplementation(project(":komapper-annotation"))
    testImplementation(project(":komapper-slf4j"))
    testRuntimeOnly("ch.qos.logback:logback-classic:1.4.8")
    testImplementation(project(":komapper-dialect-h2-jdbc"))
    kspTest(project(":komapper-processor"))
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
        }
    }
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
