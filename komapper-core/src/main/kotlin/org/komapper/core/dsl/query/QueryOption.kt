package org.komapper.core.dsl.query

import org.komapper.core.data.JdbcOption

interface QueryOption {
    val queryTimeoutSeconds: Int?
    fun asJdbcOption(): JdbcOption
}

interface VersionOption : QueryOption {
    val ignoreVersion: Boolean
    val suppressOptimisticLockException: Boolean
}

interface BatchOption : QueryOption {
    val batchSize: Int?
}

interface SelectOption : QueryOption {
    val fetchSize: Int?
    val maxRows: Int?
}

interface WhereOption : QueryOption {
    val allowEmptyWhereClause: Boolean
}

interface EntityDeleteOption : VersionOption

interface EntityInsertOption : QueryOption

interface EntityUpdateOption : VersionOption

interface EntitySelectOption : SelectOption, WhereOption {
    fun asSqlSelectOption(): SqlSelectOption
}

interface EntityBatchDeleteOption : EntityDeleteOption, BatchOption

interface EntityBatchInsertOption : EntityInsertOption, BatchOption

interface EntityBatchUpdateOption : EntityUpdateOption, BatchOption

interface SqlDeleteOption : WhereOption

interface SqlInsertOption : QueryOption

interface SqlUpdateOption : WhereOption

interface SqlSelectOption : SelectOption, WhereOption

interface SqlSetOperationOption : SelectOption, WhereOption

interface TemplateSelectOption : SelectOption

interface TemplateUpdateOption : QueryOption

interface ScriptExecutionOption : QueryOption

internal data class QueryOptionImpl(
    override val batchSize: Int? = null,
    override val fetchSize: Int? = null,
    override val maxRows: Int? = null,
    override val queryTimeoutSeconds: Int? = null,
    override val allowEmptyWhereClause: Boolean = false,
    override val ignoreVersion: Boolean = false,
    override val suppressOptimisticLockException: Boolean = false
) : EntityBatchDeleteOption,
    EntityBatchInsertOption,
    EntityBatchUpdateOption,
    EntityDeleteOption,
    EntityInsertOption,
    EntitySelectOption,
    EntityUpdateOption,
    SqlDeleteOption,
    SqlInsertOption,
    SqlSelectOption,
    SqlUpdateOption,
    SqlSetOperationOption,
    TemplateSelectOption,
    TemplateUpdateOption,
    ScriptExecutionOption {
    override fun asJdbcOption(): JdbcOption {
        return JdbcOption(
            batchSize = batchSize,
            fetchSize = fetchSize,
            maxRows = maxRows,
            queryTimeoutSeconds = queryTimeoutSeconds
        )
    }
    override fun asSqlSelectOption(): SqlSelectOption {
        return this
    }
}
