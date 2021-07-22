dependencies {
    api(project(":komapper-annotation"))
    api(project(":komapper-r2dbc"))
    api(project(":komapper-tx-r2dbc"))
    api(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly(project(":komapper-dialect-h2-r2dbc"))
    runtimeOnly(project(":komapper-dialect-mariadb-r2dbc"))
    runtimeOnly(project(":komapper-dialect-mysql-r2dbc"))
    runtimeOnly(project(":komapper-dialect-postgresql-r2dbc"))
    runtimeOnly("ch.qos.logback:logback-classic:1.2.4")
}
