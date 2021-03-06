plugins {
    `java-gradle-plugin`
    // use this plugin to enable the "publishToMavenLocal" task
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.15.0"
}

// we don't publish this project to sonatype
gradle.startParameter.excludedTaskNames.add(":gradle-plugin:publishToSonatype")

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

    mavenCoordinates {
        groupId = project.group.toString()
        artifactId = project.name
    }
}

dependencies {
    implementation(project(":komapper-codegen"))
    testImplementation(gradleTestKit())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion("1.4.31")
        }
    }
}
