package org.komapper.gradle.codegen

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.util.ConfigureUtil

interface GeneratorContainer : NamedDomainObjectContainer<Generator> {
    fun default(closure: Closure<*>): NamedDomainObjectProvider<Generator>
    fun default(action: Action<in Generator>): NamedDomainObjectProvider<Generator>
}

internal class GeneratorContainerImpl(container: NamedDomainObjectContainer<Generator>) :
    NamedDomainObjectContainer<Generator> by container, GeneratorContainer {

    override fun default(closure: Closure<*>): NamedDomainObjectProvider<Generator> {
        return default(ConfigureUtil.configureUsing(closure))
    }

    override fun default(action: Action<in Generator>): NamedDomainObjectProvider<Generator> {
        return register("") {
            action.execute(it)
        }
    }
}
