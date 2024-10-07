package org.komapper.core.dsl.context

import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.element.Returning
import org.komapper.core.dsl.expression.AssignmentDeclaration
import org.komapper.core.dsl.expression.TableExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.where
import org.komapper.core.dsl.options.UpdateOptions

@ThreadSafe
data class EntityUpdateContext<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    val target: META,
    val includedProperties: List<PropertyMetamodel<ENTITY, *, *>> = emptyList(),
    val excludedProperties: List<PropertyMetamodel<ENTITY, *, *>> = emptyList(),
    override val returning: Returning = Returning.Expressions(emptyList()),
    override val options: UpdateOptions,
) : TablesProvider, WhereProvider, ReturningProvider {

    override fun getTables(): Set<TableExpression<*>> {
        return setOf(target)
    }

    override fun getCompositeWhere(): WhereDeclaration {
        return target.where
    }

    fun getTargetProperties(): List<PropertyMetamodel<ENTITY, *, *>> {
        val idProperties = target.idProperties()
        val versionProperty = target.versionProperty()
        val createdAtProperty = target.createdAtProperty()
        val updatedAtProperty = target.updatedAtProperty()
        val base = includedProperties.toSet().ifEmpty { target.properties().toSet() } - excludedProperties.toSet()
        val subtraction = idProperties.toSet() + setOfNotNull(createdAtProperty) +
            target.properties().filter { !it.updatable }.toSet()
        val addition = setOfNotNull(versionProperty, updatedAtProperty)
        return (base - subtraction + addition).toList()
    }

    fun asRelationUpdateContext(declaration: AssignmentDeclaration<ENTITY, META>): RelationUpdateContext<ENTITY, ID, META> {
        return RelationUpdateContext(target, declaration, options = options)
    }
}
