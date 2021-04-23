plugins {
    application
    idea
    id("com.google.devtools.ksp") version "1.4.32-1.0.0-alpha08"
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
    implementation(project(":komapper-annotation"))
    implementation(project(":komapper-jdbc-h2"))
    implementation(project(":komapper-transaction"))
    ksp(project(":komapper-processor"))
}

application {
    mainClass.set("example.ExampleKt")
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
