dependencies {
    api(project(":komapper-dialect-mysql"))
    api(project(":komapper-jdbc"))
    implementation("mysql:mysql-connector-java:8.0.29")
}
