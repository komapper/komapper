dependencies {
    api(project(":komapper-annotation"))
    api(project(":komapper-jdbc"))
    api(project(":komapper-jdbc-tx"))
    api(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly(project(":komapper-jdbc-dialect-h2"))
    runtimeOnly(project(":komapper-jdbc-dialect-mysql"))
    runtimeOnly(project(":komapper-jdbc-dialect-postgresql"))
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
}
