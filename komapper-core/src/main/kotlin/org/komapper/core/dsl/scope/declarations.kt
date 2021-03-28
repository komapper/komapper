package org.komapper.core.dsl.scope

typealias JoinDeclaration<ENTITY> = JoinScope<ENTITY>.() -> Unit
typealias WhereDeclaration = WhereScope.() -> Unit
typealias HavingDeclaration = HavingScope.() -> Unit
typealias ValuesDeclaration = ValuesScope.() -> Unit
typealias SetDeclaration = SetScope.() -> Unit
