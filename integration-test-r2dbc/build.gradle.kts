plugins {
    idea
    id("com.google.devtools.ksp")
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
    implementation(project(":komapper-tx-r2dbc"))
    runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
    runtimeOnly(project(":komapper-dialect-postgresql-r2dbc"))
    runtimeOnly(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}

tasks {
    test {
        val driver: Any by project
        val databaseKey = "$driver.database"
        val userKey = "$driver.user"
        val passwordKey = "$driver.password"
        val database = project.findProperty(databaseKey) ?: ""
        val user = project.findProperty(userKey) ?: ""
        val password = project.findProperty(passwordKey) ?: ""
        systemProperty("driver", driver)
        systemProperty("database", database)
        systemProperty("user", user)
        systemProperty("password", password)
    }
}
