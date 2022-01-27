dependencies {
    val r2dbcVersion: String by project
    api(project(":komapper-dialect-mariadb"))
    api(project(":komapper-r2dbc"))
    implementation(platform("io.r2dbc:r2dbc-bom:$r2dbcVersion"))
    implementation("org.mariadb:r2dbc-mariadb")
}
