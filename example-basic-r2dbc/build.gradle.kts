plugins {
    application
    id("com.google.devtools.ksp")
}

dependencies {
    compileOnly(project(":komapper-annotation"))
    implementation(project(":komapper-tx-r2dbc"))
    ksp(project(":komapper-processor"))
    runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
}

application {
    mainClass.set("example.basic.r2dbc.ApplicationKt")
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
