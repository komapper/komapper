plugins {
    id("com.gradle.plugin-publish") version "1.1.0"
}

// we don't publish this project to sonatype
gradle.startParameter.excludedTaskNames.add(":gradle-plugin:publishToSonatype")

val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")

gradlePlugin {
    plugins {
        create("gradlePlugin") {
            id = "org.komapper.gradle"
            displayName = project.description
            description = project.description
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

tasks {
    publishPlugins {
        enabled = isReleaseVersion
    }
}
