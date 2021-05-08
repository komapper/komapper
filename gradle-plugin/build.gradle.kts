plugins {
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.14.0"
}

gradlePlugin {
    plugins {
        create("gradlePlugin") {
            id = "org.komapper.gradle"
            displayName = "Komapper Gradle Plugin"
            description = "Komapper Gradle Plugin"
            implementationClass = "org.komapper.gradle.KomapperPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/komapper/komapper"
    vcsUrl = "https://github.com/komapper/komapper.git"
    tags = listOf("komapper", "code generator")
}

dependencies {
    implementation(project(":komapper-codegen"))
    testImplementation(gradleTestKit())
}
