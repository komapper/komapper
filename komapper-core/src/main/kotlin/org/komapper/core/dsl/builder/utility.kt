package org.komapper.core.dsl.builder

import org.komapper.core.dsl.context.EntityUpsertContext
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.WhereProvider
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.scope.AssignmentScope
import org.komapper.core.dsl.scope.FilterScopeSupport
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.OnScope
import org.komapper.core.dsl.scope.WhereScope

internal fun WhereProvider.getWhereCriteria(): List<Criterion> {
    val where = getCompositeWhere()
    val support = FilterScopeSupport()
    WhereScope(support).apply(where)
    return support.toList()
}

internal fun Join<*, *, *>.getOnCriteria(): List<Criterion> {
    val support = FilterScopeSupport()
    OnScope(support).apply(on)
    return support.toList()
}

internal fun SelectContext<*, *, *>.getHavingCriteria(): List<Criterion> {
    val support = FilterScopeSupport()
    HavingScope(support).apply(having)
    return support.toList()
}

internal fun <ENTITY : Any> RelationUpdateContext<ENTITY, *, *>.getAssignments(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
    val assignments = mutableListOf<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>()
    AssignmentScope(assignments).apply(set)
    return assignments
}

internal fun <ENTITY : Any> RelationInsertValuesContext<ENTITY, *, *>.getAssignments(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
    val assignments = mutableListOf<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>()
    AssignmentScope(assignments).apply(values)
    return assignments
}

fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> EntityUpsertContext<ENTITY, ID, META>.getAssignments(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
    val context = this
    val assignments = mutableListOf<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>>()
    AssignmentScope(assignments).apply {
        context.set(this, context.excluded)
    }
    return assignments
}

fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> EntityUpsertContext<ENTITY, ID, META>.createAssignments(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
    fun properties(meta: META): List<PropertyMetamodel<ENTITY, *, *>> {
        return meta.properties().filter { it != meta.createdAtProperty() } - meta.idProperties().toSet()
    }
    return properties(target).zip(properties(excluded)).map { (p1, p2) ->
        p1 to Operand.Column(p2)
    }
}
