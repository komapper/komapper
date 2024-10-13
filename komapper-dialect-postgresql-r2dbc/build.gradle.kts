dependencies {
    api(project(":komapper-dialect-postgresql"))
    api(project(":komapper-r2dbc"))
    api(libs.r2dbc.postgresql)
    api(libs.jts.core)
}
