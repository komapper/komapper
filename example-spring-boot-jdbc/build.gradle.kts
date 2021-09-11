plugins {
    id("org.springframework.boot")
    id("com.google.devtools.ksp")
    kotlin("plugin.allopen")
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    ksp(project(":komapper-processor"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(project(":komapper-spring-boot-starter-jdbc"))
    runtimeOnly(project(":komapper-dialect-h2-jdbc"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}

allOpen {
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.transaction.annotation.Transactional")
}

springBoot {
    mainClass.set("example.ApplicationKt")
}
