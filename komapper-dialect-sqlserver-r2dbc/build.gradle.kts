dependencies {
    api(project(":komapper-dialect-sqlserver"))
    api(project(":komapper-r2dbc"))
    implementation("io.r2dbc:r2dbc-mssql:1.0.0.RELEASE")
}
