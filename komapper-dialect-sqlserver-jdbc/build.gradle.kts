dependencies {
    api(project(":komapper-dialect-sqlserver"))
    api(project(":komapper-jdbc"))
    implementation("com.microsoft.sqlserver:mssql-jdbc:9.4.1.jre16")
}
