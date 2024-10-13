plugins {
    `java-library`
}

dependencies {
    implementation(libs.quarkus.core.deployment)
    implementation(libs.quarkus.arc.deployment)
    implementation(libs.quarkus.agroal.deployment)
    implementation(libs.quarkus.datasource.deployment)
    implementation(project(":komapper-quarkus-jdbc"))
}
