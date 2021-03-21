plugins {
    application
    idea
    id("com.google.devtools.ksp") version "1.4.30-1.0.0-alpha05"
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
    implementation(project(":komapper-jdbc-h2"))
    ksp(project(":komapper-processor"))
}

application {
    mainClass.set("example.ExampleKt")
}
