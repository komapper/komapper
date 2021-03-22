plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(project(":komapper-core"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.4.30-1.0.0-alpha05")
    testImplementation("com.google.devtools.ksp:symbol-processing:1.4.30-1.0.0-alpha05")
    testImplementation("com.squareup:kotlinpoet:1.7.2")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.3.6")
    testImplementation("com.google.truth:truth:1.1.2")
    testImplementation("junit:junit:4.13.2")
}
