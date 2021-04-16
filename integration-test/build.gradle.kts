plugins {
    idea
    id("com.google.devtools.ksp") version "1.4.32-1.0.0-alpha07"
}

sourceSets {
    test {
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
    implementation(project(":komapper-annotation"))
    implementation(project(":komapper-template"))
    ksp(project(":komapper-processor"))
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}

tasks {
    test {
        val jdbcUrl: Any by project
        val jdbcUser: Any by project
        val jdbcPassword: Any by project
        systemProperty("url", jdbcUrl)
        systemProperty("user", jdbcUser)
        systemProperty("password", jdbcPassword)
    }
}
