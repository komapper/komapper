dependencies {
    val kspVersion: String by project
    implementation(project(":komapper-core"))
    implementation(project(":komapper-annotation"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    testImplementation(project(":komapper-annotation"))
    testImplementation("com.google.devtools.ksp:symbol-processing:$kspVersion")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.2")
}
