package org.komapper.core.query.command

import org.komapper.core.DatabaseConfig
import org.komapper.core.data.Statement
import org.komapper.core.jdbc.Executor
import org.komapper.core.metamodel.ColumnInfo
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.PropertyMetamodel
import kotlin.reflect.cast

internal class SqlSelectCommand<ENTITY, R>(
    private val entityMetamodel: EntityMetamodel<ENTITY>,
    private val config: DatabaseConfig,
    private val statement: Statement,
    private val transformer: (Sequence<ENTITY>) -> R
) : Command<R> {

    private val executor: Executor = Executor(config)

    override fun execute(): R {
        return executor.executeQuery(
            statement,
            { rs ->
                val properties = entityMetamodel.properties()
                val valueMap = mutableMapOf<PropertyMetamodel<*, *>, Any?>()
                for ((index, p) in properties.withIndex()) {
                    val value = config.dialect.getValue(rs, index + 1, p.klass)
                    valueMap[p] = value
                }
                checkNotNull(entityMetamodel.instantiate(valueMap))
            },
            transformer
        )
    }
}

internal class SingleColumnSqlSelectCommand<T : Any, R>(
    private val columnInfo: ColumnInfo<T>,
    private val config: DatabaseConfig,
    private val statement: Statement,
    private val transformer: (Sequence<T>) -> R
) : Command<R> {

    private val executor: Executor = Executor(config)

    override fun execute(): R {
        return executor.executeQuery(
            statement,
            { rs ->
                val value = config.dialect.getValue(rs, 1, columnInfo.klass)
                columnInfo.klass.cast(value)
            },
            transformer
        )
    }
}

internal class PairColumnsSqlSelectCommand<A : Any, B : Any, R>(
    private val pair: Pair<ColumnInfo<A>, ColumnInfo<B>>,
    private val config: DatabaseConfig,
    private val statement: Statement,
    private val transformer: (Sequence<Pair<A, B>>) -> R
) : Command<R> {

    private val executor: Executor = Executor(config)

    override fun execute(): R {
        return executor.executeQuery(
            statement,
            { rs ->
                val (first, second) = pair
                val a = config.dialect.getValue(rs, 1, first.klass)
                val b = config.dialect.getValue(rs, 2, second.klass)
                first.klass.cast(a) to second.klass.cast(b)
            },
            transformer
        )
    }
}
