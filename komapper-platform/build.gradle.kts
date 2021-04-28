dependencies {
    constraints {
        for (project in rootProject.subprojects.filter { it.name.startsWith("komapper") && !it.name.endsWith("platform") }) {
            api(project)
        }
    }
}
