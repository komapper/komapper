package org.komapper.core.dsl.scope

typealias JoinDeclaration<ENTITY> = JoinScope<ENTITY>.() -> Unit
typealias WhereDeclaration = WhereScope.() -> Unit
typealias HavingDeclaration = HavingScope.() -> Unit
