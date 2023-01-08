dependencies {
    api(project(":komapper-dialect-mariadb"))
    api(project(":komapper-r2dbc"))
    implementation("org.mariadb:r2dbc-mariadb:1.1.2")
}
