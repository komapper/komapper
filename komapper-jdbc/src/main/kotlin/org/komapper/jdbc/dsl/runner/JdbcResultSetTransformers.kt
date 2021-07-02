package org.komapper.jdbc.dsl.runner

import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.Columns
import org.komapper.core.dsl.query.ColumnsImpl
import org.komapper.core.dsl.query.Entities
import org.komapper.core.dsl.query.EntitiesImpl
import org.komapper.jdbc.JdbcDialect
import java.sql.ResultSet

internal object JdbcResultSetTransformers {

    fun <T : Any> singleEntity(metamodel: EntityMetamodel<T, *, *>): (JdbcDialect, ResultSet) -> T = { dialect, rs ->
        val mapper = JdbcEntityMapper(dialect, rs)
        val entity = mapper.execute(metamodel, true)
        checkNotNull(entity)
    }

    fun <A : Any, B : Any> pairEntities(metamodels: Pair<EntityMetamodel<A, *, *>, EntityMetamodel<B, *, *>>): (JdbcDialect, ResultSet) -> Pair<A, B?> =
        { dialect, rs ->
            val mapper = JdbcEntityMapper(dialect, rs)
            val first = mapper.execute(metamodels.first, true)
            val second = mapper.execute(metamodels.second)
            checkNotNull(first) to second
        }

    fun <A : Any, B : Any, C : Any> tripleEntities(metamodels: Triple<EntityMetamodel<A, *, *>, EntityMetamodel<B, *, *>, EntityMetamodel<C, *, *>>): (JdbcDialect, ResultSet) -> Triple<A, B?, C?> =
        { dialect, rs ->
            val mapper = JdbcEntityMapper(dialect, rs)
            val first = mapper.execute(metamodels.first, true)
            val second = mapper.execute(metamodels.second)
            val third = mapper.execute(metamodels.third)
            Triple(checkNotNull(first), second, third)
        }

    fun multipleEntities(metamodels: List<EntityMetamodel<*, *, *>>): (JdbcDialect, ResultSet) -> Entities =
        { dialect, rs ->
            val mapper = JdbcEntityMapper(dialect, rs)
            val map = metamodels.associateWith { mapper.execute(it) }
            EntitiesImpl(map)
        }

    fun <A : Any> singleColumn(expression: ColumnExpression<A, *>): (JdbcDialect, ResultSet) -> A? =
        { dialect, rs ->
            val mapper = JdbcPropertyMapper(dialect, rs)
            mapper.execute(expression)
        }

    fun <A : Any, B : Any> pairColumns(expressions: Pair<ColumnExpression<A, *>, ColumnExpression<B, *>>): (JdbcDialect, ResultSet) -> Pair<A?, B?> =
        { dialect, rs ->
            val mapper = JdbcPropertyMapper(dialect, rs)
            val first = mapper.execute(expressions.first)
            val second = mapper.execute(expressions.second)
            first to second
        }

    fun <A : Any, B : Any, C : Any> tripleColumns(expressions: Triple<ColumnExpression<A, *>, ColumnExpression<B, *>, ColumnExpression<C, *>>): (JdbcDialect, ResultSet) -> Triple<A?, B?, C?> =
        { dialect, rs ->
            val mapper = JdbcPropertyMapper(dialect, rs)
            val first = mapper.execute(expressions.first)
            val second = mapper.execute(expressions.second)
            val third = mapper.execute(expressions.third)
            Triple(first, second, third)
        }

    fun multipleColumns(expressions: List<ColumnExpression<*, *>>): (JdbcDialect, ResultSet) -> Columns =
        { dialect, rs ->
            val mapper = JdbcPropertyMapper(dialect, rs)
            val map = expressions.associateWith { mapper.execute(it) }
            ColumnsImpl(map)
        }
}
