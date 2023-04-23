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
    override val options: UpdateOptions = UpdateOptions.DEFAULT,
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
        val properties = includedProperties.ifEmpty { target.properties() } - excludedProperties
        val versionProperties = if (versionProperty != null && versionProperty !in properties) {
            listOf(versionProperty)
        } else {
            emptyList()
        }
        return properties.filter { it != createdAtProperty } - idProperties + versionProperties
    }

    fun asRelationUpdateContext(declaration: AssignmentDeclaration<ENTITY, META>): RelationUpdateContext<ENTITY, ID, META> {
        return RelationUpdateContext(target, declaration)
    }
}
