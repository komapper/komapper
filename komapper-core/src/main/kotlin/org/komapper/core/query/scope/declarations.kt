package org.komapper.core.query.scope

typealias JoinDeclaration<ENTITY> = JoinScope<ENTITY>.() -> Unit
typealias WhereDeclaration = WhereScope.() -> Unit
