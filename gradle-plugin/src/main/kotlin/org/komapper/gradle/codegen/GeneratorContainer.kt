package org.komapper.gradle.codegen

import org.gradle.api.NamedDomainObjectContainer

interface GeneratorContainer : NamedDomainObjectContainer<Generator>

internal class GeneratorContainerImpl(container: NamedDomainObjectContainer<Generator>) :
    NamedDomainObjectContainer<Generator> by container, GeneratorContainer
