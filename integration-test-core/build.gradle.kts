plugins {
    idea
    id("com.google.devtools.ksp")
}

dependencies {
    compileOnly(project(":komapper-annotation"))
    ksp(project(":komapper-processor"))
    api(project(":komapper-core"))
    runtimeOnly(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly("ch.qos.logback:logback-classic:1.2.10")
}

idea {
    module {
        sourceDirs = sourceDirs + file("build/generated/ksp/main/kotlin")
        testSourceDirs = testSourceDirs + file("build/generated/ksp/test/kotlin")
        generatedSourceDirs = generatedSourceDirs + file("build/generated/ksp/main/kotlin") + file("build/generated/ksp/test/kotlin")
    }
}

ksp {
    arg("komapper.namingStrategy", "lower_snake_case")
}
