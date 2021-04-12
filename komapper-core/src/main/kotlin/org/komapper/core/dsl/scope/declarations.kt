package org.komapper.core.dsl.scope

typealias OnDeclaration<ENTITY> = OnScope<ENTITY>.() -> Unit
typealias WhereDeclaration = WhereScope.() -> Unit
typealias HavingDeclaration = HavingScope.() -> Unit
typealias ValuesDeclaration<ENTITY> = ValuesScope<ENTITY>.() -> Unit
typealias SetDeclaration<ENTITY> = SetScope<ENTITY>.() -> Unit

typealias EntityBatchDeleteOptionDeclaration = EntityBatchDeleteOptionScope.() -> Unit
typealias EntityBatchInsertOptionDeclaration = EntityBatchInsertOptionScope.() -> Unit
typealias EntityBatchUpdateOptionDeclaration = EntityBatchUpdateOptionScope.() -> Unit

typealias EntitySelectOptionDeclaration = EntitySelectOptionScope.() -> Unit
typealias EntityDeleteOptionDeclaration = EntityDeleteOptionScope.() -> Unit
typealias EntityInsertOptionDeclaration = EntityInsertOptionScope.() -> Unit
typealias EntityUpdateOptionDeclaration = EntityUpdateOptionScope.() -> Unit

typealias SqlSelectOptionDeclaration = SqlSelectOptionScope.() -> Unit
typealias SqlDeleteOptionDeclaration = SqlDeleteOptionScope.() -> Unit
typealias SqlInsertOptionDeclaration = SqlInsertOptionScope.() -> Unit
typealias SqlUpdateOptionDeclaration = SqlUpdateOptionScope.() -> Unit
typealias SqlSetOperationOptionDeclaration = SqlSetOperationOptionScope.() -> Unit

typealias ScriptExecutionOptionDeclaration = ScriptExecuteOptionScope.() -> Unit

typealias TemplateSelectOptionDeclaration = TemplateSelectOptionScope.() -> Unit
typealias TemplateUpdateOptionDeclaration = TemplateUpdateOptionScope.() -> Unit
