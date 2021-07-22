dependencies {
    val springBootVersion: String by project
    api("org.springframework.boot:spring-boot-starter:$springBootVersion")
    api("org.springframework.boot:spring-boot-starter-jdbc:$springBootVersion")
    api(project(":komapper-annotation"))
    api(project(":komapper-jdbc"))
    api(project(":komapper-sqlcommenter"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-dialect-h2-jdbc"))
    runtimeOnly(project(":komapper-dialect-mariadb-jdbc"))
    runtimeOnly(project(":komapper-dialect-mysql-jdbc"))
    runtimeOnly(project(":komapper-dialect-postgresql-jdbc"))
    runtimeOnly(project(":komapper-spring-boot-autoconfigure-jdbc"))
}
