dependencies {
    api("org.springframework.boot:spring-boot-starter:2.4.5")
    api("org.springframework.boot:spring-boot-starter-jdbc:2.4.5")
    api(project(":komapper-annotation"))
    api(project(":komapper-core"))
    implementation(project(":komapper-ext-spring-boot"))
    implementation(project(":komapper-logging-slf4j"))
}
