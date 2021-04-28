plugins {
    idea
    id("com.google.devtools.ksp") version "1.4.32-1.0.0-alpha08"
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
    compileOnly(project(":komapper-annotation"))
    ksp(project(":komapper-processor"))
    implementation(project(":komapper-transaction"))
    implementation(libs.postgresql)

    runtimeOnly(project(":komapper-jdbc-h2"))
    runtimeOnly(project(":komapper-jdbc-mysql"))
    runtimeOnly(project(":komapper-jdbc-postgresql"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly(project(":komapper-ext-slf4j"))
    runtimeOnly(libs.logback.classic)
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}

tasks {
    test {
        val jdbc: Any by project
        val urlKey = "$jdbc.url"
        val userKey = "$jdbc.user"
        val passwordKey = "$jdbc.password"
        val url = project.property(urlKey) ?: throw GradleException("The $urlKey property is not found.")
        val user = project.property(userKey) ?: throw GradleException("The $userKey property is not found.")
        val password = project.property(passwordKey) ?: throw GradleException("The $passwordKey property is not found.")
        systemProperty("url", url)
        systemProperty("user", user)
        systemProperty("password", password)
    }
}
