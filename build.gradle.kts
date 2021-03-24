import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.4.30" apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
}

val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    ktlint {
        version.set("0.41.0")
        verbose.set(true)
        outputToConsole.set(true)
        coloredOutput.set(true)
        reporters {
            reporter(ReporterType.JSON)
        }
        filter {
            exclude { element -> element.file.path.contains("generated/") }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    configure<JavaPluginExtension> {
        withJavadocJar()
        withSourcesJar()
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.7.1")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.7.1")
        "testRuntimeOnly"("org.junit.vintage:junit-vintage-engine:5.7.1")
    }
    
}

configure(subprojects.filter {it.name != "example"}) {

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
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
//        val signingKey: String? by project
//        val signingPassword: String? by project
//        useInMemoryPgpKeys(signingKey, signingPassword)
        val publishing = convention.findByType(PublishingExtension::class)!!
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
}
