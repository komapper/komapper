dependencies {
    api(project(":komapper-annotation"))
    api(project(":komapper-jdbc"))
    api(project(":komapper-tx-jdbc"))
    api(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly("ch.qos.logback:logback-classic:1.2.6")
}
