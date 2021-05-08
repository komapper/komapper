package org.komapper.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class KomapperPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("komapperCodeGen", CodeGenExtension::class.java, project)
        project.tasks
            .register("komapperCodeGen", CodeGenerateTask::class.java, extension)
            .configure {
                it.description = "Generate entities and definitions"
                it.group = "komapper"
                it.outputs.upToDateWhen { false }
            }
    }
}
