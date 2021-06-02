dependencies {
    api(project(":komapper-dialect-mysql"))
    api(project(":komapper-r2dbc"))
    runtimeOnly("dev.miku:r2dbc-mysql:0.8.2.RELEASE")
}
