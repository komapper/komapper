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
    val kotlinVersion: String by project
    compileOnly(project(":komapper-annotation"))
    ksp(project(":komapper-processor"))
    implementation(project(":komapper-tx-r2dbc"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinVersion")
    runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
    runtimeOnly("io.r2dbc:r2dbc-h2:0.8.4.RELEASE")
}

application {
    mainClass.set("example.ApplicationKt")
}
