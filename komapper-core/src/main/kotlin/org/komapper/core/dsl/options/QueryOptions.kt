package org.komapper.core.dsl.options

import org.komapper.core.ExecutionOptions
import org.komapper.core.ExecutionOptionsProvider
import org.komapper.core.ThreadSafe

@ThreadSafe
interface QueryOptions : ExecutionOptionsProvider {
    val queryTimeoutSeconds: Int?
    val suppressLogging: Boolean

    override fun getExecutionOptions(): ExecutionOptions {
        return ExecutionOptions(
            queryTimeoutSeconds = queryTimeoutSeconds,
            suppressLogging = suppressLogging
        )
    }
}

interface VersionOptions : QueryOptions {
    val ignoreVersion: Boolean
    val suppressOptimisticLockException: Boolean
}

interface BatchOptions : QueryOptions {
    val batchSize: Int?

    override fun getExecutionOptions(): ExecutionOptions {
        return super.getExecutionOptions().copy(
            batchSize = batchSize
        )
    }
}

interface InsertOptions : QueryOptions {
    val disableSequenceAssignment: Boolean
}

interface SelectOptions : QueryOptions {
    val fetchSize: Int?
    val maxRows: Int?

    override fun getExecutionOptions(): ExecutionOptions {
        return super.getExecutionOptions().copy(
            fetchSize = fetchSize,
            maxRows = maxRows
        )
    }
}

interface WhereOptions : QueryOptions {
    val allowEmptyWhereClause: Boolean
    val escapeSequence: String?
}

data class EntityDeleteOptions(
    override val ignoreVersion: Boolean,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
    override val suppressOptimisticLockException: Boolean,
) : VersionOptions {

    companion object {
        val default = EntityDeleteOptions(
            ignoreVersion = false,
            queryTimeoutSeconds = null,
            suppressLogging = false,
            suppressOptimisticLockException = false
        )
    }

    fun asEntityBatchDeleteOption(batchSize: Int?): EntityDeleteBatchOptions {
        return EntityDeleteBatchOptions(
            batchSize = batchSize,
            queryTimeoutSeconds = queryTimeoutSeconds,
            suppressLogging = suppressLogging,
            ignoreVersion = ignoreVersion,
            suppressOptimisticLockException = suppressOptimisticLockException
        )
    }

    fun asSqlDeleteOptions(): SqlDeleteOptions {
        return SqlDeleteOptions(
            allowEmptyWhereClause = false,
            escapeSequence = null,
            queryTimeoutSeconds = queryTimeoutSeconds,
            suppressLogging = suppressLogging
        )
    }
}

