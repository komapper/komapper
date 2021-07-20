dependencies {
    api(project(":komapper-dialect-mariadb"))
    api(project(":komapper-r2dbc"))
    runtimeOnly("org.mariadb:r2dbc-mariadb:1.0.2")
}
