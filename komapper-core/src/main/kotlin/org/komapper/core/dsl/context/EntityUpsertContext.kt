package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.WhereOptions

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
    val where: WhereDeclaration = {},
) : WhereProvider, TablesProvider {

    override val options: WhereOptions
        get() = insertContext.options

    override fun getTables(): Set<TableExpression<*>> {
        return setOf(target)
    }

    override fun getCompositeWhere(): WhereDeclaration {
        return where
    }
}

enum class DuplicateKeyType {
    UPDATE, IGNORE
}
