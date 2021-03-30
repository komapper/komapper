package org.komapper.core.config

internal interface Options {
    fun asJdbcConfig(): JdbcConfig
}

internal interface EntityDeleteOptions : Options {
    val queryTimeoutSeconds: Int?
    val ignoreVersion: Boolean
    val suppressOptimisticLockException: Boolean
}

internal interface EntityInsertOptions : Options {
    val queryTimeoutSeconds: Int?
}

internal interface EntitySelectOptions : Options {
    val fetchSize: Int?
    val maxRows: Int?
    val queryTimeoutSeconds: Int?
    val allowEmptyWhereClause: Boolean
}

internal interface EntityUpdateOptions : Options {
    val queryTimeoutSeconds: Int?
    val ignoreVersion: Boolean
    val suppressOptimisticLockException: Boolean
}

internal interface SqlDeleteOptions : Options {
    val queryTimeoutSeconds: Int?
    val allowEmptyWhereClause: Boolean
}

internal interface SqlInsertOptions : Options {
    val queryTimeoutSeconds: Int?
}

internal interface SqlSelectOptions : Options {
    val fetchSize: Int?
    val maxRows: Int?
    val queryTimeoutSeconds: Int?
    val allowEmptyWhereClause: Boolean
}

internal interface SqlUpdateOptions : Options {
    val queryTimeoutSeconds: Int?
    val allowEmptyWhereClause: Boolean
}

internal interface TemplateSelectOptions : Options {
    val fetchSize: Int?
    val maxRows: Int?
    val queryTimeoutSeconds: Int?
}

internal interface TemplateUpdateOptions : Options {
    val queryTimeoutSeconds: Int?
}

internal interface ScriptExecutionOptions : Options {
    val queryTimeoutSeconds: Int?
}

internal data class OptionsImpl(
    val batchSize: Int? = null,
    override val fetchSize: Int? = null,
    override val maxRows: Int? = null,
    override val queryTimeoutSeconds: Int? = null,
    override val allowEmptyWhereClause: Boolean = false,
    override val ignoreVersion: Boolean = false,
    override val suppressOptimisticLockException: Boolean = false
) : EntityDeleteOptions,
    EntityInsertOptions,
    EntitySelectOptions,
    EntityUpdateOptions,
    SqlDeleteOptions,
    SqlInsertOptions,
    SqlSelectOptions,
    SqlUpdateOptions,
    TemplateSelectOptions,
    TemplateUpdateOptions,
    ScriptExecutionOptions {
    override fun asJdbcConfig(): JdbcConfig {
        return JdbcConfig(
            batchSize = batchSize,
            fetchSize = fetchSize,
            maxRows = maxRows,
            queryTimeoutSeconds = queryTimeoutSeconds
        )
    }
}
