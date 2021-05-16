package org.komapper.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.komapper.gradle.codegen.GenerateTask

@Suppress("unused")
class KomapperPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension =
            project.extensions.create(KomapperExtension.NAME, KomapperExtension::class.java, project)
        registerGenerateTasks(project, extension)
    }

    private fun registerGenerateTasks(project: Project, extension: KomapperExtension) {
        val description = "Generate entities and definitions"
        val prefix = KomapperExtension.NAME
        val suffix = "Generator"
        val aggregateTask = project.tasks.register(prefix + suffix) {
            it.description = "$description."
            it.group = KomapperExtension.NAME
            it.outputs.upToDateWhen { false }
        }
        extension.generators.all { generator ->
            val task = project.tasks
                .register("$prefix${generator.name.capitalize()}$suffix", GenerateTask::class.java, generator)
            task.configure {
                it.description = "$description for ${generator.name}."
                it.group = KomapperExtension.NAME
                it.outputs.upToDateWhen { false }
            }
            aggregateTask.configure { it.dependsOn(task) }
        }
    }
}
