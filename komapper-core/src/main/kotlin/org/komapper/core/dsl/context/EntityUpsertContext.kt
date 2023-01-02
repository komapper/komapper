package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

@ThreadSafe
data class EntityUpsertContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val insertContext: EntityInsertContext<ENTITY, ID, META>,
    val target: META,
    val excluded: META = target.newMetamodel(
        table = "excluded",
        catalog = "",
        schema = "",
        alwaysQuote = false,
        disableSequenceAssignment = false,
        declaration = {},
    ),
    val keys: List<PropertyMetamodel<ENTITY, *, *>> = emptyList(),
    val duplicateKeyType: DuplicateKeyType,
    val set: AssignmentDeclaration<ENTITY, META> = {},
) : TablesProvider {

    override fun getTables(): Set<TableExpression<*>> {
        return setOf(target)
    }
}

enum class DuplicateKeyType {
    UPDATE, IGNORE
}
