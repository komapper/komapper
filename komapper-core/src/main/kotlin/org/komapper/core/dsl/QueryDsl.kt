package org.komapper.core.dsl

import org.intellij.lang.annotations.Language
import org.komapper.core.ThreadSafe
import org.komapper.core.dsl.context.EntityDeleteContext
import org.komapper.core.dsl.context.EntityInsertContext
import org.komapper.core.dsl.context.EntityUpdateContext
import org.komapper.core.dsl.context.SchemaContext
import org.komapper.core.dsl.context.ScriptContext
import org.komapper.core.dsl.context.SelectContext
import org.komapper.core.dsl.context.TemplateExecuteContext
import org.komapper.core.dsl.context.TemplateSelectContext
import org.komapper.core.dsl.element.With
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.ScalarExpression
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EmptyMetamodel
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.DeleteOptions
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.options.ScriptOptions
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.options.UpdateOptions
import org.komapper.core.dsl.query.DeleteQueryBuilder
import org.komapper.core.dsl.query.DeleteQueryBuilderImpl
import org.komapper.core.dsl.query.FlowSubquery
import org.komapper.core.dsl.query.InsertQueryBuilder
import org.komapper.core.dsl.query.InsertQueryBuilderImpl
import org.komapper.core.dsl.query.ScalarQuery
import org.komapper.core.dsl.query.SchemaCreateQuery
import org.komapper.core.dsl.query.SchemaCreateQueryImpl
import org.komapper.core.dsl.query.SchemaDropQuery
import org.komapper.core.dsl.query.SchemaDropQueryImpl
import org.komapper.core.dsl.query.ScriptExecuteQuery
import org.komapper.core.dsl.query.ScriptExecuteQueryImpl
import org.komapper.core.dsl.query.SelectQueryBuilder
import org.komapper.core.dsl.query.SelectQueryBuilderImpl
import org.komapper.core.dsl.query.TemplateExecuteQuery
import org.komapper.core.dsl.query.TemplateExecuteQueryImpl
import org.komapper.core.dsl.query.TemplateSelectQueryBuilder
import org.komapper.core.dsl.query.TemplateSelectQueryBuilderImpl
import org.komapper.core.dsl.query.UpdateQueryBuilder
import org.komapper.core.dsl.query.UpdateQueryBuilderImpl

/**
 * The entry point for constructing queries.
 */
@ThreadSafe
interface QueryDsl {
    /**
     * Creates a `WITH` query DSL.
     *
     * @param metamodel the entity metamodel
     * @param subquery the subquery expression
     * @return the `WITH` query DSL
     */
    fun with(
        metamodel: EntityMetamodel<*, *, *>,
        subquery: SubqueryExpression<*>,
    ): WithQueryDsl

    /**
     * Creates a `WITH` query DSL with multiple pairs of entity metamodels and subquery expressions.
     *
     * @param pairs the pairs of entity metamodels and subquery expressions
     * @return the `WITH` query DSL
     */
    fun with(
        vararg pairs: Pair<EntityMetamodel<*, *, *>, SubqueryExpression<*>>,
    ): WithQueryDsl

    /**
     * Creates a `WITH RECURSIVE` query DSL.
     *
     * @param metamodel the entity metamodel
     * @param subquery the subquery expression
     * @return the `WITH RECURSIVE` query DSL
     */
    fun withRecursive(
        metamodel: EntityMetamodel<*, *, *>,
        subquery: SubqueryExpression<*>,
    ): WithQueryDsl

    /**
     * Creates a `WITH RECURSIVE` query DSL with multiple pairs of entity metamodels and subquery expressions.
     *
     * @param pairs the pairs of entity metamodels and subquery expressions
     * @return the `WITH RECURSIVE` query DSL
     */
    fun withRecursive(
        vararg pairs: Pair<EntityMetamodel<*, *, *>, SubqueryExpression<*>>,
    ): WithQueryDsl

    /**
     * Creates a SELECT query builder.
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
    ): SelectQueryBuilder<ENTITY, ID, META>

    /**
     * Creates a SELECT query builder which uses a derived table.
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @param subquery the derived table
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
        subquery: SubqueryExpression<*>,
    ): SelectQueryBuilder<ENTITY, ID, META>

    /**
     * Creates a SELECT query for a single column expression.
     *
     * @param A the type of the column expression
     * @param expression the column expression
     * @return a flow subquery for the column expression
     */
    fun <A : Any> select(expression: ColumnExpression<A, *>): FlowSubquery<A?>

