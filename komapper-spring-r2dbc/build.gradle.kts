dependencies {
    val kotlinCoroutinesVersion: String by project
    val springVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCoroutinesVersion")
    implementation("org.springframework:spring-r2dbc:$springVersion")
    implementation(project(":komapper-r2dbc"))
}
