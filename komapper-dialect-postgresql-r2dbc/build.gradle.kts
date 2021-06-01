dependencies {
    api(project(":komapper-dialect-postgresql"))
    api(project(":komapper-r2dbc"))
    runtimeOnly("org.postgresql:r2dbc-postgresql:0.9.0.M1")
}
