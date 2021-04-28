plugins {
    id("org.springframework.boot") version "2.4.5"
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation("org.springframework:spring-jdbc")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation(project(":komapper-core"))
    testImplementation(project(":komapper-ext-slf4j"))
    testImplementation(project(":komapper-jdbc-h2"))
    testImplementation("ch.qos.logback:logback-classic")
    testImplementation("com.zaxxer:HikariCP")
}

tasks.bootJar {
    enabled = false
}
