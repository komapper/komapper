dependencies {
    api(project(":komapper-jdbc"))
    runtimeOnly(project(":komapper-jdbc-dialect-h2"))
    runtimeOnly(project(":komapper-jdbc-dialect-mysql"))
    runtimeOnly(project(":komapper-jdbc-dialect-postgresql"))
}
