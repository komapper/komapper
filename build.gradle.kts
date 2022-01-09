plugins {
    java
    `maven-publish`
    signing
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("net.researchgate.release") version "2.8.1"
}

val platformProject = project("komapper-platform")
val gradlePluginProject = project("gradle-plugin")
val libraryProjects = subprojects.filter {
    it.name.startsWith("komapper") && !it.name.endsWith("platform")
}
val exampleProjects = subprojects.filter {
    it.name.startsWith("example")
}
val integrationTestProjects = subprojects.filter {
    it.name.startsWith("integration-test")
}

val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")

allprojects {
    apply(plugin = "base")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        enableExperimentalRules.set(true)
        disabledRules.set(
            listOf(
                "experimental:annotation",
                "experimental:annotation-spacing",
                "experimental:argument-list-wrapping",
                "experimental:enum-entry-name-case",
                "experimental:multiline-if-else",
                "experimental:no-empty-first-line-in-method-block",
                "experimental:package-name",
                "experimental:spacing-around-angle-brackets",
                "experimental:double-colon-spacing",
                "experimental:unary-op-spacing",
                "experimental:spacing-between-declarations-with-annotations",
                // "experimental:spacing-between-declarations-with-comments"
            )
        )
        filter {
            exclude("build/**")
        }
    }

    repositories {
        mavenCentral()
    }

    tasks {
        val pairs = listOf(
            "ktlintKotlinScriptCheck" to "ktlintKotlinScriptFormat",
            "ktlintMainSourceSetCheck" to "ktlintMainSourceSetFormat",
            "ktlintTestSourceSetCheck" to "ktlintTestSourceSetFormat",
        )
        for ((checkTask, formatTask) in pairs) {
            findByName(checkTask)?.mustRunAfter(formatTask)
        }

        build {
            dependsOn("ktlintFormat")
        }
    }
}

configure(libraryProjects + gradlePluginProject + exampleProjects + integrationTestProjects) {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        testImplementation(kotlin("test"))
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }

    tasks {
        withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}

configure(libraryProjects + gradlePluginProject) {
    java {
        withJavadocJar()
        withSourcesJar()
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

    publishing {
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

    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        val publishing = extensions.getByType(PublishingExtension::class)
        sign(publishing.publications)
        isRequired = isReleaseVersion
    }
}

rootProject.apply {
    nexusPublishing {
        repositories {
            sonatype {
                nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
                snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            }
        }
    }

    release {
        newVersionCommitMessage = "[Gradle Release Plugin] - [skip ci] new version commit: "
        tagTemplate = "v\$version"
        with(propertyMissing("git") as net.researchgate.release.GitAdapter.GitConfig) {
            requireBranch = "main"
        }
    }

    tasks {
        val replaceVersion by registering {
            doLast {
                val releaseVersion = project.properties["release.releaseVersion"]?.toString()
                checkNotNull(releaseVersion) { "release.releaseVersion is not set" }
                ant.withGroovyBuilder {
                    "replaceregexp"(
                        "match" to """(val komapperVersion = ")[^"]*(")""",
                        "replace" to "\\1${releaseVersion}\\2",
                        "encoding" to "UTF-8",
                        "flags" to "g"
                    ) {
                        "fileset"("dir" to ".") {
                            "include"("name" to "README.md")
                        }
                    }
                }
            }
        }

        beforeReleaseBuild {
            dependsOn(replaceVersion)
        }
    }
}
