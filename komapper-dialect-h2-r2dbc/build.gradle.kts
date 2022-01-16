dependencies {
    api(project(":komapper-dialect-h2"))
    api(project(":komapper-r2dbc"))
    implementation(platform("io.r2dbc:r2dbc-bom:Arabba-SR12"))
    implementation("io.r2dbc:r2dbc-h2")
}
