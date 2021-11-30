package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.InsertOptions

@ThreadSafe
data class EntityInsertContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val options: InsertOptions = InsertOptions.default,
) : TablesProvider {

    override fun getTables(): Set<TableExpression<*>> {
        return setOf(target)
    }

    fun asEntityUpsertContext(
        keys: List<PropertyMetamodel<ENTITY, *, *>>,
        duplicateKeyType: DuplicateKeyType
    ): EntityUpsertContext<ENTITY, ID, META> {
        return EntityUpsertContext(
            insertContext = this,
            target = target,
            keys = keys.ifEmpty { target.idProperties() },
            duplicateKeyType = duplicateKeyType
        )
    }

    fun asRelationInsertValuesContext(declaration: AssignmentDeclaration<ENTITY, META>): RelationInsertValuesContext<ENTITY, ID, META> {
        return RelationInsertValuesContext(
            target = target,
            values = declaration,
            options = options
        )
    }

    fun asRelationInsertSelectContext(block: () -> SubqueryExpression<ENTITY>): RelationInsertSelectContext<ENTITY, ID, META> {
        return RelationInsertSelectContext(
            target = target,
            select = block(),
            options = options.copy(disableSequenceAssignment = true)
        )
    }
}
