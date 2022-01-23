dependencies {
    api(project(":komapper-dialect-oracle"))
    api(project(":komapper-jdbc"))
    implementation("com.oracle.database.jdbc:ojdbc8-production:18.15.0.0")
}
