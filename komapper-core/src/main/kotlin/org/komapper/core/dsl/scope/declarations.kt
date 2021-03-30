package org.komapper.core.dsl.scope

typealias JoinDeclaration<ENTITY> = JoinScope<ENTITY>.() -> Unit
typealias WhereDeclaration = WhereScope.() -> Unit
typealias HavingDeclaration = HavingScope.() -> Unit
typealias ValuesDeclaration = ValuesScope.() -> Unit
typealias SetDeclaration = SetScope.() -> Unit

typealias EntitySelectOptionsDeclaration = EntitySelectOptionsScope.() -> Unit
typealias EntityDeleteOptionsDeclaration = EntityDeleteOptionsScope.() -> Unit
typealias EntityInsertOptionsDeclaration = EntityInsertOptionsScope.() -> Unit
typealias EntityUpdateOptionsDeclaration = EntityUpdateOptionsScope.() -> Unit

typealias SqlSelectOptionsDeclaration = SqlSelectOptionsScope.() -> Unit
typealias SqlDeleteOptionsDeclaration = SqlDeleteOptionsScope.() -> Unit
typealias SqlInsertOptionsDeclaration = SqlInsertOptionsScope.() -> Unit
typealias SqlUpdateOptionsDeclaration = SqlUpdateOptionsScope.() -> Unit

typealias TemplateSelectOptionsDeclaration = TemplateSelectOptionsScope.() -> Unit
typealias TemplateUpdateOptionsDeclaration = TemplateUpdateOptionsScope.() -> Unit

typealias ScriptExecutionOptionsDeclaration = ScriptExecutionOptionsScope.() -> Unit
