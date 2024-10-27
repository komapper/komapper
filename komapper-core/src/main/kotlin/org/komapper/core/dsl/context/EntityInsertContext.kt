package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.element.Returning
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.options.InsertOptions

@ThreadSafe
data class EntityInsertContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    override val returning: Returning = Returning.Expressions(emptyList()),
    val options: InsertOptions,
) : TablesProvider, ReturningProvider {
    override fun getTables(): Set<TableExpression<*>> {
        return setOf(target)
    }

    fun asEntityUpsertContext(
        keys: List<PropertyMetamodel<ENTITY, *, *>>,
        duplicateKeyType: DuplicateKeyType,
        indexPredicate: WhereDeclaration?,
    ): EntityUpsertContext<ENTITY, ID, META> {
        return EntityUpsertContext(
            insertContext = this,
            target = target,
            keys = keys,
            duplicateKeyType = duplicateKeyType,
            indexPredicate = indexPredicate,
        )
    }

    fun asEntityUpsertContext(
        conflictTarget: String,
        duplicateKeyType: DuplicateKeyType,
        indexPredicate: WhereDeclaration?,
    ): EntityUpsertContext<ENTITY, ID, META> {
        return EntityUpsertContext(
            insertContext = this,
            target = target,
            conflictTarget = conflictTarget,
            duplicateKeyType = duplicateKeyType,
            indexPredicate = indexPredicate,
        )
    }

    fun asRelationInsertValuesContext(declaration: AssignmentDeclaration<ENTITY, META>): RelationInsertValuesContext<ENTITY, ID, META> {
        return RelationInsertValuesContext(
            target = target,
            values = declaration,
            returning = returning,
            options = options,
        )
    }

    fun asRelationInsertSelectContext(subquery: SubqueryExpression<ENTITY>): RelationInsertSelectContext<ENTITY, ID, META> {
        return RelationInsertSelectContext(
            target = target,
            select = subquery,
            options = options.copy(disableSequenceAssignment = true),
        )
    }
}
