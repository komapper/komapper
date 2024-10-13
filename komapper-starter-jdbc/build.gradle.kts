dependencies {
    api(project(":komapper-annotation"))
    api(project(":komapper-jdbc"))
    api(project(":komapper-tx-jdbc"))
    api(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-datetime-jdbc"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly(libs.logback.classic)
}
