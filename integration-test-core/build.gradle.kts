plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    val kotlinxDatetime: String by project
    compileOnly(project(":komapper-annotation"))
    ksp(project(":komapper-processor"))
    api(project(":komapper-core"))
    api("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetime")
    implementation("ch.qos.logback:logback-classic:1.5.6")
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
            freeCompilerArgs.add("-Xcontext-receivers")
        }
    }
}
