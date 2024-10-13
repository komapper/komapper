dependencies {
    api(libs.spring.boot.starter)
    api(libs.spring.boot.starter.jdbc)
    api(project(":komapper-annotation"))
    api(project(":komapper-jdbc"))
    runtimeOnly(project(":komapper-datetime-jdbc"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-spring-boot-autoconfigure-jdbc"))
}
