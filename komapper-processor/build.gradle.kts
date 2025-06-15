import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    implementation(project(":komapper-core"))
    implementation(project(":komapper-annotation"))
    implementation(libs.ksp.symbol.processing.api)
    testImplementation(project(":komapper-annotation"))
    testImplementation(libs.ksp.symbol.processing)
    testImplementation(libs.ksp.test)
}

tasks {
    withType<KotlinCompile>().configureEach {
        compilerOptions {
            if (name == "compileTestKotlin") {
                optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
            }
            optIn.add("org.komapper.annotation.KomapperExperimentalAssociation")
        }
    }

    test {
        systemProperty("junit.jupiter.execution.parallel.enabled", "true")
        systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    }
}
