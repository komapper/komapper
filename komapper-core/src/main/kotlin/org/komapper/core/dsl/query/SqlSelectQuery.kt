package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.config.Dialect
import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SqlSelectStatementBuilder
import org.komapper.core.dsl.context.SqlSelectContext
import org.komapper.core.dsl.context.SqlSetOperationComponent
import org.komapper.core.dsl.context.SqlSetOperationContext
import org.komapper.core.dsl.context.SqlSetOperationKind
import org.komapper.core.dsl.expr.PropertyExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.scope.HavingDeclaration
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.OnDeclaration
import org.komapper.core.dsl.scope.SqlSelectOptionDeclaration
import org.komapper.core.dsl.scope.SqlSelectOptionScope
import org.komapper.core.dsl.scope.WhereDeclaration
import org.komapper.core.jdbc.JdbcExecutor
import java.sql.ResultSet

interface SqlSelectQuery<ENTITY : Any> : SqlSetOperandQuery<ENTITY> {

    fun <OTHER_ENTITY : Any> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQuery<ENTITY>

    fun <OTHER_ENTITY : Any> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQuery<ENTITY>

    fun where(declaration: WhereDeclaration): SqlSelectQuery<ENTITY>
    fun groupBy(vararg properties: PropertyExpression<*>): SqlSelectQuery<ENTITY>
    fun having(declaration: HavingDeclaration): SqlSelectQuery<ENTITY>
    fun orderBy(vararg properties: PropertyExpression<*>): SqlSelectQuery<ENTITY>
    fun offset(value: Int): SqlSelectQuery<ENTITY>
    fun limit(value: Int): SqlSelectQuery<ENTITY>
    fun forUpdate(): SqlSelectQuery<ENTITY>
    fun option(declaration: SqlSelectOptionDeclaration): SqlSelectQuery<ENTITY>

    fun <A : Any> select(
        e: EntityMetamodel<A>
    ): SqlSetOperandQuery<A?>

    fun <A : Any, B : Any> select(
        e1: EntityMetamodel<A>,
        e2: EntityMetamodel<B>
    ): SqlSetOperandQuery<Pair<A?, B?>>

    fun <A : Any, B : Any, C : Any> select(
        e1: EntityMetamodel<A>,
        e2: EntityMetamodel<B>,
        e3: EntityMetamodel<C>
    ): SqlSetOperandQuery<Triple<A?, B?, C?>>

    fun select(
        vararg entityMetamodels: EntityMetamodel<*>,
    ): SqlSetOperandQuery<EntityRecord>

    fun <A : Any> select(
        p: PropertyExpression<A>
    ): SqlSetOperandQuery<A?>

    fun <A : Any, B : Any> select(
        p1: PropertyExpression<A>,
        p2: PropertyExpression<B>
    ): SqlSetOperandQuery<Pair<A?, B?>>

    fun <A : Any, B : Any, C : Any> select(
        p1: PropertyExpression<A>,
        p2: PropertyExpression<B>,
        p3: PropertyExpression<C>
    ): SqlSetOperandQuery<Triple<A?, B?, C?>>

    fun select(
        vararg propertyExpressions: PropertyExpression<*>,
    ): SqlSetOperandQuery<PropertyRecord>
}

