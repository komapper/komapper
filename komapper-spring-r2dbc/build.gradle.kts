dependencies {
    val springVersion: String by project
    implementation("org.springframework:spring-r2dbc:$springVersion")
    implementation(project(":komapper-r2dbc"))
}
