package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.EntityNotFoundException
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.builder.getWhereCriteria
import org.komapper.core.dsl.context.WhereProvider
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.hasAutoIncrementProperty
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.options.MutationOptions
import org.komapper.core.dsl.options.OptimisticLockOptions

fun checkWhereClause(whereProvider: WhereProvider) {
    if (!whereProvider.options.allowMissingWhereClause) {
        val criteria = whereProvider.getWhereCriteria()
        if (criteria.isEmpty()) {
            error("WHERE clause not found. If this is intentional, enable the allowMissingWhereClause option.")
        }
    }
}

fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> checkEntityExistence(
    option: MutationOptions,
    metamodel: META,
    entity: ENTITY,
    count: Long,
    index: Int?,
) {
    if (!option.suppressEntityNotFoundException) {
        if (count != 1L) {
            throw EntityNotFoundException(
                "The specified entity does not exist. " +
                    "entity=${metamodel.toText(entity)}, count=$count, index=$index."
            )
        }
    }
}

@Deprecated("Use another overload.", replaceWith = ReplaceWith("checkOptimisticLock(option, metamodel, entity, count, index)"))
fun checkOptimisticLock(
    option: OptimisticLockOptions,
    count: Long,
    index: Int?,
) {
    if (!option.disableOptimisticLock && !option.suppressOptimisticLockException) {
        if (count != 1L) {
            val message = if (index == null) {
                "count=$count"
            } else {
                "index=$index, count=$count"
            }
            throw OptimisticLockException(message)
        }
    }
}

fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> checkOptimisticLock(
    option: OptimisticLockOptions,
    metamodel: META,
    entity: ENTITY,
    count: Long,
    index: Int?,
) {
    if (!option.disableOptimisticLock && !option.suppressOptimisticLockException) {
        if (count != 1L) {
            throw OptimisticLockException(
                "Optimistic lock failed. " +
                    "entity=${metamodel.toText(entity)}, count=$count, index=$index."
            )
        }
    }
}

internal fun checkAutoIncrementWhenInsertingMultipleRows(config: DatabaseConfig, metamodel: EntityMetamodel<*, *, *>) {
    val dialect = config.dialect
    if (!dialect.supportsAutoIncrementWhenInsertingMultipleRows() &&
        metamodel.hasAutoIncrementProperty()
    ) {
        throw UnsupportedOperationException(
            "The dialect(driver=${dialect.driver}) does not support auto-increment when inserting multiple rows."
        )
    }
}

internal fun checkConflictTargetInUpsertStatement(config: DatabaseConfig, conflictTarget: String?) {
    val dialect = config.dialect
    if (!config.dialect.supportsConflictTargetInUpsertStatement() && conflictTarget != null) {
        throw UnsupportedOperationException(
            "The dialect(driver=${dialect.driver}) does not support specifying a conflict target in upsert statements.",
        )
    }
}

internal fun checkIndexPredicateInUpsertStatement(config: DatabaseConfig, indexPredicate: WhereDeclaration?) {
    val dialect = config.dialect
    if (!config.dialect.supportsIndexPredicateInUpsertStatement() && indexPredicate != null) {
        throw UnsupportedOperationException(
            "The dialect(driver=${dialect.driver}) does not support specifying a index predicate in upsert statements.",
        )
    }
}

internal fun checkBatchExecutionOfParameterizedStatement(config: DatabaseConfig) {
    val dialect = config.dialect
    if (!config.dialect.supportsBatchExecutionOfParameterizedStatement()) {
        throw UnsupportedOperationException("The dialect(driver=${dialect.driver}) does not support batch execution.")
    }
}

internal fun checkBatchExecutionReturningGeneratedValues(config: DatabaseConfig, metamodel: EntityMetamodel<*, *, *>) {
    val dialect = config.dialect
    if (!dialect.supportsBatchExecutionReturningGeneratedValues() &&
        metamodel.hasAutoIncrementProperty()
    ) {
        throw UnsupportedOperationException(
            "The dialect(driver=${dialect.driver}) does not support batch execution for entities with auto-increment properties."
        )
    }
}

