package org.komapper.core.dsl.builder

import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.SqlDeleteContext
import org.komapper.core.dsl.context.SqlUpdateContext
import org.komapper.core.dsl.declaration.WhereDeclaration
import org.komapper.core.dsl.element.Join
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.scope.OnScope
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
