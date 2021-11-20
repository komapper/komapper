package org.komapper.core.dsl.builder

import org.komapper.core.dsl.context.QueryContext
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.Values
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.OnScope
import org.komapper.core.dsl.scope.SetScope
import org.komapper.core.dsl.scope.ValuesScope
import org.komapper.core.dsl.scope.WhereScope

internal fun QueryContext.getWhereCriteria(): List<Criterion> {
    val declarations = getWhereDeclarations()
    return WhereScope().apply { declarations.forEach { it() } }
}

internal fun Join<*, *, *>.getOnCriteria(): List<Criterion> {
    return OnScope().apply { on() }
}

internal fun SelectContext<*, *, *>.getHavingCriteria(): List<Criterion> {
    return HavingScope().apply { having.forEach { it() } }
}

internal fun <ENTITY : Any> RelationUpdateContext<ENTITY, *, *>.getAssignments(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
    return SetScope<ENTITY>().apply { set.forEach { it() } }
}

internal fun <ENTITY : Any> Values.Declarations<ENTITY>.getAssignments(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
    return ValuesScope<ENTITY>().apply { declarations.forEach { it() } }
}
