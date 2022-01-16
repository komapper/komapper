dependencies {
    api(project(":komapper-r2dbc"))
    testImplementation(platform("io.r2dbc:r2dbc-bom:Arabba-SR12"))
    testRuntimeOnly("io.r2dbc:r2dbc-h2")
}