internal fun checkDeleteReturning(config: DatabaseConfig) {
    val dialect = config.dialect
    if (!config.dialect.supportsDeleteReturning()) {
        throw UnsupportedOperationException(
            "The dialect(driver=${dialect.driver}) does not support `returning` for delete statements. " +
                "Do not use the `returning` function in your query.",
        )
    }
}

internal fun checkGeneratedKeysReturningWhenInsertingMultipleRows(
    config: DatabaseConfig,
    metamodel: EntityMetamodel<*, *, *>,
    options: InsertOptions,
) {
    val dialect = config.dialect
    if (!dialect.supportsGeneratedKeysReturningWhenInsertingMultipleRows() &&
        metamodel.hasAutoIncrementProperty() &&
        options.returnGeneratedKeys
    ) {
        throw UnsupportedOperationException(
            "The dialect(driver=${dialect.driver}) does not support returning generated keys when inserting multiple rows. " +
                "You can avoid this exception by setting `InsertOption.returnGeneratedKeys=false`.",
        )
    }
}

internal fun checkInsertSingleReturning(config: DatabaseConfig) {
    val dialect = config.dialect
    if (!config.dialect.supportsInsertSingleReturning()) {
        throw UnsupportedOperationException(
            "The dialect(driver=${dialect.driver}) does not support `returning` for inserting single row. " +
                "Do not use the `returning` function in your query.",
        )
    }
}

internal fun checkInsertMultipleReturning(config: DatabaseConfig) {
    val dialect = config.dialect
    if (!config.dialect.supportsInsertMultipleReturning()) {
        throw UnsupportedOperationException(
            "The dialect(driver=${dialect.driver}) does not support `returning` for inserting multiple rows. " +
                "Do not use the `returning` function in your query.",
        )
    }
}

internal fun checkOptimisticLockOfBatchExecution(config: DatabaseConfig, options: OptimisticLockOptions) {
    val dialect = config.dialect
    if (!config.dialect.supportsOptimisticLockOfBatchExecution() &&
        !options.disableOptimisticLock &&
        !options.suppressOptimisticLockException
    ) {
        throw UnsupportedOperationException(
            "The dialect(driver=${dialect.driver}) does not support optimistic lock of batch execution. " +
                "You can avoid this exception by setting `OptimisticLockOptions.disableOptimisticLock=true` or `OptimisticLockOptions.suppressOptimisticLockException=true`",
        )
    }
}

internal fun checkSearchConditionInUpsertStatement(config: DatabaseConfig, whereProvider: WhereProvider) {
    val dialect = config.dialect
    if (!config.dialect.supportsSearchConditionInUpsertStatement()) {
        val criteria = whereProvider.getWhereCriteria()
        if (criteria.isNotEmpty()) {
            throw UnsupportedOperationException(
                "The dialect(driver=${dialect.driver}) does not support specifying search conditions in upsert statements.",
            )
        }
    }
}

internal fun checkUpdateReturning(config: DatabaseConfig) {
    val dialect = config.dialect
    if (!config.dialect.supportsUpdateReturning()) {
        throw UnsupportedOperationException(
            "The dialect(driver=${dialect.driver}) does not support `returning` for update statements. " +
                "Do not use the `returning` function in your query.",
        )
    }
}

internal fun checkUpsertSingleReturning(config: DatabaseConfig) {
    val dialect = config.dialect
    if (!config.dialect.supportsUpsertSingleReturning()) {
        throw UnsupportedOperationException(
            "The dialect(driver=${dialect.driver}) does not support `returning` in combination with `onDuplicateKeyIgnore` or `onDuplicateKeyUpdate` for single row. " +
                "Do not use the `returning` function in your query.",
        )
    }
}

internal fun checkUpsertMultipleReturning(config: DatabaseConfig) {
    val dialect = config.dialect
    if (!config.dialect.supportsUpsertMultipleReturning()) {
        throw UnsupportedOperationException(
            "The dialect(driver=${dialect.driver}) does not support `returning` in combination with `onDuplicateKeyIgnore` or `onDuplicateKeyUpdate` for multiple rows. " +
                "Do not use the `returning` function in your query.",
        )
    }
}
