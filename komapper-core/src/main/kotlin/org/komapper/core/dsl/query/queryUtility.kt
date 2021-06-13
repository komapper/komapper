package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunDatabaseConfig
import org.komapper.core.DryRunResult
import org.komapper.core.OptimisticLockException
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.options.VersionOptions
import org.komapper.core.dsl.visitor.DefaultQueryVisitor
import org.komapper.core.toDryRunResult

internal fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
EntityMetamodel<ENTITY, ID, META>.checkIdValueNotNull(entity: ENTITY) {
    this.idProperties().forEach { p ->
        p.getter(entity) ?: error("The id value must not null. name=${p.name}")
    }
}

internal fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>>
EntityMetamodel<ENTITY, ID, META>.checkIdValueNotNull(entities: List<ENTITY>) {
    entities.mapIndexed { index, entity ->
        this.idProperties().forEach { p ->
            p.getter(entity) ?: error("The id value must not null. index=$index, name=${p.name}")
        }
    }
}

fun checkOptimisticLock(
    option: VersionOptions,
    count: Int,
    index: Int?
) {
    if (!option.ignoreVersion && !option.suppressOptimisticLockException) {
        if (count != 1) {
            val message = if (index == null) {
                "count=$count"
            } else {
                "index=$index, count=$count"
            }
            throw OptimisticLockException(message)
        }
    }
}

fun Query<*>.dryRun(config: DatabaseConfig = DryRunDatabaseConfig): DryRunResult {
    // TODO MetadataQuery
    val runner = this.accept(DefaultQueryVisitor())
    val statement = runner.dryRun(config)
    val result = statement.toDryRunResult(config.dialect)
    return if (config is DryRunDatabaseConfig) {
        result.copy(
            description = "This data was generated using DryRunDatabaseConfig. " +
                "To get more correct information, specify the actual DatabaseConfig instance."
        )
    } else {
        result
    }
}
