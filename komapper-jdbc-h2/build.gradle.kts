plugins {
    idea
    id("com.google.devtools.ksp") version "1.4.30-1.0.0-alpha05"
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
    implementation("com.h2database:h2:1.4.199")
    kspTest(project(":komapper-processor"))
}
