dependencies {
    api(project(":komapper-dialect-mysql"))
    api(project(":komapper-r2dbc"))
    implementation(platform("io.r2dbc:r2dbc-spi:0.8.2.RELEASE"))
    implementation("dev.miku:r2dbc-mysql:0.8.2.RELEASE")
}
