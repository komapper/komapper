dependencies {
    val kotlinxDatetime: String by project
    implementation(project(":komapper-jdbc"))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetime")
}