    /**
     * Creates a SELECT query for two column expressions.
     *
     * @param A the type of the first column expression
     * @param B the type of the second column expression
     * @param expression1 the first column expression
     * @param expression2 the second column expression
     * @return a flow subquery for the pair of column expressions
     */
    fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
    ): FlowSubquery<Pair<A?, B?>>

    /**
     * Creates a SELECT query for three column expressions.
     *
     * @param A the type of the first column expression
     * @param B the type of the second column expression
     * @param C the type of the third column expression
     * @param expression1 the first column expression
     * @param expression2 the second column expression
     * @param expression3 the third column expression
     * @return a flow subquery for the triple of column expressions
     */
    fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>,
    ): FlowSubquery<Triple<A?, B?, C?>>

    /**
     * Creates a SELECT query for a scalar expression.
     *
     * @param T the exterior type of the scalar expression
     * @param S the interior type of the scalar expression
     * @param expression the scalar expression
     * @return a scalar query for the scalar expression
     */
    fun <T : Any, S : Any> select(
        expression: ScalarExpression<T, S>,
    ): ScalarQuery<T?, T, S>

    /**
     * Creates a INSERT query builder.
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> insert(
        metamodel: META,
    ): InsertQueryBuilder<ENTITY, ID, META>

    /**
     * Creates a UPDATE query builder.
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> update(
        metamodel: META,
    ): UpdateQueryBuilder<ENTITY, ID, META>

    /**
     * Creates a DELETE query builder.
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> delete(
        metamodel: META,
    ): DeleteQueryBuilder<ENTITY>

    /**
     * Creates a builder for constructing a SELECT query.
     *
     * @param sql the sql template
     * @return the builder
     */
    fun fromTemplate(
        @Language("sql") sql: String,
    ): TemplateSelectQueryBuilder

    /**
     * Creates a query for executing an arbitrary command.
     *
     * @param sql the sql template
     * @return the query
     */
    fun executeTemplate(
        @Language("sql") sql: String,
    ): TemplateExecuteQuery

    /**
     * Creates a query for executing a script.
     *
     * @param sql the script to execute
     */
    fun executeScript(
        @Language("sql") sql: String,
    ): ScriptExecuteQuery

    /**
     * Creates a query for creating tables and their associated constraints.
     *
     * @param metamodels the entity metamodels
     */
    fun create(metamodels: List<EntityMetamodel<*, *, *>>): SchemaCreateQuery

    /**
     * Creates a query for creating tables and their associated constraints.
     *
     * @param metamodels the entity metamodels
     */
    fun create(vararg metamodels: EntityMetamodel<*, *, *>): SchemaCreateQuery

    /**
     * Creates a query for dropping tables and their associated constraints.
     *
     * @param metamodels the entity metamodels
     */
    fun drop(metamodels: List<EntityMetamodel<*, *, *>>): SchemaDropQuery

    /**
     * Creates a query for dropping tables and their associated constraints.
     *
     * @param metamodels the entity metamodels
     */
    fun drop(vararg metamodels: EntityMetamodel<*, *, *>): SchemaDropQuery

    /**
     * The companion object for the `QueryDsl` interface, delegating to a new instance of `QueryDsl`.
     */
    companion object : QueryDsl by QueryDsl()
}

