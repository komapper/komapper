plugins {
    application
    idea
    id("com.google.devtools.ksp")
}

sourceSets {
    main {
        java {
            srcDir("build/generated/ksp/main/kotlin")
        }
    }
}

idea.module {
    generatedSourceDirs.add(file("build/generated/ksp/main/kotlin"))
}

dependencies {
    compileOnly(project(":komapper-annotation"))
    implementation(project(":komapper-jdbc-tx"))
    runtimeOnly(project(":komapper-jdbc-dialect-h2"))
    ksp(project(":komapper-processor"))
}

application {
    mainClass.set("example.ApplicationKt")
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
