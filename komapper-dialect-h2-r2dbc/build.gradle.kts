dependencies {
    val r2dbcBomVersion: String by project
    api(project(":komapper-dialect-h2"))
    api(project(":komapper-r2dbc"))
    implementation(platform("io.r2dbc:r2dbc-bom:$r2dbcBomVersion"))
    implementation("io.r2dbc:r2dbc-h2")
}
