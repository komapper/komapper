package org.komapper.core.dsl.option

import org.komapper.core.JdbcOption
import org.komapper.core.JdbcOptionProvider
import org.komapper.core.ThreadSafe

@ThreadSafe
interface QueryOption : JdbcOptionProvider {
    val queryTimeoutSeconds: Int?
    val suppressLogging: Boolean

    override fun getJdbcOption(): JdbcOption {
        return JdbcOption(
            queryTimeoutSeconds = queryTimeoutSeconds,
            suppressLogging = suppressLogging
        )
    }
}

interface VersionOption : QueryOption {
    val ignoreVersion: Boolean
    val suppressOptimisticLockException: Boolean
}

interface BatchOption : QueryOption {
    val batchSize: Int?

    override fun getJdbcOption(): JdbcOption {
        return super.getJdbcOption().copy(
            batchSize = batchSize
        )
    }
}

interface InsertOption : QueryOption {
    val disableSequenceAssignment: Boolean
}

interface SelectOption : QueryOption {
    val fetchSize: Int?
    val maxRows: Int?

    override fun getJdbcOption(): JdbcOption {
        return super.getJdbcOption().copy(
            fetchSize = fetchSize,
            maxRows = maxRows
        )
    }
}

interface WhereOption : QueryOption {
    val allowEmptyWhereClause: Boolean
    val escapeSequence: String?
}

data class EntityDeleteOption(
    override val ignoreVersion: Boolean,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
    override val suppressOptimisticLockException: Boolean
) : VersionOption {

    companion object {
        val default = EntityDeleteOption(
            ignoreVersion = false,
            queryTimeoutSeconds = null,
            suppressLogging = false,
            suppressOptimisticLockException = false
        )
    }

    fun asEntityBatchDeleteOption(batchSize: Int?): EntityDeleteBatchOption {
        return EntityDeleteBatchOption(
            batchSize = batchSize,
            queryTimeoutSeconds = queryTimeoutSeconds,
            suppressLogging = suppressLogging,
            ignoreVersion = ignoreVersion,
            suppressOptimisticLockException = suppressOptimisticLockException
        )
    }
}

data class EntityInsertOption(
    override val disableSequenceAssignment: Boolean,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOption, InsertOption {

    companion object {
        val default = EntityInsertOption(
            disableSequenceAssignment = false,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }

    fun asEntityBatchInsertOption(batchSize: Int?): EntityInsertBatchOption {
        return EntityInsertBatchOption(
            batchSize = batchSize,
            disableSequenceAssignment = disableSequenceAssignment,
            queryTimeoutSeconds = queryTimeoutSeconds,
            suppressLogging = suppressLogging,
        )
    }
}

data class EntityUpdateOption(
    override val ignoreVersion: Boolean,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
    override val suppressOptimisticLockException: Boolean
) : VersionOption {

    companion object {
        val default = EntityUpdateOption(
            ignoreVersion = false,
            queryTimeoutSeconds = null,
            suppressLogging = false,
            suppressOptimisticLockException = false
        )
    }

    fun asEntityBatchUpdateOption(batchSize: Int?): EntityUpdateBatchOption {
        return EntityUpdateBatchOption(
            batchSize = batchSize,
            ignoreVersion = ignoreVersion,
            queryTimeoutSeconds = queryTimeoutSeconds,
            suppressLogging = suppressLogging,
            suppressOptimisticLockException = suppressOptimisticLockException
        )
    }
}

data class EntitySelectOption(
    override val allowEmptyWhereClause: Boolean,
    override val escapeSequence: String?,
    override val fetchSize: Int?,
    override val maxRows: Int?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : SelectOption, WhereOption {

    companion object {
        val default = EntitySelectOption(
            allowEmptyWhereClause = true,
            escapeSequence = null,
            fetchSize = null,
            maxRows = null,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }

    fun asSqlSelectOption() = SqlSelectOption(
        allowEmptyWhereClause = allowEmptyWhereClause,
        escapeSequence = escapeSequence,
        fetchSize = fetchSize,
        maxRows = maxRows,
        queryTimeoutSeconds = queryTimeoutSeconds,
        suppressLogging = suppressLogging,
    )
}

data class EntityDeleteBatchOption(
    override val batchSize: Int?,
    override val ignoreVersion: Boolean,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
    override val suppressOptimisticLockException: Boolean
) : VersionOption, BatchOption

data class EntityInsertBatchOption(
    override val batchSize: Int?,
    override val disableSequenceAssignment: Boolean,
    override val suppressLogging: Boolean,
    override val queryTimeoutSeconds: Int?,
) : BatchOption, InsertOption

data class EntityUpdateBatchOption(
    override val suppressLogging: Boolean,
    override val batchSize: Int?,
    override val queryTimeoutSeconds: Int?,
    override val ignoreVersion: Boolean,
    override val suppressOptimisticLockException: Boolean,
) : VersionOption, BatchOption

data class SqlDeleteOption(
    override val allowEmptyWhereClause: Boolean,
    override val escapeSequence: String?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : WhereOption {

    companion object {
        val default = SqlDeleteOption(
            allowEmptyWhereClause = false,
            escapeSequence = null,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class SqlInsertOption(
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOption {

    companion object {
        val default = SqlInsertOption(
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class SqlUpdateOption(
    override val allowEmptyWhereClause: Boolean,
    override val escapeSequence: String?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) :
    WhereOption {

    companion object {
        val default = SqlUpdateOption(
            allowEmptyWhereClause = false,
            escapeSequence = null,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class SqlSelectOption(
    override val allowEmptyWhereClause: Boolean,
    override val escapeSequence: String?,
    override val fetchSize: Int?,
    override val maxRows: Int?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : SelectOption, WhereOption {

    companion object {
        val default = SqlSelectOption(
            allowEmptyWhereClause = true,
            escapeSequence = null,
            fetchSize = null,
            maxRows = null,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class SqlSetOperationOption(
    override val allowEmptyWhereClause: Boolean,
    override val escapeSequence: String?,
    override val fetchSize: Int?,
    override val maxRows: Int?,
    override val suppressLogging: Boolean,
    override val queryTimeoutSeconds: Int?,
) : SelectOption, WhereOption {

    companion object {
        val default = SqlSetOperationOption(
            allowEmptyWhereClause = true,
            escapeSequence = null,
            fetchSize = null,
            maxRows = null,
            suppressLogging = false,
            queryTimeoutSeconds = null
        )
    }
}

data class TemplateSelectOption(
    val escapeSequence: String?,
    override val fetchSize: Int?,
    override val maxRows: Int?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : SelectOption {

    companion object {
        val default = TemplateSelectOption(
            escapeSequence = null,
            fetchSize = null,
            maxRows = null,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class TemplateExecuteOption(
    val escapeSequence: String?,
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOption {

    companion object {
        val default = TemplateExecuteOption(
            escapeSequence = null,
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class ScriptExecuteOption(
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOption {

    companion object {
        val default = ScriptExecuteOption(
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class SchemaCreateOption(
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOption {

    companion object {
        val default = SchemaCreateOption(
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class SchemaDropOption(
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOption {

    companion object {
        val default = SchemaDropOption(
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}

data class SchemaDropAllOption(
    override val queryTimeoutSeconds: Int?,
    override val suppressLogging: Boolean,
) : QueryOption {

    companion object {
        val default = SchemaDropAllOption(
            queryTimeoutSeconds = null,
            suppressLogging = false
        )
    }
}
