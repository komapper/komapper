plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":komapper-core"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.5.0-1.0.0-alpha09")
    testImplementation(project(":komapper-annotation"))
    testImplementation("com.google.devtools.ksp:symbol-processing:1.5.0-1.0.0-alpha09")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.0")
    testImplementation("com.google.truth:truth:1.1.2")
}
