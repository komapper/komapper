dependencies {
    api(project(":komapper-dialect-mariadb"))
    api(project(":komapper-jdbc"))
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.8")
}
