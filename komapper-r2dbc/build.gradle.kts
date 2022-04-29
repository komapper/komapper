dependencies {
    val kotlinCoroutinesVersion: String by project
    val r2dbcSpiVersion: String by project
    api(project(":komapper-core"))
    api(project(":komapper-tx-core"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinCoroutinesVersion")
    api("io.r2dbc:r2dbc-spi:$r2dbcSpiVersion")
}
