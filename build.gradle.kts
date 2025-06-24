import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
    signing
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spotless)
    alias(libs.plugins.publish)
    alias(libs.plugins.release)
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
val javaProjects = subprojects.filter {
    it.name.startsWith("komapper-quarkus") || it.name == "komapper-codegen"
} + gradlePluginProject
val kotlinProjects = subprojects - platformProject - javaProjects.toSet()

val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")

// Retain a reference to rootProject.libs to make the version catalog accessible within allprojects and subprojects.
// See https://github.com/gradle/gradle/issues/16708
val catalog = libs

allprojects {
    apply(plugin = "base")
    apply(plugin = catalog.plugins.spotless.get().pluginId)

    repositories {
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }

    spotless {
        lineEndings = com.diffplug.spotless.LineEnding.UNIX
        kotlinGradle {
            ktlint(catalog.ktlint.get().version)
        }
    }

    tasks {
        build {
            dependsOn(spotlessApply)
        }
    }
}

configure(libraryProjects + gradlePluginProject + exampleProjects + integrationTestProjects) {
    apply(plugin = "java")
    apply(plugin = catalog.plugins.kotlin.jvm.get().pluginId)

    dependencies {
        testImplementation(rootProject.libs.kotlin.test)
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
            vendor.set(JvmVendorSpec.ADOPTIUM)
        }
    }

    val jvmTargetVersion = if (project.name.contains("spring")) 17 else 11

    tasks {
        withType<Test>().configureEach {
            useJUnitPlatform {
                val tags = (project.properties["excludeTags"] ?: "").toString()
                    .split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    .toTypedArray()
                excludeTags(*tags)
            }
            jvmArgs("-Xmx4g")
        }

        withType<JavaCompile>().configureEach {
            options.release.set(jvmTargetVersion)
        }

        withType<KotlinCompile>().configureEach {
            compilerOptions {
                freeCompilerArgs.add("-Xjdk-release=$jvmTargetVersion")
                jvmTarget.set(JvmTarget.fromTarget(jvmTargetVersion.toString()))
                apiVersion.set(KotlinVersion.KOTLIN_2_0)
            }
        }
    }
}

configure(libraryProjects + gradlePluginProject) {
    java {
        withJavadocJar()
        withSourcesJar()
    }
}

configure(kotlinProjects) {
    spotless {
        kotlin {
            ktlint(catalog.ktlint.get().version)
            targetExclude("build/**")
        }
    }
}

configure(javaProjects) {
    spotless {
        java {
            googleJavaFormat(catalog.google.java.format.get().version)
        }
        kotlin {
            ktlint(catalog.ktlint.get().version)
            targetExclude("build/**")
        }
    }

    tasks {
        javadoc {
            (options as StandardJavadocDocletOptions).apply {
                addStringOption("Xdoclint:none", "-quiet")
            }
        }
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
                    description.set(project.description)
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
        newVersionCommitMessage.set("[Gradle Release Plugin] - [skip ci] new version commit: ")
        tagTemplate.set("v\$version")
    }

    fun replaceVersion(version: String, prefix: String, suffix: String = "\"") {
        ant.withGroovyBuilder {
            "replaceregexp"(
                "match" to """($prefix)[^"]*($suffix)""",
                "replace" to "\\1${version}\\2",
                "encoding" to "UTF-8",
                "flags" to "g",
            ) {
                "fileset"("dir" to ".") {
                    "include"("name" to "README.md")
                }
            }
        }
    }

    tasks {
        val replaceVersion by registering {
            doLast {
                val releaseVersion = project.properties["release.releaseVersion"]?.toString()
                checkNotNull(releaseVersion) { "release.releaseVersion is not set" }
                replaceVersion(releaseVersion, """val komapperVersion = """")
            }
        }

        beforeReleaseBuild {
            dependsOn(replaceVersion)
        }
    }
}
