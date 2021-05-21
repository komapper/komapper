plugins {
    idea
    id("org.springframework.boot")
    id("com.google.devtools.ksp")
    kotlin("plugin.allopen") version "1.5.0"
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
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(project(":komapper-ext-spring-boot-starter"))
    ksp(project(":komapper-processor"))
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
