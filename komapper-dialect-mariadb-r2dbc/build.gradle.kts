dependencies {
    api(project(":komapper-dialect-mariadb"))
    api(project(":komapper-r2dbc"))
    implementation(platform("io.r2dbc:r2dbc-bom:Arabba-SR12"))
    implementation("org.mariadb:r2dbc-mariadb")
}
