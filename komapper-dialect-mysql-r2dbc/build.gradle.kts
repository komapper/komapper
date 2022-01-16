dependencies {
    api(project(":komapper-dialect-mysql"))
    api(project(":komapper-r2dbc"))
    implementation(platform("io.r2dbc:r2dbc-bom:Arabba-SR12"))
    implementation("dev.miku:r2dbc-mysql")
}
