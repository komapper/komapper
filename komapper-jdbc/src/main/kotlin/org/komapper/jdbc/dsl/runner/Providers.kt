package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.jdbc.JdbcDialect
import java.sql.ResultSet

internal object Providers {

    fun <T : Any> singleEntity(metamodel: EntityMetamodel<T, *, *>): (JdbcDialect, ResultSet) -> T = { dialect, rs ->
        val mapper = EntityMapper(dialect, rs)
        val entity = mapper.execute(metamodel, true)
        checkNotNull(entity)
    }

    fun <A : Any, B : Any> pairEntities(metamodels: Pair<EntityMetamodel<A, *, *>, EntityMetamodel<B, *, *>>): (JdbcDialect, ResultSet) -> Pair<A, B?> =
        { dialect, rs ->
            val mapper = EntityMapper(dialect, rs)
            val first = mapper.execute(metamodels.first, true)
            val second = mapper.execute(metamodels.second)
            checkNotNull(first) to second
        }

    fun <A : Any> singleColumn(expression: ColumnExpression<A, *>): (JdbcDialect, ResultSet) -> A? =
        { dialect, rs ->
            val mapper = PropertyMapper(dialect, rs)
            mapper.execute(expression)
        }

    fun <A : Any, B : Any> pairColumns(expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>): (JdbcDialect, ResultSet) -> Pair<A?, B?> =
        { dialect, rs ->
            val mapper = PropertyMapper(dialect, rs)
            val first = mapper.execute(expressions.first)
            val second = mapper.execute(expressions.second)
            first to second
        }

    fun <A : Any, B : Any, C : Any> tripleColumns(expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>): (JdbcDialect, ResultSet) -> Triple<A?, B?, C?> =
        { dialect, rs ->
            val mapper = PropertyMapper(dialect, rs)
            val first = mapper.execute(expressions.first)
            val second = mapper.execute(expressions.second)
            val third = mapper.execute(expressions.third)
            Triple(first, second, third)
        }
}
