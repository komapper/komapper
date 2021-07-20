plugins {
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
    ksp(project(":komapper-processor"))
    api(project(":komapper-core"))
    runtimeOnly(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly("ch.qos.logback:logback-classic:1.2.4")
    api(platform("org.testcontainers:testcontainers-bom:1.16.0"))
    runtimeOnly("org.testcontainers:mariadb")
    runtimeOnly("org.testcontainers:mysql")
    runtimeOnly("org.testcontainers:postgresql")
    runtimeOnly("org.testcontainers:r2dbc")
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
