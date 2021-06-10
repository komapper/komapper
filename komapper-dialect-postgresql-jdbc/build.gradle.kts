dependencies {
    api(project(":komapper-dialect-postgresql"))
    api(project(":komapper-jdbc"))
    runtimeOnly("org.postgresql:postgresql:42.2.21")
}
