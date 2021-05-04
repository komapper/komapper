plugins {
    application
    idea
    id("com.google.devtools.ksp") version "1.5.0-1.0.0-alpha09"
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
    implementation(project(":komapper-core"))
    implementation(project(":komapper-transaction"))
    runtimeOnly(project(":komapper-jdbc-h2"))
    ksp(project(":komapper-processor"))
}

application {
    mainClass.set("example.ApplicationKt")
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
