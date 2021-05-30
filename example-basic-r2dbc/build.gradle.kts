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
    implementation(project(":komapper-tx-r2dbc"))
    ksp(project(":komapper-processor"))
    runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
}

application {
    mainClass.set("example.ApplicationKt")
}
