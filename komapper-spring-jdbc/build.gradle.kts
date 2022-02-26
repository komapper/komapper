dependencies {
    val springVersion: String by project
    implementation("org.springframework:spring-jdbc:$springVersion")
    implementation(project(":komapper-jdbc"))
}
