package org.komapper.core.dsl.builder

import org.komapper.core.dsl.context.QueryContext
import org.komapper.core.dsl.context.RelationUpdateContext
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.element.ColumnsAndSource
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.OnScope
import org.komapper.core.dsl.scope.SetScope
import org.komapper.core.dsl.scope.WhereScope

internal fun QueryContext.getWhereCriteria(): List<Criterion> {
    val where = getCompositeWhere()
    return WhereScope().apply(where)
}

internal fun Join<*, *, *>.getOnCriteria(): List<Criterion> {
    return OnScope().apply(on)
}

internal fun SelectContext<*, *, *>.getHavingCriteria(): List<Criterion> {
    return HavingScope().apply(having)
}

internal fun <ENTITY : Any> RelationUpdateContext<ENTITY, *, *>.getAssignments(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
    return SetScope<ENTITY>().apply(set)
}

internal fun <ENTITY : Any> ColumnsAndSource.Values<ENTITY>.getAssignments(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
    return SetScope<ENTITY>().apply(declaration)
}