internal class QueryDslImpl(
    private val deleteOptions: DeleteOptions,
    private val insertOptions: InsertOptions,
    private val schemaOptions: SchemaOptions,
    private val scriptOptions: ScriptOptions,
    private val selectOptions: SelectOptions,
    private val templateExecuteOptions: TemplateExecuteOptions,
    private val templateSelectOptions: TemplateSelectOptions,
    private val updateOptions: UpdateOptions,
) : QueryDsl {
    override fun with(
        metamodel: EntityMetamodel<*, *, *>,
        subquery: SubqueryExpression<*>,
    ): WithQueryDsl {
        val with = With(false, listOf(metamodel to subquery))
        return WithQueryDslImpl(with, selectOptions)
    }

    override fun with(
        vararg pairs: Pair<EntityMetamodel<*, *, *>, SubqueryExpression<*>>,
    ): WithQueryDsl {
        val with = With(false, pairs.toList())
        return WithQueryDslImpl(with, selectOptions)
    }

    override fun withRecursive(
        metamodel: EntityMetamodel<*, *, *>,
        subquery: SubqueryExpression<*>,
    ): WithQueryDsl {
        val with = With(true, listOf(metamodel to subquery))
        return WithQueryDslImpl(with, selectOptions)
    }

    override fun withRecursive(
        vararg pairs: Pair<EntityMetamodel<*, *, *>, SubqueryExpression<*>>,
    ): WithQueryDsl {
        val with = With(true, pairs.toList())
        return WithQueryDslImpl(with, selectOptions)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
    ): SelectQueryBuilder<ENTITY, ID, META> {
        return SelectQueryBuilderImpl(SelectContext(metamodel, options = selectOptions))
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
        subquery: SubqueryExpression<*>,
    ): SelectQueryBuilder<ENTITY, ID, META> {
        return SelectQueryBuilderImpl(SelectContext(metamodel, subquery, options = selectOptions))
    }

    override fun <A : Any> select(expression: ColumnExpression<A, *>): FlowSubquery<A?> {
        return from(EmptyMetamodel).select(expression)
    }

    override fun <A : Any, B : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
    ): FlowSubquery<Pair<A?, B?>> {
        return from(EmptyMetamodel).select(expression1, expression2)
    }

    override fun <A : Any, B : Any, C : Any> select(
        expression1: ColumnExpression<A, *>,
        expression2: ColumnExpression<B, *>,
        expression3: ColumnExpression<C, *>,
    ): FlowSubquery<Triple<A?, B?, C?>> {
        return from(EmptyMetamodel).select(expression1, expression2, expression3)
    }

    override fun <T : Any, S : Any> select(expression: ScalarExpression<T, S>): ScalarQuery<T?, T, S> {
        return from(EmptyMetamodel).select(expression)
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> insert(
        metamodel: META,
    ): InsertQueryBuilder<ENTITY, ID, META> {
        return InsertQueryBuilderImpl(EntityInsertContext(metamodel, options = insertOptions))
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> update(
        metamodel: META,
    ): UpdateQueryBuilder<ENTITY, ID, META> {
        return UpdateQueryBuilderImpl(EntityUpdateContext(metamodel, options = updateOptions))
    }

    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> delete(
        metamodel: META,
    ): DeleteQueryBuilder<ENTITY> {
        return DeleteQueryBuilderImpl(EntityDeleteContext(metamodel, options = deleteOptions))
    }

    override fun fromTemplate(
        @Language("sql") sql: String,
    ): TemplateSelectQueryBuilder {
        return TemplateSelectQueryBuilderImpl(TemplateSelectContext(sql, options = templateSelectOptions))
    }

    override fun executeTemplate(
        @Language("sql") sql: String,
    ): TemplateExecuteQuery {
        return TemplateExecuteQueryImpl(TemplateExecuteContext(sql, options = templateExecuteOptions))
    }

    override fun executeScript(
        @Language("sql") sql: String,
    ): ScriptExecuteQuery {
        return ScriptExecuteQueryImpl(ScriptContext(sql, options = scriptOptions))
    }

    override fun create(metamodels: List<EntityMetamodel<*, *, *>>): SchemaCreateQuery {
        return SchemaCreateQueryImpl(SchemaContext(metamodels, options = schemaOptions))
    }

    override fun create(vararg metamodels: EntityMetamodel<*, *, *>): SchemaCreateQuery {
        return create(metamodels.toList())
    }

    override fun drop(metamodels: List<EntityMetamodel<*, *, *>>): SchemaDropQuery {
        return SchemaDropQueryImpl(SchemaContext(metamodels, options = schemaOptions))
    }

    override fun drop(vararg metamodels: EntityMetamodel<*, *, *>): SchemaDropQuery {
        return drop(metamodels.toList())
    }
}

/**
 * Interface for creating a `WITH` query DSL.
 */
interface WithQueryDsl {
    /**
     * Creates a SELECT query builder.
     *
     * @param ENTITY the entity type
     * @param ID the entity id type
     * @param META the entity metamodel type
     * @param metamodel the entity metamodel
     * @return the query builder
     */
    fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
    ): SelectQueryBuilder<ENTITY, ID, META>
}

internal data class WithQueryDslImpl(private val with: With, private val options: SelectOptions) : WithQueryDsl {
    override fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> from(
        metamodel: META,
    ): SelectQueryBuilder<ENTITY, ID, META> {
        return SelectQueryBuilderImpl(SelectContext(metamodel, with = with, options = options))
    }
}

/**
 * Creates a new instance of [QueryDsl].
 *
 * @param deleteOptions the options for DELETE queries
 * @param insertOptions the options for INSERT queries
 * @param schemaOptions the options for schema operations
 * @param scriptOptions the options for script operations
 * @param selectOptions the options for SELECT queries
 * @param templateExecuteOptions the options for template execute operations
 * @param templateSelectOptions the options for template select operations
 * @param updateOptions the options for UPDATE queries
 */
fun QueryDsl(
    deleteOptions: DeleteOptions = DeleteOptions.DEFAULT,
    insertOptions: InsertOptions = InsertOptions.DEFAULT,
    schemaOptions: SchemaOptions = SchemaOptions.DEFAULT,
    scriptOptions: ScriptOptions = ScriptOptions.DEFAULT,
    selectOptions: SelectOptions = SelectOptions.DEFAULT,
    templateExecuteOptions: TemplateExecuteOptions = TemplateExecuteOptions.DEFAULT,
    templateSelectOptions: TemplateSelectOptions = TemplateSelectOptions.DEFAULT,
    updateOptions: UpdateOptions = UpdateOptions.DEFAULT,
): QueryDsl {
    return QueryDslImpl(
        deleteOptions,
        insertOptions,
        schemaOptions,
        scriptOptions,
        selectOptions,
        templateExecuteOptions,
        templateSelectOptions,
        updateOptions,
    )
}