data class EntityInsertOptions(
    override val disableSequenceAssignment: Boolean,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOptions, InsertOptions {

    companion object {
        val default = EntityInsertOptions(
            disableSequenceAssignment = false,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }

    fun asEntityBatchInsertOption(batchSize: Int?): EntityInsertBatchOptions {
        return EntityInsertBatchOptions(
            batchSize = batchSize,
            disableSequenceAssignment = disableSequenceAssignment,
            queryTimeoutSeconds = queryTimeoutSeconds,
            suppressLogging = suppressLogging,
        )
    }

    fun asSqlInsertOptions(): SqlInsertOptions {
        return SqlInsertOptions(
            queryTimeoutSeconds = queryTimeoutSeconds,
            suppressLogging = suppressLogging,
        )
    }
}

data class EntityUpdateOptions(
    override val ignoreVersion: Boolean,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
    override val suppressOptimisticLockException: Boolean
) : VersionOptions {

    companion object {
        val default = EntityUpdateOptions(
            ignoreVersion = false,
            queryTimeoutSeconds = null,
            suppressLogging = false,
            suppressOptimisticLockException = false
        )
    }

    fun asEntityBatchUpdateOption(batchSize: Int?): EntityUpdateBatchOptions {
        return EntityUpdateBatchOptions(
            batchSize = batchSize,
            ignoreVersion = ignoreVersion,
            queryTimeoutSeconds = queryTimeoutSeconds,
            suppressLogging = suppressLogging,
            suppressOptimisticLockException = suppressOptimisticLockException
        )
    }

    fun asSqlUpdateOptions(): SqlUpdateOptions {
        return SqlUpdateOptions(
            allowEmptyWhereClause = false,
            escapeSequence = null,
            queryTimeoutSeconds = queryTimeoutSeconds,
            suppressLogging = suppressLogging,
        )
    }
}

data class EntitySelectOptions(
    override val allowEmptyWhereClause: Boolean,
    override val escapeSequence: String?,
    override val fetchSize: Int?,
    override val maxRows: Int?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : SelectOptions, WhereOptions {

    companion object {
        val default = EntitySelectOptions(
            allowEmptyWhereClause = true,
            escapeSequence = null,
            fetchSize = null,
            maxRows = null,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }

    fun asSqlSelectOption() = SqlSelectOptions(
        allowEmptyWhereClause = allowEmptyWhereClause,
        escapeSequence = escapeSequence,
        fetchSize = fetchSize,
        maxRows = maxRows,
        queryTimeoutSeconds = queryTimeoutSeconds,
        suppressLogging = suppressLogging,
    )
}

data class EntityDeleteBatchOptions(
    override val batchSize: Int?,
    override val ignoreVersion: Boolean,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
    override val suppressOptimisticLockException: Boolean
) : VersionOptions, BatchOptions

data class EntityInsertBatchOptions(
    override val batchSize: Int?,
    override val disableSequenceAssignment: Boolean,
    override val suppressLogging: Boolean,
    override val queryTimeoutSeconds: Int?,
) : BatchOptions, InsertOptions

data class EntityUpdateBatchOptions(
    override val suppressLogging: Boolean,
    override val batchSize: Int?,
    override val queryTimeoutSeconds: Int?,
    override val ignoreVersion: Boolean,
    override val suppressOptimisticLockException: Boolean,
) : VersionOptions, BatchOptions

data class SqlDeleteOptions(
    override val allowEmptyWhereClause: Boolean,
    override val escapeSequence: String?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : WhereOptions {

    companion object {
        val default = SqlDeleteOptions(
            allowEmptyWhereClause = false,
            escapeSequence = null,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class SqlInsertOptions(
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOptions {

    companion object {
        val default = SqlInsertOptions(
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class SqlUpdateOptions(
    override val allowEmptyWhereClause: Boolean,
    override val escapeSequence: String?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) :
    WhereOptions {

    companion object {
        val default = SqlUpdateOptions(
            allowEmptyWhereClause = false,
            escapeSequence = null,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class SqlSelectOptions(
    override val allowEmptyWhereClause: Boolean,
    override val escapeSequence: String?,
    override val fetchSize: Int?,
    override val maxRows: Int?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : SelectOptions, WhereOptions {

    companion object {
        val default = SqlSelectOptions(
            allowEmptyWhereClause = true,
            escapeSequence = null,
            fetchSize = null,
            maxRows = null,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class SqlSetOperationOptions(
    override val allowEmptyWhereClause: Boolean,
    override val escapeSequence: String?,
    override val fetchSize: Int?,
    override val maxRows: Int?,
    override val suppressLogging: Boolean,
    override val queryTimeoutSeconds: Int?,
) : SelectOptions, WhereOptions {

    companion object {
        val default = SqlSetOperationOptions(
            allowEmptyWhereClause = true,
            escapeSequence = null,
            fetchSize = null,
            maxRows = null,
            suppressLogging = false,
            queryTimeoutSeconds = null
        )
    }
}

data class TemplateSelectOptions(
    val escapeSequence: String?,
    override val fetchSize: Int?,
    override val maxRows: Int?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : SelectOptions {

    companion object {
        val default = TemplateSelectOptions(
            escapeSequence = null,
            fetchSize = null,
            maxRows = null,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class TemplateExecuteOptions(
    val escapeSequence: String?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOptions {

    companion object {
        val default = TemplateExecuteOptions(
            escapeSequence = null,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class ScriptExecuteOptions(
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOptions {

    companion object {
        val default = ScriptExecuteOptions(
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class SchemaCreateOptions(
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOptions {

    companion object {
        val default = SchemaCreateOptions(
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class SchemaDropOptions(
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOptions {

    companion object {
        val default = SchemaDropOptions(
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class SchemaDropAllOptions(
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOptions {

    companion object {
        val default = SchemaDropAllOptions(
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}
