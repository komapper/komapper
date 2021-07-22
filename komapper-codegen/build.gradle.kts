dependencies {
    api(project(":komapper-jdbc"))
    runtimeOnly(project(":komapper-dialect-h2-jdbc"))
    runtimeOnly(project(":komapper-dialect-mariadb-jdbc"))
    runtimeOnly(project(":komapper-dialect-mysql-jdbc"))
    runtimeOnly(project(":komapper-dialect-postgresql-jdbc"))
}
