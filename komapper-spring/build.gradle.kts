dependencies {
    val springVersion: String by project
    implementation("org.springframework:spring-tx:$springVersion")
    implementation(project(":komapper-tx-core"))
}
