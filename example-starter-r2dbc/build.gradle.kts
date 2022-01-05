plugins {
    idea
    application
    id("com.google.devtools.ksp")
}

dependencies {
    ksp(project(":komapper-processor"))
    implementation(project(":komapper-starter-r2dbc"))
    runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
}

application {
    mainClass.set("example.ApplicationKt")
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
