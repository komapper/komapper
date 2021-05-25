dependencies {
    api(project(":komapper-dialect-h2"))
    api(project(":komapper-r2dbc"))
    runtimeOnly("io.r2dbc:r2dbc-spi:0.8.4.RELEASE")
}
