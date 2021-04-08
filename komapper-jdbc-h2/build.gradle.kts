plugins {
    idea
    id("com.google.devtools.ksp") version "1.4.32-1.0.0-alpha07"
}

sourceSets {
    test {
        java {
            srcDir("build/generated/ksp/test/kotlin")
        }
    }
}

idea.module {
    generatedSourceDirs.add(file("build/generated/ksp/main/kotlin"))
}

dependencies {
    api(project(":komapper-core"))
    implementation("com.h2database:h2:1.4.200")
    testImplementation(project(":komapper-annotation"))
    testImplementation(project(":komapper-template"))
    kspTest(project(":komapper-processor"))
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
