dependencies {
    val r2dbcBomVersion: String by project
    api(project(":komapper-dialect-mariadb"))
    api(project(":komapper-r2dbc"))
    implementation(platform("io.r2dbc:r2dbc-bom:$r2dbcBomVersion"))
    implementation("org.mariadb:r2dbc-mariadb")
}
