dependencies {
    api(project(":komapper-annotation"))
    api(project(":komapper-r2dbc"))
    api(project(":komapper-tx-r2dbc"))
    api(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly("ch.qos.logback:logback-classic:1.2.10")
}
