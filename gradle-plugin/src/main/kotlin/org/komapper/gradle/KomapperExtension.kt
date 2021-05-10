package org.komapper.gradle

import org.gradle.api.Action
import org.gradle.api.Project
import org.komapper.gradle.codegen.Generator
import org.komapper.gradle.codegen.GeneratorContainer
import org.komapper.gradle.codegen.GeneratorContainerImpl
import javax.inject.Inject

open class KomapperExtension @Inject internal constructor(private val project: Project) {
    companion object {
        const val NAME = "komapper"
    }

    internal val generators: GeneratorContainer =
        GeneratorContainerImpl(
            project.container(Generator::class.java) { name ->
                project.objects.newInstance(Generator::class.java, name, project)
            }
        )

    fun generators(action: Action<in GeneratorContainer>) {
        action.execute(generators)
    }
}
