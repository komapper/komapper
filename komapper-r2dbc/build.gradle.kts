import org.gradle.model.internal.core.ModelNodes.withType

dependencies {
    api(project(":komapper-core"))
    api("io.r2dbc:r2dbc-spi:0.8.5.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.5.0")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}
