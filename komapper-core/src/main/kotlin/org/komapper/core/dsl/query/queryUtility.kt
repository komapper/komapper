package org.komapper.core.dsl.query

import org.komapper.core.OptimisticLockException
import org.komapper.core.Value
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.option.VersionOption
import kotlin.reflect.cast

internal fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
EntityMetamodel<ENTITY, ID, META>.checkIdValueNotNull(entity: ENTITY) {
    this.idProperties().forEach { p ->
        p.getter(entity) ?: error("The id value must not null. name=${p.name}")
    }
}

internal fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
EntityMetamodel<ENTITY, ID, META>.checkIdValueNotNull(entities: List<ENTITY>) {
    entities.mapIndexed { index, entity ->
        this.idProperties().forEach { p ->
            p.getter(entity) ?: error("The id value must not null. index=$index, name=${p.name}")
        }
    }
}

internal fun checkOptimisticLock(
    option: VersionOption,
    count: Int,
    index: Int?
) {
    if (!option.ignoreVersion && !option.suppressOptimisticLockException) {
        if (count != 1) {
            val message = if (index == null) {
                "count=$count"
            } else {
                "index=$index, count=$count"
            }
            throw OptimisticLockException(message)
        }
    }
}

internal fun <EXTERIOR : Any, INTERIOR : Any>
ColumnExpression<EXTERIOR, INTERIOR>.asValue(value: Any?): Value {
    return if (value == null) {
        Value(null, this.interiorClass)
    } else {
        val exterior = this.exteriorClass.cast(value)
        val interior = this.unwrap(exterior)
        Value(interior, this.interiorClass)
    }
}
