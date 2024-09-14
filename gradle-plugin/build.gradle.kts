plugins {
    id("com.gradle.plugin-publish") version "1.3.0"
}

// we don't publish this project to sonatype
gradle.startParameter.excludedTaskNames.add(":gradle-plugin:publishToSonatype")

val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")

gradlePlugin {
    website.set("https://github.com/komapper/komapper")
    vcsUrl.set("https://github.com/komapper/komapper.git")
    plugins {
        create("gradlePlugin") {
            id = "org.komapper.gradle"
            displayName = project.description
            description = project.description
            implementationClass = "org.komapper.gradle.KomapperPlugin"
            tags.set(listOf("komapper", "code generator"))
        }
    }
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
