dependencies {
    val micronautVersion: String by project
    implementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    api("io.micronaut.sql:micronaut-jdbc-hikari")
    api(project(":komapper-annotation"))
    api(project(":komapper-jdbc"))
    api(project(":komapper-micronaut-autoconfigure-jdbc"))
    api(project(":komapper-slf4j"))
    api("jakarta.annotation:jakarta.annotation-api")
    runtimeOnly(project(":komapper-datetime-jdbc"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly("ch.qos.logback:logback-classic")
}
