dependencies {
    val r2dbcBomVersion: String by project
    api(project(":komapper-dialect-oracle"))
    api(project(":komapper-r2dbc"))
    implementation(platform("io.r2dbc:r2dbc-bom:$r2dbcBomVersion"))
    implementation("com.oracle.database.r2dbc:oracle-r2dbc")
}
