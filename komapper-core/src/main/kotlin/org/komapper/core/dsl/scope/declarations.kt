package org.komapper.core.dsl.scope

typealias OnDeclaration<ENTITY> = OnScope<ENTITY>.() -> Unit
typealias WhereDeclaration = WhereScope.() -> Unit
typealias HavingDeclaration = HavingScope.() -> Unit
typealias ValuesDeclaration = ValuesScope.() -> Unit
typealias SetDeclaration = SetScope.() -> Unit

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

typealias TemplateSelectOptionDeclaration = TemplateSelectOptionScope.() -> Unit
typealias TemplateUpdateOptionDeclaration = TemplateUpdateOptionScope.() -> Unit

typealias ScriptExecutionOptionDeclaration = ScriptExecuteOptionScope.() -> Unit
