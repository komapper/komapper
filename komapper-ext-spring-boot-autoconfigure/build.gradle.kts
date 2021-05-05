dependencies {
    implementation(libs.bundles.ext.spring.boot.autoconfigure)
    implementation(project(":komapper-core"))
    testImplementation(project(":komapper-ext-slf4j"))
    testImplementation(project(":komapper-jdbc-h2"))
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("com.zaxxer:HikariCP:4.0.3")
}
