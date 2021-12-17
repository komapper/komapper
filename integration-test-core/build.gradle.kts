plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    compileOnly(project(":komapper-annotation"))
    ksp(project(":komapper-processor"))
    api(project(":komapper-core"))
    runtimeOnly(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly("ch.qos.logback:logback-classic:1.2.9")
}

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
