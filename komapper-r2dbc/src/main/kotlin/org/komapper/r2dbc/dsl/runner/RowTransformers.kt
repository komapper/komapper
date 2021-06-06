package org.komapper.r2dbc.dsl.runner

import io.r2dbc.spi.Row
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.ColumnsImpl
import org.komapper.core.dsl.query.Entities
import org.komapper.core.dsl.query.EntitiesImpl
import org.komapper.r2dbc.R2dbcDialect

internal object RowTransformers {

    fun <ENTITY : Any> singleEntity(metamodel: EntityMetamodel<ENTITY, *, *>): (R2dbcDialect, Row) -> ENTITY =
        { dialect, row ->
            val mapper = EntityMapper(dialect, row)
            mapper.execute(metamodel, true) as ENTITY
        }

    fun <A : Any, B : Any> pairEntities(metamodels: Pair<EntityMetamodel<A, *, *>, EntityMetamodel<B, *, *>>): (R2dbcDialect, Row) -> Pair<A, B?> =
        { dialect, row ->
            val mapper = EntityMapper(dialect, row)
            val first = mapper.execute(metamodels.first, true)
            val second = mapper.execute(metamodels.second)
            checkNotNull(first) to second
        }

    fun <A : Any, B : Any, C : Any> tripleEntities(
        metamodels: Triple<EntityMetamodel<A, *, *>, EntityMetamodel<B, *, *>, EntityMetamodel<C, *, *>>
    ): (R2dbcDialect, Row) -> Triple<A, B?, C?> =
        { dialect, row ->
            val mapper = EntityMapper(dialect, row)
            val first = mapper.execute(metamodels.first, true)
            val second = mapper.execute(metamodels.second)
            val third = mapper.execute(metamodels.third)
            Triple(checkNotNull(first), second, third)
        }

    fun multipleEntities(metamodels: List<EntityMetamodel<*, *, *>>): (R2dbcDialect, Row) -> Entities =
        { dialect, row ->
            val mapper = EntityMapper(dialect, row)
            val map = metamodels.associateWith { mapper.execute(it) }
            EntitiesImpl(map)
        }

    fun <A : Any> singleColumn(expression: ColumnExpression<A, *>): (R2dbcDialect, Row) -> A? =
        { dialect, row ->
            val mapper = PropertyMapper(dialect, row)
            mapper.execute(expression)
        }

    fun <A : Any, B : Any> pairColumns(expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>): (R2dbcDialect, Row) -> Pair<A?, B?> =
        { dialect, row ->
            val mapper = PropertyMapper(dialect, row)
            val first = mapper.execute(expressions.first)
            val second = mapper.execute(expressions.second)
            first to second
        }

    fun <A : Any, B : Any, C : Any> tripleColumns(expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>): (R2dbcDialect, Row) -> Triple<A?, B?, C?> =
        { dialect, row ->
            val mapper = PropertyMapper(dialect, row)
            val first = mapper.execute(expressions.first)
            val second = mapper.execute(expressions.second)
            val third = mapper.execute(expressions.third)
            Triple(first, second, third)
        }

    fun multipleColumns(expressions: List<ColumnExpression<*, *>>): (R2dbcDialect, Row) -> Columns = { dialect, row ->
        val mapper = PropertyMapper(dialect, row)
        val map = expressions.associateWith { mapper.execute(it) }
        ColumnsImpl(map)
    }
}
