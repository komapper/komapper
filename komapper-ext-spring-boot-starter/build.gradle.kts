dependencies {
    val springBootVersion: String by project
    api("org.springframework.boot:spring-boot-starter:$springBootVersion")
    api("org.springframework.boot:spring-boot-starter-jdbc:$springBootVersion")
    api(project(":komapper-annotation"))
    api(project(":komapper-jdbc"))
    api(project(":komapper-ext-sqlcommenter"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly(project(":komapper-ext-slf4j"))
    runtimeOnly(project(":komapper-ext-spring-boot-autoconfigure"))
    runtimeOnly(project(":komapper-jdbc-h2"))
    runtimeOnly(project(":komapper-jdbc-mysql"))
    runtimeOnly(project(":komapper-jdbc-postgresql"))
}
