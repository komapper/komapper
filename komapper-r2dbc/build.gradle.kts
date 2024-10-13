dependencies {
    val kotlinCoroutinesVersion: String by project
    val r2dbcSpiVersion: String by project
    api(project(":komapper-core"))
    api(project(":komapper-tx-core"))
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.reactive)
    api(libs.r2dbc.spi)
}
