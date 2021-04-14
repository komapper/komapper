package org.komapper.core.dsl.option

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

data class EntityDeleteOption(
    override val queryTimeoutSeconds: Int? = null,
    override val ignoreVersion: Boolean = false,
    override val suppressOptimisticLockException: Boolean = false
) : VersionOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class EntityInsertOption(
    override val queryTimeoutSeconds: Int? = null
) : QueryOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
    fun asEntityUpsertOption() = EntityUpsertOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class EntityUpdateOption(
    override val queryTimeoutSeconds: Int? = null,
    override val ignoreVersion: Boolean = false,
    override val suppressOptimisticLockException: Boolean = false
) : VersionOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class EntityUpsertOption(
    override val queryTimeoutSeconds: Int? = null,
    override val ignoreVersion: Boolean = false,
    override val suppressOptimisticLockException: Boolean = false
) : VersionOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class EntitySelectOption(
    override val fetchSize: Int? = null,
    override val maxRows: Int? = null,
    override val queryTimeoutSeconds: Int? = null,
    override val allowEmptyWhereClause: Boolean = true
) : SelectOption, WhereOption {
    override fun asJdbcOption() = JdbcOption(
        fetchSize = fetchSize,
        maxRows = maxRows,
        queryTimeoutSeconds = queryTimeoutSeconds
    )

    fun asSqlSelectOption() = SqlSelectOption(
        fetchSize = fetchSize,
        maxRows = maxRows,
        queryTimeoutSeconds = queryTimeoutSeconds,
        allowEmptyWhereClause = allowEmptyWhereClause
    )
}

data class EntityBatchDeleteOption(
    override val queryTimeoutSeconds: Int? = null,
    override val batchSize: Int? = null,
    override val ignoreVersion: Boolean = false,
    override val suppressOptimisticLockException: Boolean = false
) : VersionOption, BatchOption {
    override fun asJdbcOption() = JdbcOption(
        batchSize = batchSize,
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class EntityBatchInsertOption(
    override val batchSize: Int? = null,
    override val queryTimeoutSeconds: Int? = null,
    override val ignoreVersion: Boolean = false,
    override val suppressOptimisticLockException: Boolean = false,
) : VersionOption, BatchOption {
    override fun asJdbcOption() = JdbcOption(
        batchSize = batchSize,
        queryTimeoutSeconds = queryTimeoutSeconds
    )
    fun asEntityBatchUpsertOption() = EntityBatchUpsertOption(
        batchSize = batchSize,
        queryTimeoutSeconds = queryTimeoutSeconds,
        ignoreVersion = ignoreVersion,
        suppressOptimisticLockException = suppressOptimisticLockException
    )
}

data class EntityBatchUpdateOption(
    override val batchSize: Int? = null,
    override val queryTimeoutSeconds: Int? = null,
    override val ignoreVersion: Boolean = false,
    override val suppressOptimisticLockException: Boolean = false,
) : VersionOption, BatchOption {
    override fun asJdbcOption() = JdbcOption(
        batchSize = batchSize,
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class EntityBatchUpsertOption(
    override val batchSize: Int? = null,
    override val queryTimeoutSeconds: Int? = null,
    override val ignoreVersion: Boolean = false,
    override val suppressOptimisticLockException: Boolean = false,
) : VersionOption, BatchOption {
    override fun asJdbcOption() = JdbcOption(
        batchSize = batchSize,
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class SqlDeleteOption(
    override val queryTimeoutSeconds: Int? = null,
    override val allowEmptyWhereClause: Boolean = false
) : WhereOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class SqlInsertOption(
    override val queryTimeoutSeconds: Int? = null
) : QueryOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class SqlUpdateOption(
    override val queryTimeoutSeconds: Int? = null,
    override val allowEmptyWhereClause: Boolean = false
) :
    WhereOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class SqlSelectOption(
    override val queryTimeoutSeconds: Int? = null,
    override val fetchSize: Int? = null,
    override val maxRows: Int? = null,
    override val allowEmptyWhereClause: Boolean = true
) : SelectOption, WhereOption {
    override fun asJdbcOption() = JdbcOption(
        fetchSize = fetchSize,
        maxRows = maxRows,
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class SqlSetOperationOption(
    override val fetchSize: Int? = null,
    override val maxRows: Int? = null,
    override val queryTimeoutSeconds: Int? = null,
    override val allowEmptyWhereClause: Boolean = true
) : SelectOption, WhereOption {
    override fun asJdbcOption() = JdbcOption(
        fetchSize = fetchSize,
        maxRows = maxRows,
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class TemplateSelectOption(
    override val fetchSize: Int? = null,
    override val maxRows: Int? = null,
    override val queryTimeoutSeconds: Int? = null,
) : SelectOption {
    override fun asJdbcOption() = JdbcOption(
        fetchSize = fetchSize,
        maxRows = maxRows,
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class TemplateExecuteOption(
    override val queryTimeoutSeconds: Int? = null
) : QueryOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class ScriptExecuteOption(
    override val queryTimeoutSeconds: Int? = null
) : QueryOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}
