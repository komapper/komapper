plugins {
    id("org.springframework.boot") version "2.4.5"
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-jdbc")
    api(project(":komapper-annotation"))
    api(project(":komapper-core"))
    api(project(":komapper-ext-sqlcommenter"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly(project(":komapper-ext-slf4j"))
    runtimeOnly(project(":komapper-ext-spring-boot"))
    runtimeOnly(project(":komapper-jdbc-h2"))
    runtimeOnly(project(":komapper-jdbc-mysql"))
    runtimeOnly(project(":komapper-jdbc-postgresql"))
}

tasks.bootJar {
    enabled = false
}
