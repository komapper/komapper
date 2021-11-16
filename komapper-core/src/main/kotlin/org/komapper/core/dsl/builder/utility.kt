package org.komapper.core.dsl.builder

import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.element.Values
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.OnScope
import org.komapper.core.dsl.scope.SetScope
import org.komapper.core.dsl.scope.ValuesScope
import org.komapper.core.dsl.scope.WhereScope

internal fun SelectContext<*, *, *, *>.where(): List<Criterion> {
    return where(target, where)
}

internal fun SqlDeleteContext<*, *, *>.where(): List<Criterion> {
    return where(target, where)
}

internal fun SqlUpdateContext<*, *, *>.where(): List<Criterion> {
    return where(target, where)
}

private fun where(metamodel: EntityMetamodel<*, *, *>, where: List<WhereDeclaration>): List<Criterion> {
    val w1 = metamodel.where()
    val w2 = WhereScope().apply { where.forEach { it() } }
    return w1 + w2
}

internal fun Join<*, *>.on(): List<Criterion> {
    return OnScope().apply { on() }
}

internal fun SqlSelectContext<*, *, *>.having(): List<Criterion> {
    return HavingScope().apply { having.forEach { it() } }
}

internal fun <ENTITY: Any> SqlUpdateContext<ENTITY, *, *>.set(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
    return SetScope<ENTITY>().apply { set.forEach { it() } }
}

internal fun <ENTITY : Any> Values.Declarations.pairs(): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
    return ValuesScope<ENTITY>().apply { declarations.forEach { it() } }
}
