plugins {
    application
    alias(libs.plugins.ksp)
}

dependencies {
    ksp(project(":komapper-processor"))
    implementation(project(":komapper-starter-jdbc"))
    implementation(libs.kotlinx.datetime)
    runtimeOnly(project(":komapper-dialect-h2-jdbc"))
}

application {
    mainClass.set("example.starter.jdbc.ApplicationKt")
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
