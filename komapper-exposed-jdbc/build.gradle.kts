dependencies {
    implementation(project(":komapper-core"))
    implementation(project(":komapper-template"))
    implementation(project(":komapper-exposed"))
    implementation(libs.kotlin.reflect)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)

    testImplementation(libs.exposed.dao)
    testRuntimeOnly(libs.jdbc.h2)
}
