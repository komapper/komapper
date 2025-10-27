dependencies {
    implementation(project(":komapper-core"))
    implementation(project(":komapper-r2dbc"))
    implementation(project(":komapper-template"))
    implementation(project(":komapper-exposed"))
    implementation(libs.kotlin.reflect)
    implementation(libs.exposed.core)
    implementation(libs.exposed.r2dbc)

    testImplementation(libs.exposed.dao)
    testRuntimeOnly(libs.r2dbc.h2)
}
