plugins {
    idea
    kotlin("plugin.allopen")
    kotlin("kapt")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("com.google.devtools.ksp")
    id("io.micronaut.application")
}

dependencies {
    kapt("io.micronaut:micronaut-http-validation")
    ksp(project(":komapper-processor"))
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut.data:micronaut-data-tx")
    implementation("io.micronaut.flyway:micronaut-flyway")
    implementation(project(":komapper-micronaut-starter-jdbc"))
    implementation(project(":komapper-dialect-h2-jdbc"))
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    runtimeOnly("org.flywaydb:flyway-mysql")
}

application {
    mainClass.set("example.micronaut.jdbc.JdbcMicronautApplicationKt")
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

graalvmNative.toolchainDetection.set(false)

micronaut {
    val micronautVersion: String by project
    version(micronautVersion)
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("example.micronaut.jdbc.*")
    }
}

allOpen {
    annotation("io.micronaut.http.annotation.Controller")
}
