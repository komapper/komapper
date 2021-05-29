dependencies {
    val kotlinCoroutinesVersion: String by project
    api(project(":komapper-r2dbc"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCoroutinesVersion")
}
