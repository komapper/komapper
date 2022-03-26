plugins {
    application
    idea
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

idea {
    module {
        sourceDirs = sourceDirs + file("build/generated/ksp/main/kotlin")
        testSourceDirs = testSourceDirs + file("build/generated/ksp/test/kotlin")
        generatedSourceDirs = generatedSourceDirs + file("build/generated/ksp/main/kotlin") + file("build/generated/ksp/test/kotlin")
    }
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
