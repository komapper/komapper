package org.komapper.core.dsl.context

import org.komapper.core.dsl.expression.UpsertAssignmentDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

data class EntityUpsertContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val insertContext: EntityInsertContext<ENTITY, ID, META>,
    val target: META,
    val excluded: META = target.newMetamodel(
        table = "excluded",
        catalog = "",
        schema = "",
        alwaysQuote = false,
        disableSequenceAssignment = false,
        declaration = {}
    ),
    val keys: List<PropertyMetamodel<ENTITY, *, *>> = emptyList(),
    val duplicateKeyType: DuplicateKeyType,
    val set: UpsertAssignmentDeclaration<ENTITY, META> = {},
) : QueryContext {

    override fun getEntityMetamodels(): Set<EntityMetamodel<*, *, *>> {
        return setOf(target)
    }
}

enum class DuplicateKeyType {
    UPDATE, IGNORE
}
