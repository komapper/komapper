plugins {
    `java-library`
    id("io.quarkus.extension")
}

quarkusExtension {
    deploymentModule = "komapper-quarkus-jdbc-deployment"
}

dependencies {
    val quarkusVersion: String by project
    api(platform("io.quarkus:quarkus-bom:$quarkusVersion"))
    api("io.quarkus:quarkus-core")
    api("io.quarkus:quarkus-arc")
    api("io.quarkus:quarkus-agroal")
    api(project(":komapper-jdbc"))
    api(project(":komapper-annotation"))
    implementation(project(":komapper-slf4j"))
}
