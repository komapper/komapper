package org.komapper.core.dsl.option

import org.komapper.core.data.JdbcOption

interface QueryOption {
    val queryTimeoutSeconds: Int?
    val suppressLogging: Boolean
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
    val escapeSequence: String?
}

data class EntityDeleteOption(
    override val queryTimeoutSeconds: Int? = null,
    override val suppressLogging: Boolean = false,
    override val ignoreVersion: Boolean = false,
    override val suppressOptimisticLockException: Boolean = false
) : VersionOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class EntityInsertOption(
    override val queryTimeoutSeconds: Int? = null,
    override val suppressLogging: Boolean = false,
) : QueryOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class EntityUpdateOption(
    override val queryTimeoutSeconds: Int? = null,
    override val suppressLogging: Boolean = false,
    override val ignoreVersion: Boolean = false,
    override val suppressOptimisticLockException: Boolean = false
) : VersionOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class EntitySelectOption(
    override val suppressLogging: Boolean = false,
    override val fetchSize: Int? = null,
    override val maxRows: Int? = null,
    override val queryTimeoutSeconds: Int? = null,
    override val allowEmptyWhereClause: Boolean = true,
    override val escapeSequence: String? = null
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
    override val suppressLogging: Boolean = false,
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
    override val suppressLogging: Boolean = false,
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

data class EntityBatchUpdateOption(
    override val suppressLogging: Boolean = false,
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
    override val suppressLogging: Boolean = false,
    override val allowEmptyWhereClause: Boolean = false,
    override val escapeSequence: String? = null
) : WhereOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class SqlInsertOption(
    override val queryTimeoutSeconds: Int? = null,
    override val suppressLogging: Boolean = false,
) : QueryOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class SqlUpdateOption(
    override val queryTimeoutSeconds: Int? = null,
    override val suppressLogging: Boolean = false,
    override val allowEmptyWhereClause: Boolean = false,
    override val escapeSequence: String? = null
) :
    WhereOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class SqlSelectOption(
    override val queryTimeoutSeconds: Int? = null,
    override val suppressLogging: Boolean = false,
    override val fetchSize: Int? = null,
    override val maxRows: Int? = null,
    override val allowEmptyWhereClause: Boolean = true,
    override val escapeSequence: String? = null,
) : SelectOption, WhereOption {
    override fun asJdbcOption() = JdbcOption(
        fetchSize = fetchSize,
        maxRows = maxRows,
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class SqlSetOperationOption(
    override val suppressLogging: Boolean = false,
    override val fetchSize: Int? = null,
    override val maxRows: Int? = null,
    override val queryTimeoutSeconds: Int? = null,
    override val allowEmptyWhereClause: Boolean = true,
    override val escapeSequence: String? = null,
) : SelectOption, WhereOption {
    override fun asJdbcOption() = JdbcOption(
        fetchSize = fetchSize,
        maxRows = maxRows,
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class TemplateSelectOption(
    override val suppressLogging: Boolean = false,
    override val fetchSize: Int? = null,
    override val maxRows: Int? = null,
    override val queryTimeoutSeconds: Int? = null,
    val escapeString: String? = null
) : SelectOption {
    override fun asJdbcOption() = JdbcOption(
        fetchSize = fetchSize,
        maxRows = maxRows,
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class TemplateExecuteOption(
    override val queryTimeoutSeconds: Int? = null,
    override val suppressLogging: Boolean = false,
    val escapeString: String? = null
) : QueryOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class ScriptExecuteOption(
    override val queryTimeoutSeconds: Int? = null,
    override val suppressLogging: Boolean = false,
) : QueryOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class SchemaCreateOption(
    override val queryTimeoutSeconds: Int? = null,
    override val suppressLogging: Boolean = false,
) : QueryOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class SchemaDropOption(
    override val queryTimeoutSeconds: Int? = null,
    override val suppressLogging: Boolean = false,
) : QueryOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}

data class SchemaDropAllOption(
    override val queryTimeoutSeconds: Int? = null,
    override val suppressLogging: Boolean = false,
) : QueryOption {
    override fun asJdbcOption() = JdbcOption(
        queryTimeoutSeconds = queryTimeoutSeconds
    )
}
