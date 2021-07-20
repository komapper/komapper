dependencies {
    api(project(":komapper-dialect-mariadb"))
    api(project(":komapper-jdbc"))
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:2.7.3")
}
