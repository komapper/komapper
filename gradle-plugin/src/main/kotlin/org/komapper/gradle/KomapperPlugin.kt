package org.komapper.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.komapper.gradle.codegen.GenerateTask

class KomapperPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension =
            project.extensions.create(KomapperExtension.NAME, KomapperExtension::class.java, project)

        extension.generators.all { generator ->
            project.tasks
                .register("komapper${generator.name.capitalize()}Generator", GenerateTask::class.java, generator)
                .configure {
                    it.description = "Generate entities and definitions"
                    it.group = KomapperExtension.NAME
                    it.outputs.upToDateWhen { false }
                }
        }
    }
}
