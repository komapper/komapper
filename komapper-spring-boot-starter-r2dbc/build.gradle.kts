dependencies {
    val kotlinCoroutinesVersion: String by project
    val r2dbcBomVersion: String by project
    val springVersion: String by project
    val springBootVersion: String by project
    implementation(platform("io.r2dbc:r2dbc-bom:$r2dbcBomVersion"))
    api("org.springframework:spring-beans:$springVersion")
    api("org.springframework:spring-core:$springVersion")
    api("org.springframework:spring-context:$springVersion")
    api("org.springframework:spring-r2dbc:$springVersion")
    api("org.springframework:spring-tx:$springVersion")
    api("org.springframework.boot:spring-boot-starter:$springBootVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinCoroutinesVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinCoroutinesVersion")
    api("io.r2dbc:r2dbc-pool")
    api(project(":komapper-annotation"))
    api(project(":komapper-r2dbc"))
    runtimeOnly(project(":komapper-datetime-r2dbc"))
    runtimeOnly(project(":komapper-template"))
    runtimeOnly(project(":komapper-slf4j"))
    runtimeOnly(project(":komapper-spring-boot-autoconfigure-r2dbc"))
}
