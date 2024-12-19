dependencies {
    api(libs.spring.test)
    api(libs.spring.boot.autoconfigure)
    api(libs.spring.boot.test.autoconfigure)
    implementation(libs.spring.boot.starter.test)

    testImplementation(project(":komapper-spring-boot-starter-r2dbc"))
    testImplementation(project(":komapper-slf4j"))
    testImplementation(project(":komapper-dialect-h2-r2dbc"))
}
