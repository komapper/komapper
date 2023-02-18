dependencies {
    val kotlinVersion: String by project
    val kspVersion: String by project
    val kspFullVersion = "$kotlinVersion-$kspVersion"
    implementation(project(":komapper-core"))
    implementation(project(":komapper-annotation"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspFullVersion")
    testImplementation(project(":komapper-annotation"))
    testImplementation("com.google.devtools.ksp:symbol-processing:$kspFullVersion")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.9")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs += listOf(
            "-opt-in=org.komapper.annotation.KomapperExperimentalAssociation",
        )
    }
}
