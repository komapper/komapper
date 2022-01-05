plugins {
    application
    idea
    id("com.google.devtools.ksp")
}

dependencies {
    ksp(project(":komapper-processor"))
    implementation(project(":komapper-starter-jdbc"))
    runtimeOnly(project(":komapper-dialect-h2-jdbc"))
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
