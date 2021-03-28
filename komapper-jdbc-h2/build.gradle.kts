plugins {
    idea
    id("com.google.devtools.ksp") version "1.4.31-1.0.0-alpha06"
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
    kspTest(project(":komapper-processor"))
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}
