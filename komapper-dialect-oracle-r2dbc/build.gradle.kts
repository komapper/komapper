dependencies {
    api(project(":komapper-dialect-oracle"))
    api(project(":komapper-r2dbc"))
    implementation(platform("io.r2dbc:r2dbc-bom:Arabba-SR12"))
    implementation("com.oracle.database.r2dbc:oracle-r2dbc")
}
