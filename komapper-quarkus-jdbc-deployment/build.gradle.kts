plugins {
    `java-library`
}

dependencies {
    val quarkusVersion: String by project
    implementation("io.quarkus:quarkus-core-deployment:$quarkusVersion")
    implementation("io.quarkus:quarkus-arc-deployment:$quarkusVersion")
    implementation("io.quarkus:quarkus-agroal-deployment:$quarkusVersion")
    implementation("io.quarkus:quarkus-datasource-deployment:$quarkusVersion")
    implementation(project(":komapper-quarkus-jdbc"))
}
