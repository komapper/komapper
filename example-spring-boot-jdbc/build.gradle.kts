plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.plugin.spring)
}

dependencies {
    ksp(project(":komapper-processor"))
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.jackson.module.kotlin)
    implementation(project(":komapper-spring-boot-starter-jdbc"))
    runtimeOnly(project(":komapper-dialect-h2-jdbc"))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.http.client)
    testImplementation(libs.spring.boot.restclient)
    testImplementation(libs.spring.boot.resttestclient)
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}

springBoot {
    mainClass.set("example.spring.boot.jdbc.JdbcSpringApplicationKt")
}
