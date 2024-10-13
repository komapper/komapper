plugins {
    application
    alias(libs.plugins.ksp)
}

dependencies {
    compileOnly(project(":komapper-annotation"))
    implementation(project(":komapper-tx-jdbc"))
    ksp(project(":komapper-processor"))
    runtimeOnly(project(":komapper-dialect-h2-jdbc"))
}

application {
    mainClass.set("example.basic.jdbc.ApplicationKt")
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
