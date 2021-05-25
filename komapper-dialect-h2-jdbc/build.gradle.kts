dependencies {
    api(project(":komapper-dialect-h2"))
    api(project(":komapper-jdbc"))
    runtimeOnly("com.h2database:h2:1.4.200")
}
