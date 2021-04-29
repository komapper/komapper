dependencies {
    api(libs.bundles.ext.spring.boot.starter)
    api(project(":komapper-annotation"))
    api(project(":komapper-core"))
    api(project(":komapper-ext-sqlcommenter"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly(project(":komapper-ext-slf4j"))
    runtimeOnly(project(":komapper-ext-spring-boot-autoconfigure"))
    runtimeOnly(project(":komapper-jdbc-h2"))
    runtimeOnly(project(":komapper-jdbc-mysql"))
    runtimeOnly(project(":komapper-jdbc-postgresql"))
}
