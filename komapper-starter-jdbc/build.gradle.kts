dependencies {
    api(project(":komapper-annotation"))
    api(project(":komapper-jdbc"))
    api(project(":komapper-tx-jdbc"))
    api(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly(project(":komapper-dialect-h2-jdbc"))
    runtimeOnly(project(":komapper-dialect-mysql-jdbc"))
    runtimeOnly(project(":komapper-dialect-postgresql-jdbc"))
    runtimeOnly("ch.qos.logback:logback-classic:1.2.4")
}
