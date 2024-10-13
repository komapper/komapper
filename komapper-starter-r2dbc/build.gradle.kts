dependencies {
    api(project(":komapper-annotation"))
    api(project(":komapper-r2dbc"))
    api(project(":komapper-tx-r2dbc"))
    api(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-datetime-r2dbc"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly(libs.logback.classic)
}
