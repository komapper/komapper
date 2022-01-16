dependencies {
    val kotlinCoroutinesVersion: String by project
    api(platform("io.r2dbc:r2dbc-bom:Arabba-SR12"))
    api(project(":komapper-core"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinCoroutinesVersion")
    api("io.r2dbc:r2dbc-spi")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}
