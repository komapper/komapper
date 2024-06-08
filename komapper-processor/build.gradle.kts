import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    val kotlinVersion: String by project
    val kspVersion: String by project
    val kspFullVersion = "$kotlinVersion-$kspVersion"
    implementation(project(":komapper-core"))
    implementation(project(":komapper-annotation"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspFullVersion")
    testImplementation(project(":komapper-annotation"))
    testImplementation("com.google.devtools.ksp:symbol-processing:$kspFullVersion")
    testImplementation("dev.zacsweers.kctfork:ksp:0.5.0")
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
}
