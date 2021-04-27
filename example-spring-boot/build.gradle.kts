plugins {
    idea
    id("com.google.devtools.ksp") version "1.4.32-1.0.0-alpha08"
    id("org.springframework.boot") version "2.4.5"
    kotlin("plugin.allopen") version "1.4.32"
}

apply(plugin = "io.spring.dependency-management")

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
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(project(":komapper-ext-spring-boot-starter"))
    implementation(project(":komapper-jdbc-h2"))
    implementation("com.h2database:h2:1.4.200")
    ksp(project(":komapper-processor"))
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}

allOpen {
    annotation("org.springframework.transaction.annotation.Transactional")
}
