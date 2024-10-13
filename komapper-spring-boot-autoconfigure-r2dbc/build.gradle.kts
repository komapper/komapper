dependencies {
    api(libs.spring.r2dbc)
    api(libs.spring.boot.autoconfigure)
    api(project(":komapper-r2dbc"))
    api(project(":komapper-spring-r2dbc"))
    testImplementation(project(":komapper-slf4j"))
    testImplementation(project(":komapper-dialect-h2-r2dbc"))
    testImplementation(libs.r2dbc.pool)
    testImplementation(libs.logback.classic)
}
