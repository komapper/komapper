plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    compileOnly(project(":komapper-annotation"))
    ksp(project(":komapper-processor"))
    api(project(":komapper-core"))
    api(libs.kotlinx.datetime)
    implementation(libs.logback.classic)
    runtimeOnly(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-template"))
}

ksp {
    arg("komapper.namingStrategy", "lower_snake_case")
    arg("komapper.enableEntityMetamodelListing", "true")
    arg("komapper.enableEntityStoreContext", "true")
}

kotlin {
    explicitApi()
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-opt-in=org.komapper.annotation.KomapperExperimentalAssociation")
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }
}
