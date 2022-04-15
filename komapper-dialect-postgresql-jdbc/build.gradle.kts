dependencies {
    api(project(":komapper-dialect-postgresql"))
    api(project(":komapper-jdbc"))
    implementation("org.postgresql:postgresql:42.3.4")
}
