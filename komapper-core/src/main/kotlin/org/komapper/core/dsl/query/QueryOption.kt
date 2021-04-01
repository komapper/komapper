package org.komapper.core.dsl.query

import org.komapper.core.config.JdbcOption

internal interface QueryOption {
    val queryTimeoutSeconds: Int?
    fun asJdbcOption(): JdbcOption
}

internal interface VersionOption : QueryOption {
    val ignoreVersion: Boolean
    val suppressOptimisticLockException: Boolean
}

internal interface BatchOption : QueryOption {
    val batchSize: Int?
}

internal interface SelectOption : QueryOption {
    val fetchSize: Int?
    val maxRows: Int?
}

internal interface WhereOption : QueryOption {
    val allowEmptyWhereClause: Boolean
}

internal interface EntityDeleteOption : VersionOption

internal interface EntityInsertOption : QueryOption

internal interface EntityUpdateOption : VersionOption

internal interface EntitySelectOption : SelectOption, WhereOption

internal interface EntityBatchDeleteOption : EntityDeleteOption, BatchOption

internal interface EntityBatchInsertOption : EntityInsertOption, BatchOption

internal interface EntityBatchUpdateOption : EntityUpdateOption, BatchOption

internal interface SqlDeleteOption : WhereOption

internal interface SqlInsertOption : QueryOption

internal interface SqlUpdateOption : WhereOption

internal interface SqlSelectOption : SelectOption, WhereOption

internal interface TemplateSelectOption : SelectOption

internal interface TemplateUpdateOption : QueryOption

internal interface ScriptExecutionOption : QueryOption

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
}
