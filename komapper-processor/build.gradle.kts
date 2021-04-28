plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":komapper-core"))
    implementation(libs.symbol.processing.api)
    testImplementation(project(":komapper-annotation"))
    testImplementation(libs.symbol.processing.impl)
    testImplementation(libs.kotlin.compile.testing.ksp)
    testImplementation(libs.truth)
}
