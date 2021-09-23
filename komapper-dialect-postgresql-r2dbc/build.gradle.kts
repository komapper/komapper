dependencies {
    api(project(":komapper-dialect-postgresql"))
    api(project(":komapper-r2dbc"))
    implementation("io.r2dbc:r2dbc-postgresql:0.8.9.RELEASE")
}
