dependencies {
    val r2dbcVersion: String by project
    api(project(":komapper-r2dbc"))
    testImplementation(platform("io.r2dbc:r2dbc-bom:$r2dbcVersion"))
    testRuntimeOnly("io.r2dbc:r2dbc-h2")
}
