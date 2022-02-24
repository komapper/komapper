dependencies {
    api(project(":komapper-dialect-oracle"))
    api(project(":komapper-jdbc"))
    implementation("com.oracle.database.jdbc:ojdbc8-production:21.5.0.0")
}
