dependencies {
    val kotlinxDatetime: String by project
    implementation(project(":komapper-r2dbc"))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetime")
}
