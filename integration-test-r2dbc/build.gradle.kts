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
    implementation(project(":komapper-tx-jdbc"))
    implementation(project(":komapper-dialect-h2-r2dbc"))
    implementation(project(":komapper-dialect-h2-jdbc"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.5.0")
    runtimeOnly("io.r2dbc:r2dbc-h2:0.8.4.RELEASE")
    runtimeOnly(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
}

tasks {
    test {
        val r2dbc: Any by project
        val urlKey = "$r2dbc.url"
        val userKey = "$r2dbc.user"
        val passwordKey = "$r2dbc.password"
        val url = project.property(urlKey) ?: throw GradleException("The $urlKey property is not found.")
        val user = project.property(userKey) ?: throw GradleException("The $userKey property is not found.")
        val password = project.property(passwordKey) ?: throw GradleException("The $passwordKey property is not found.")
        systemProperty("url", url)
        systemProperty("user", user)
        systemProperty("password", password)
    }
}
