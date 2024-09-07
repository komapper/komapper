import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

dependencies {
    compileOnly(project(":komapper-annotation"))
    implementation(project(":komapper-core"))
    ksp(project(":komapper-processor"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

tasks {
    val jvmTargetVersion = 11

    withType<JavaCompile>().configureEach {
        options.release.set(jvmTargetVersion)
    }

    withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xjdk-release=$jvmTargetVersion")
            jvmTarget.set(JvmTarget.fromTarget(jvmTargetVersion.toString()))
            apiVersion.set(KotlinVersion.KOTLIN_1_7)
        }
    }
}

ksp {
    arg("komapper.enableEntityMetamodelListing", "true")
}
