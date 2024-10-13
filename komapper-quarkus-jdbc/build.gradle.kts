plugins {
    `java-library`
    alias(libs.plugins.quarkus)
}

quarkusExtension {
    deploymentModule.set("komapper-quarkus-jdbc-deployment")
}

dependencies {
    api(platform(libs.quarkus.bom))
    api(libs.quarkus.core)
    api(libs.quarkus.arc)
    api(libs.quarkus.agroal)
    api(project(":komapper-jdbc"))
    api(project(":komapper-annotation"))
    implementation(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-datetime-jdbc"))
    runtimeOnly(project(":komapper-template"))
}
