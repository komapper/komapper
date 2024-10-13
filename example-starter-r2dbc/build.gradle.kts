plugins {
    application
    alias(libs.plugins.ksp)
}

dependencies {
    val kotlinxDatetime: String by project
    ksp(project(":komapper-processor"))
    implementation(project(":komapper-starter-r2dbc"))
    implementation(libs.kotlinx.datetime)
    runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
}

application {
    mainClass.set("example.starter.r2dbc.ApplicationKt")
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
