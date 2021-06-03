package org.komapper.r2dbc.dsl.runner

import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.query.EntityDeleteBatchQuery
import org.komapper.core.dsl.query.EntityDeleteSingleQuery
import org.komapper.core.dsl.query.EntityInsertBatchQuery
import org.komapper.core.dsl.query.EntityInsertMultipleQuery
import org.komapper.core.dsl.query.EntityInsertSingleQuery
import org.komapper.core.dsl.query.EntitySelectQueryImpl
import org.komapper.core.dsl.query.EntitySetOperationQueryImpl
import org.komapper.core.dsl.query.EntityUpdateBatchQuery
import org.komapper.core.dsl.query.EntityUpdateSingleQuery
import org.komapper.core.dsl.query.EntityUpsertBatchQuery
import org.komapper.core.dsl.query.EntityUpsertMultipleQuery
import org.komapper.core.dsl.query.EntityUpsertSingleQuery
import org.komapper.core.dsl.query.SqlMultipleColumnsQuery
import org.komapper.core.dsl.query.SqlMultipleEntitiesQuery
import org.komapper.core.dsl.query.SqlPairColumnsQuery
import org.komapper.core.dsl.query.SqlPairEntitiesQuery
import org.komapper.core.dsl.query.QueryRunner
import org.komapper.core.dsl.query.QueryVisitor
import org.komapper.core.dsl.query.SchemaCreateQueryImpl
import org.komapper.core.dsl.query.SchemaDropAllQueryImpl
import org.komapper.core.dsl.query.SchemaDropQueryImpl
import org.komapper.core.dsl.query.ScriptExecuteQueryImpl
import org.komapper.core.dsl.query.SqlSingleColumnQuery
import org.komapper.core.dsl.query.SqlDeleteQueryImpl
import org.komapper.core.dsl.query.SqlInsertQueryImpl
import org.komapper.core.dsl.query.SqlSelectQueryImpl
import org.komapper.core.dsl.query.SqlSetOperationQueryImpl
import org.komapper.core.dsl.query.SqlUpdateQueryImpl
import org.komapper.core.dsl.query.TemplateExecuteQueryImpl
import org.komapper.core.dsl.query.TemplateSelectQueryImpl
import org.komapper.core.dsl.query.SqlTripleColumnsQuery
import org.komapper.core.dsl.query.SqlTripleEntitiesQuery

class R2dbcQueryVisitor : QueryVisitor {

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
    visit(query: EntitySelectQueryImpl<ENTITY, ID, META>): R2dbcQueryRunner<List<ENTITY>> {
        return EntitySelectQueryRunner(query.context, query.option)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: EntitySetOperationQueryImpl<ENTITY, ID, META>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: EntityDeleteBatchQuery<ENTITY, ID, META>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: EntityDeleteSingleQuery<ENTITY, ID, META>): QueryRunner {
        return EntityDeleteSingleQueryRunner(query.context, query.entity, query.option)
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: EntityInsertMultipleQuery<ENTITY, ID, META>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: EntityInsertBatchQuery<ENTITY, ID, META>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: EntityInsertSingleQuery<ENTITY, ID, META>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: EntityUpdateBatchQuery<ENTITY, ID, META>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: EntityUpdateSingleQuery<ENTITY, ID, META>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: EntityUpsertBatchQuery<ENTITY, ID, META>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: EntityUpsertMultipleQuery<ENTITY, ID, META>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: EntityUpsertSingleQuery<ENTITY, ID, META>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun visit(query: SchemaCreateQueryImpl): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun visit(query: SchemaDropQueryImpl): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun visit(query: SchemaDropAllQueryImpl): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun visit(query: ScriptExecuteQueryImpl): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: SqlSelectQueryImpl<ENTITY, ID, META>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <T> visit(query: SqlSetOperationQueryImpl<T>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <A, B> visit(query: SqlPairEntitiesQuery<A, B>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <A, B, C> visit(query: SqlTripleEntitiesQuery<A, B, C>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun visit(query: SqlMultipleEntitiesQuery): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <A> visit(query: SqlSingleColumnQuery<A>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <A, B> visit(query: SqlPairColumnsQuery<A, B>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <A, B, C> visit(query: SqlTripleColumnsQuery<A, B, C>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun visit(query: SqlMultipleColumnsQuery): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: SqlDeleteQueryImpl<ENTITY, ID, META>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: SqlInsertQueryImpl<ENTITY, ID, META>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> visit(query: SqlUpdateQueryImpl<ENTITY, ID, META>): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun visit(query: TemplateExecuteQueryImpl): QueryRunner {
        TODO("Not yet implemented")
    }

    override fun <T> visit(query: TemplateSelectQueryImpl<T>): QueryRunner {
        TODO("Not yet implemented")
    }
}
