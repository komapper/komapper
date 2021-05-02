package org.komapper.core.dsl.scope

typealias OnDeclaration<ENTITY> = OnScope<ENTITY>.() -> Unit
typealias WhereDeclaration = WhereScope.() -> Unit
typealias HavingDeclaration = HavingScope.() -> Unit
typealias ValuesDeclaration<ENTITY> = ValuesScope<ENTITY>.() -> Unit
typealias SetDeclaration<ENTITY> = SetScope<ENTITY>.() -> Unit
typealias WhenDeclaration = WhenScope.() -> Unit
