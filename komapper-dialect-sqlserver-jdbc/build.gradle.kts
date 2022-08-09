dependencies {
    api(project(":komapper-dialect-sqlserver"))
    api(project(":komapper-jdbc"))
    implementation("com.microsoft.sqlserver:mssql-jdbc:11.2.0.jre11")
}
