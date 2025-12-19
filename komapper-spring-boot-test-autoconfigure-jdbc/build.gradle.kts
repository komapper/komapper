dependencies {
    api(libs.spring.test)
    api(libs.spring.tx)
    api(libs.spring.boot.autoconfigure)
    api(libs.spring.boot.test.autoconfigure)
    api(libs.spring.boot.cache.test)
    implementation(libs.spring.boot.starter.test)

    testImplementation(project(":komapper-spring-boot-starter-jdbc"))
    testImplementation(project(":komapper-slf4j"))
    testImplementation(project(":komapper-dialect-h2-jdbc"))
}
