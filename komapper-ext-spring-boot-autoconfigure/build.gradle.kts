dependencies {
    implementation(libs.bundles.ext.spring.boot.autoconfigure)
    implementation(project(":komapper-core"))
    testImplementation(project(":komapper-ext-slf4j"))
    testImplementation(project(":komapper-jdbc-h2"))
    testImplementation(libs.logback.classic)
    testImplementation(libs.hikariCP)
}
