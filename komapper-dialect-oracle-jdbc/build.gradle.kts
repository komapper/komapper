dependencies {
    api(project(":komapper-dialect-oracle"))
    api(project(":komapper-jdbc"))
    implementation("com.oracle.database.jdbc:ojdbc11:21.6.0.0.1")
}
