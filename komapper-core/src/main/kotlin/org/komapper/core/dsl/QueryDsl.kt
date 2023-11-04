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
import org.komapper.core.dsl.context.inlineViewMetamodel
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.DeleteQueryBuilder
import org.komapper.core.dsl.query.DeleteQueryBuilderImpl
import org.komapper.core.dsl.query.InlineViewSelectQuery
import org.komapper.core.dsl.query.InsertQueryBuilder
import org.komapper.core.dsl.query.InsertQueryBuilderImpl
import org.komapper.core.dsl.query.Record
import org.komapper.core.dsl.query.RelationSelectQuery
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
object QueryDsl {

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
    ): SelectQueryBuilder<ENTITY, ID, META> {
        return SelectQueryBuilderImpl(SelectContext(metamodel))
    }

    /**
     * Creates a SELECT query using an inline view.
     *
     * @param subquery the inline vie
     * @return the query
     */
    fun from(
        subquery: SubqueryExpression<*>,
    ): RelationSelectQuery<Record> {
        val metamodel = subquery.context.inlineViewMetamodel
        return InlineViewSelectQuery(SelectContext(metamodel))
    }

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
    ): InsertQueryBuilder<ENTITY, ID, META> {
        return InsertQueryBuilderImpl(EntityInsertContext(metamodel))
    }

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
    ): UpdateQueryBuilder<ENTITY, ID, META> {
        return UpdateQueryBuilderImpl(EntityUpdateContext(metamodel))
    }

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
    ): DeleteQueryBuilder<ENTITY> {
        return DeleteQueryBuilderImpl(EntityDeleteContext(metamodel))
    }

    /**
     * Creates a builder for constructing a SELECT query.
     *
     * @param sql the sql template
     * @return the builder
     */
    fun fromTemplate(@Language("sql") sql: String): TemplateSelectQueryBuilder {
        return TemplateSelectQueryBuilderImpl(TemplateSelectContext(sql))
    }

    /**
     * Creates a query for executing an arbitrary command.
     *
     * @param sql the sql template
     * @return the query
     */
    fun executeTemplate(@Language("sql") sql: String): TemplateExecuteQuery {
        return TemplateExecuteQueryImpl(TemplateExecuteContext(sql))
    }

    /**
     * Creates a query for executing a script.
     *
     * @param sql the script to execute
     */
    fun executeScript(@Language("sql") sql: String): ScriptExecuteQuery {
        return ScriptExecuteQueryImpl(ScriptContext(sql))
    }

    /**
     * Creates a query for creating tables and their associated constraints.
     *
     * @param metamodels the entity metamodels
     */
    fun create(metamodels: List<EntityMetamodel<*, *, *>>): SchemaCreateQuery {
        return SchemaCreateQueryImpl(SchemaContext(metamodels))
    }

    /**
     * Creates a query for creating tables and their associated constraints.
     *
     * @param metamodels the entity metamodels
     */
    fun create(vararg metamodels: EntityMetamodel<*, *, *>): SchemaCreateQuery {
        return create(metamodels.toList())
    }

    /**
     * Creates a query for dropping tables and their associated constraints.
     *
     * @param metamodels the entity metamodels
     */
    fun drop(metamodels: List<EntityMetamodel<*, *, *>>): SchemaDropQuery {
        return SchemaDropQueryImpl(SchemaContext(metamodels))
    }

    /**
     * Creates a query for dropping tables and their associated constraints.
     *
     * @param metamodels the entity metamodels
     */
    fun drop(vararg metamodels: EntityMetamodel<*, *, *>): SchemaDropQuery {
        return drop(metamodels.toList())
    }
}
