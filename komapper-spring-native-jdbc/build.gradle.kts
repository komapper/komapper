repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo.spring.io/release") }
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    val springBootVersion: String by project
    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation(project(":komapper-core"))
    implementation(project(":komapper-jdbc"))
    implementation(project(":komapper-spring-boot-autoconfigure-jdbc"))
    compileOnly("org.springframework.experimental:spring-aot:0.11.2")
}