internal data class SqlSelectQueryImpl<ENTITY : Any>(
    private val context: SqlSelectContext<ENTITY>,
    private val option: SqlSelectOption = QueryOptionImpl(allowEmptyWhereClause = true)
) :
    SqlSelectQuery<ENTITY> {

    companion object Message {
        fun entityMetamodelNotFound(parameterName: String): String {
            return "The '$parameterName' metamodel is not found. Bind it to this query in advance using the from or join clause."
        }
    }

    private val support: SelectQuerySupport<ENTITY, SqlSelectContext<ENTITY>> = SelectQuerySupport(context)
    override val setOperationComponent: SqlSetOperationComponent<ENTITY> = SqlSetOperationComponent.Leaf(context)

    override fun <OTHER_ENTITY : Any> innerJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.innerJoin(entityMetamodel, on)
        return copy(context = newContext)
    }

    override fun <OTHER_ENTITY : Any> leftJoin(
        entityMetamodel: EntityMetamodel<OTHER_ENTITY>,
        on: OnDeclaration<OTHER_ENTITY>
    ): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.leftJoin(entityMetamodel, on)
        return copy(context = newContext)
    }

    override fun where(declaration: WhereDeclaration): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.where(declaration)
        return copy(context = newContext)
    }

    override fun groupBy(vararg properties: PropertyExpression<*>): SqlSelectQueryImpl<ENTITY> {
        val newContext = context.copy(groupBy = properties.toList())
        return copy(context = newContext)
    }

    override fun having(declaration: HavingDeclaration): SqlSelectQueryImpl<ENTITY> {
        val scope = HavingScope()
        declaration(scope)
        val newContext = context.addHaving(scope.toList())
        return copy(context = newContext)
    }

    override fun orderBy(vararg properties: PropertyExpression<*>): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.orderBy(*properties)
        return copy(context = newContext)
    }

    override fun offset(value: Int): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.offset(value)
        return copy(context = newContext)
    }

    override fun limit(value: Int): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.limit(value)
        return copy(context = newContext)
    }

    override fun forUpdate(): SqlSelectQueryImpl<ENTITY> {
        val newContext = support.forUpdate()
        return copy(context = newContext)
    }

    override fun option(declaration: SqlSelectOptionDeclaration): SqlSelectQueryImpl<ENTITY> {
        val scope = SqlSelectOptionScope(option)
        declaration(scope)
        return copy(option = scope.asOption())
    }

    override fun except(other: SqlSetOperandQuery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.EXCEPT, other)
    }

    override fun intersect(other: SqlSetOperandQuery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.INTERSECT, other)
    }

    override fun union(other: SqlSetOperandQuery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.UNION, other)
    }

    override fun unionAll(other: SqlSetOperandQuery<ENTITY>): SqlSetOperationQuery<ENTITY> {
        return setOperation(SqlSetOperationKind.UNION_ALL, other)
    }

    private fun setOperation(
        kind: SqlSetOperationKind,
        other: SqlSetOperandQuery<ENTITY>
    ): SqlSetOperationQuery<ENTITY> {
        val component = SqlSetOperationComponent.Composite(kind, setOperationComponent, other.setOperationComponent)
        val setOperatorContext = SqlSetOperationContext(component)
        return SetOperationQueryImpl(setOperatorContext) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
            val entity = m.execute(context.entityMetamodel)
            checkNotNull(entity)
        }
    }

    override fun <A : Any> select(
        e: EntityMetamodel<A>,
    ): SqlSetOperandQuery<A?> {
        val entityExpressions = context.getEntityExpressions()
        if (e !in entityExpressions) error(entityMetamodelNotFound("e"))
        val newContext = context.setEntity(e)
        return Transformable(newContext, option) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
            m.execute(e)
        }
    }

    override fun <A : Any, B : Any> select(
        e1: EntityMetamodel<A>,
        e2: EntityMetamodel<B>
    ): SqlSetOperandQuery<Pair<A?, B?>> {
        val entityExpressions = context.getEntityExpressions()
        if (e1 !in entityExpressions) error(entityMetamodelNotFound("e1"))
        if (e2 !in entityExpressions) error(entityMetamodelNotFound("e2"))
        val newContext = context.setEntities(listOf(e1, e2))
        return Transformable(newContext, option) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
            m.execute(e1) to m.execute(e2)
        }
    }

    override fun <A : Any, B : Any, C : Any> select(
        e1: EntityMetamodel<A>,
        e2: EntityMetamodel<B>,
        e3: EntityMetamodel<C>
    ): SqlSetOperandQuery<Triple<A?, B?, C?>> {
        val entityExpressions = context.getEntityExpressions()
        if (e1 !in entityExpressions) error(entityMetamodelNotFound("e1"))
        if (e2 !in entityExpressions) error(entityMetamodelNotFound("e2"))
        if (e3 !in entityExpressions) error(entityMetamodelNotFound("e3"))
        val newContext = context.setEntities(listOf(e1, e2, e3))
        return Transformable(newContext, option) { dialect, rs ->
            val m = EntityMapper(dialect, rs)
            Triple(m.execute(e1), m.execute(e2), m.execute(e3))
        }
    }

    override fun select(vararg entityMetamodels: EntityMetamodel<*>): SqlSetOperandQuery<EntityRecord> {
        val list = entityMetamodels.toList()
        val newContext = context.setEntities(list)
        return Transformable(newContext, option) { dialect, rs: ResultSet ->
            val mapper = EntityMapper(dialect, rs)
            val map = list.associateWith { mapper.execute(it) }
            EntityRecordImpl(map)
        }
    }

    override fun <A : Any> select(p: PropertyExpression<A>): SqlSetOperandQuery<A?> {
        val newContext = context.setProperty(p)
        return Transformable(newContext, option) { dialect, rs ->
            val m = PropertyMapper(dialect, rs)
            m.execute(p)
        }
    }

    override fun <A : Any, B : Any> select(
        p1: PropertyExpression<A>,
        p2: PropertyExpression<B>
    ): SqlSetOperandQuery<Pair<A?, B?>> {
        val newContext = context.setProperties(listOf(p1, p2))
        return Transformable(newContext, option) { dialect, rs ->
            val m = PropertyMapper(dialect, rs)
            m.execute(p1) to m.execute(p2)
        }
    }

    override fun <A : Any, B : Any, C : Any> select(
        p1: PropertyExpression<A>,
        p2: PropertyExpression<B>,
        p3: PropertyExpression<C>
    ): SqlSetOperandQuery<Triple<A?, B?, C?>> {
        val newContext = context.setProperties(listOf(p1, p2, p3))
        return Transformable(newContext, option) { dialect, rs: ResultSet ->
            val m = PropertyMapper(dialect, rs)
            Triple(m.execute(p1), m.execute(p2), m.execute(p3))
        }
    }

    override fun select(vararg propertyExpressions: PropertyExpression<*>): SqlSetOperandQuery<PropertyRecord> {
        val list = propertyExpressions.toList()
        val newContext = context.setProperties(list)
        return Transformable(newContext, option) { dialect, rs: ResultSet ->
            val mapper = PropertyMapper(dialect, rs)
            val map = list.associateWith { mapper.execute(it) }
            PropertyRecordImpl(map)
        }
    }

    override fun run(config: DatabaseConfig): List<ENTITY> {
        val terminal = createTerminal(context) { it.toList() }
        return terminal.run(config)
    }

    override fun dryRun(dialect: Dialect): Statement {
        val terminal = createTerminal(context) { it.toList() }
        return terminal.dryRun(dialect)
    }

    override fun first(): Query<ENTITY> {
        val newContext = support.limit(1)
        return createTerminal(newContext) { it.first() }
    }

    override fun firstOrNull(): Query<ENTITY?> {
        val newContext = support.limit(1)
        return createTerminal(newContext) { it.firstOrNull() }
    }

    override fun <R> transform(transformer: (Sequence<ENTITY>) -> R): Query<R> {
        return createTerminal(context, transformer)
    }

    private fun <R> createTerminal(c: SqlSelectContext<ENTITY>, transformer: (Sequence<ENTITY>) -> R): Query<R> {
        val provider: (Dialect, ResultSet) -> ENTITY = { dialect, rs ->
            val mapper = EntityMapper(dialect, rs)
            val entity = mapper.execute(c.entityMetamodel, true)
            checkNotNull(entity)
        }
        return Terminal(c, option, provider, transformer)
    }

    private data class Transformable<T>(
        private val context: SqlSelectContext<*>,
        private val option: SqlSelectOption,
        private val provider: (Dialect, ResultSet) -> T
    ) : SqlSetOperandQuery<T> {

        override val setOperationComponent = SqlSetOperationComponent.Leaf<T>(context)

        override fun except(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T> {
            return setOperation(SqlSetOperationKind.EXCEPT, other)
        }

        override fun intersect(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T> {
            return setOperation(SqlSetOperationKind.INTERSECT, other)
        }

        override fun union(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T> {
            return setOperation(SqlSetOperationKind.UNION, other)
        }

        override fun unionAll(other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T> {
            return setOperation(SqlSetOperationKind.UNION_ALL, other)
        }

        private fun setOperation(kind: SqlSetOperationKind, other: SqlSetOperandQuery<T>): SqlSetOperationQuery<T> {
            val component = SqlSetOperationComponent.Composite(kind, setOperationComponent, other.setOperationComponent)
            val context = SqlSetOperationContext(component)
            return SetOperationQueryImpl(context, provider = provider)
        }

        override fun run(config: DatabaseConfig): List<T> {
            val terminal = createTerminal { it.toList() }
            return terminal.run(config)
        }

        override fun dryRun(dialect: Dialect): Statement {
            val terminal = createTerminal { it.toList() }
            return terminal.dryRun(dialect)
        }

        override fun first(): Query<T> {
            return createTerminal { it.first() }
        }

        override fun firstOrNull(): Query<T?> {
            return createTerminal { it.firstOrNull() }
        }

        override fun <R> transform(transformer: (Sequence<T>) -> R): Query<R> {
            return createTerminal(transformer)
        }

        private fun <R> createTerminal(transformer: (Sequence<T>) -> R): Query<R> {
            return Terminal(context, option, provider, transformer)
        }
    }

    private class Terminal<T, R>(
        private val context: SqlSelectContext<*>,
        private val option: SqlSelectOption,
        private val provider: (Dialect, ResultSet) -> T,
        val transformer: (Sequence<T>) -> R
    ) : Query<R> {

        override fun run(config: DatabaseConfig): R {
            if (!option.allowEmptyWhereClause && context.where.isEmpty()) {
                error("Empty where clause is not allowed.")
            }
            val statement = buildStatement(config.dialect)
            val executor = JdbcExecutor(config, option.asJdbcOption())
            return executor.executeQuery(statement, provider, transformer)
        }

        override fun dryRun(dialect: Dialect): Statement {
            return buildStatement(dialect)
        }

        private fun buildStatement(dialect: Dialect): Statement {
            val builder = SqlSelectStatementBuilder(dialect, context)
            return builder.build()
        }
    }
}
