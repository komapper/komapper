plugins {
    idea
    id("org.springframework.boot")
    id("com.google.devtools.ksp")
    kotlin("plugin.spring")
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    ksp(project(":komapper-processor"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(project(":komapper-spring-boot-starter-r2dbc"))
    implementation(project(":komapper-sqlcommenter"))
    runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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

springBoot {
    mainClass.set("example.ApplicationKt")
}
