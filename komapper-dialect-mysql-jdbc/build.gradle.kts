dependencies {
    api(project(":komapper-dialect-mysql"))
    api(project(":komapper-jdbc"))
    runtimeOnly("mysql:mysql-connector-java:8.0.25")
}
