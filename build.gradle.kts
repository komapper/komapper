plugins {
    java
    kotlin("jvm")
    id("com.diffplug.spotless") version "5.14.2"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("net.researchgate.release") version "2.8.1"
}

val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")

allprojects {
    apply(plugin = "com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlinGradle {
            ktlint("0.41.0")
        }
    }

    tasks {
        build {
            dependsOn(spotlessApply)
        }
    }

    repositories {
        mavenCentral()
    }
}

val libraryProjects = subprojects.filter {
    it.name.startsWith("komapper") && !it.name.endsWith("platform")
}

val platformProject = project("komapper-platform")

val gradlePluginProject = project("gradle-plugin")

val exampleProjects = subprojects.filter {
    it.name.startsWith("example")
}

val integrationTestProjects = subprojects.filter {
    it.name.startsWith("integration-test")
}

configure(libraryProjects + exampleProjects + integrationTestProjects) {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            targetExclude("build/**")
            ktlint("0.41.0")
        }
    }
}

configure(listOf(gradlePluginProject)) {
    apply(plugin = "java")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat("1.7")
        }
    }
}

configure(libraryProjects + gradlePluginProject) {
    configure<JavaPluginExtension> {
        withJavadocJar()
        withSourcesJar()
    }
}

configure(libraryProjects + gradlePluginProject + exampleProjects + integrationTestProjects) {
    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.7.2")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    }
}

configure(libraryProjects + platformProject) {
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    val component = if (this == platformProject) {
        apply(plugin = "java-platform")
        "javaPlatform"
    } else {
        "java"
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components[component])
                pom {
                    val projectUrl: String by project
                    name.set(project.name)
                    description.set("Kotlin SQL Mapper")
                    url.set(projectUrl)
                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("nakamura-to")
                            name.set("Toshihiro Nakamura")
                            email.set("toshihiro.nakamura@gmail.com")
                        }
                    }
                    scm {
                        val githubUrl: String by project
                        connection.set("scm:git:$githubUrl")
                        developerConnection.set("scm:git:$githubUrl")
                        url.set(projectUrl)
                    }
                }
            }
        }
    }

    configure<SigningExtension> {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        val publishing = extensions.getByType(PublishingExtension::class)
        sign(publishing.publications)
        isRequired = isReleaseVersion
    }
}

rootProject.apply {

    release {
        newVersionCommitMessage = "[Gradle Release Plugin] - [skip ci] new version commit: "
        tagTemplate = "v\$version"
        with(propertyMissing("git") as net.researchgate.release.GitAdapter.GitConfig) {
            requireBranch = "main"
        }
    }

    nexusPublishing {
        repositories {
            sonatype {
                nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
                snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            }
        }
    }
}
