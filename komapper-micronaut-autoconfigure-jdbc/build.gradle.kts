plugins {
    kotlin("kapt")
}

dependencies {
    val micronautVersion: String by project
    kapt(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt("io.micronaut:micronaut-inject-java")
    implementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    implementation("io.micronaut:micronaut-inject")
    implementation("io.micronaut.data:micronaut-data-tx")
    implementation("io.micronaut.sql:micronaut-jdbc")
    implementation(project(":komapper-jdbc"))
    implementation(project(":komapper-micronaut-jdbc"))
}
