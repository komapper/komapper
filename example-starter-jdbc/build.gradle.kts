plugins {
    application
    id("com.google.devtools.ksp")
}

dependencies {
    val kotlinxDatetime: String by project
    ksp(project(":komapper-processor"))
    implementation(project(":komapper-starter-jdbc"))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetime")
    runtimeOnly(project(":komapper-dialect-h2-jdbc"))
}

application {
    mainClass.set("example.starter.jdbc.ApplicationKt")
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
