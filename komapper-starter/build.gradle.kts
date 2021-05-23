dependencies {
    api(project(":komapper-annotation"))
    api(project(":komapper-jdbc"))
    api(project(":komapper-transaction"))
    api(project(":komapper-ext-slf4j"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly(project(":komapper-jdbc-h2"))
    runtimeOnly(project(":komapper-jdbc-mysql"))
    runtimeOnly(project(":komapper-jdbc-postgresql"))
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
}
