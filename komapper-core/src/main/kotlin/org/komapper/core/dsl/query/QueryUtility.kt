package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunDatabaseConfig
import org.komapper.core.DryRunResult
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.visitor.DefaultQueryVisitor
import java.io.PrintWriter
import java.io.StringWriter

internal fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> EntityMetamodel<ENTITY, ID, META>.checkIdValueNotNull(entity: ENTITY) {
    this.idProperties().forEach { p ->
        p.getter(entity) ?: error("The id value must not null. name=${p.name}")
    }
}

internal fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> EntityMetamodel<ENTITY, ID, META>.checkIdValueNotNull(entities: List<ENTITY>) {
    entities.mapIndexed { index, entity ->
        this.idProperties().forEach { p ->
            p.getter(entity) ?: error("The id value must not null. index=$index, name=${p.name}")
        }
    }
}

/**
 * Executes a dry run.
 *
 * @param config the database configuration
 * @return the result of dry run
 */
fun Query<*>.dryRun(config: DatabaseConfig = DryRunDatabaseConfig): DryRunResult {
    val description = if (config is DryRunDatabaseConfig) {
        "This data was generated using DryRunDatabaseConfig. " +
            "To get more correct information, specify the actual DatabaseConfig instance."
    } else {
        ""
    }
    val runner = this.accept(DefaultQueryVisitor)
    val statement = try {
        runner.dryRun(config)
    } catch (throwable: Throwable) {
        val message = throwable.getStackTraceAsString()
        return DryRunResult(
            sql = message,
            sqlWithArgs = message,
            throwable = throwable,
            description = description,
        )
    }
    return DryRunResult(
        sql = statement.sql,
        sqlWithArgs = statement.sqlWithArgs,
        args = statement.args,
        throwable = null,
        description = description,
    )
}

private fun Throwable.getStackTraceAsString(): String {
    val stringWriter = StringWriter()
    val printWriter = PrintWriter(stringWriter)
    printStackTrace(printWriter)
    return stringWriter.toString()
}
