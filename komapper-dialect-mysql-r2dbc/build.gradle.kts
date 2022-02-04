dependencies {
    val r2dbcVersion: String by project
    api(project(":komapper-dialect-mysql"))
    api(project(":komapper-r2dbc"))
    implementation(platform("io.r2dbc:r2dbc-bom:$r2dbcVersion"))
    implementation("dev.miku:r2dbc-mysql:0.8.2.RELEASE")
}
