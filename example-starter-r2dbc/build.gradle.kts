plugins {
    idea
    application
    id("com.google.devtools.ksp")
}

dependencies {
    val kotlinxDatetime: String by project
    ksp(project(":komapper-processor"))
    implementation(project(":komapper-starter-r2dbc"))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetime")
    runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
}

application {
    mainClass.set("example.starter.r2dbc.ApplicationKt")
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
