plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.plugin.spring)
}

dependencies {
    ksp(project(":komapper-processor"))
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.jackson.module.kotlin)
    implementation(project(":komapper-spring-boot-starter-r2dbc"))
    runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
    testImplementation(libs.spring.boot.starter.test)
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}

springBoot {
    mainClass.set("example.spring.boot.r2dbc.R2dbcSpringApplicationKt")
}
