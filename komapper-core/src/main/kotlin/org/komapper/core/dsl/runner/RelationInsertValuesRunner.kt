package org.komapper.core.dsl.runner

import org.komapper.core.BuilderDialect
import org.komapper.core.DatabaseConfig
import org.komapper.core.DryRunStatement
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.getAssignments
import org.komapper.core.dsl.context.RelationInsertValuesContext
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.isAutoIncrement

class RelationInsertValuesRunner<ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>>(
    private val context: RelationInsertValuesContext<ENTITY, ID, META>,
) : Runner {
    override fun check(config: DatabaseConfig) = Unit

    override fun dryRun(config: DatabaseConfig): DryRunStatement {
        val idAssignment = when (val idGenerator = context.target.idGenerator()) {
            is IdGenerator.Sequence<ENTITY, ID> -> {
                val argument = Operand.Argument(idGenerator.property, null)
                idGenerator.property to argument
            }

            else -> {
                null
            }
        }
        val statement = buildStatement(config, idAssignment)
        return DryRunStatement.of(statement, config)
    }

    fun buildStatement(
        config: DatabaseConfig,
        idAssignment: Pair<PropertyMetamodel<ENTITY, ID, *>, Operand>?,
    ): Statement {
        val assignments = getAssignments(config, idAssignment)
        val builder = config.dialect.getRelationInsertValuesStatementBuilder(BuilderDialect(config), context)
        return builder.build(assignments)
    }

    private fun getAssignments(
        config: DatabaseConfig,
        idAssignment: Pair<PropertyMetamodel<ENTITY, ID, *>, Operand>?,
    ): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
        val assignments = context.getAssignments()
        val properties = assignments.map { it.first }
        val clock = config.clockProvider.now()
        val additionalAssignments = listOfNotNull(
            idAssignment,
            context.target.versionAssignment(),
            context.target.createdAtAssignment(clock),
            context.target.updatedAtAssignment(clock),
        ).filterNot { it.first in properties }
        return (assignments + additionalAssignments)
            .filter { !it.first.isAutoIncrement() }
    }

    fun preInsertUsingAutoIncrement(idGenerator: IdGenerator.AutoIncrement<ENTITY, ID>): String? {
        return if (context.options.returnGeneratedKeys) {
            idGenerator.property.columnName
        } else {
            null
        }
    }

    fun postInsertUsingAutoIncrement(count: Long, keys: List<Long>): Pair<Long, ID?> {
        val id = keys.firstOrNull()?.let { context.target.convertToId(it) }
        return count to id
    }

    fun preInsertUsingSequence(idGenerator: IdGenerator.Sequence<ENTITY, ID>, id: ID): Pair<PropertyMetamodel<ENTITY, ID, *>, Operand> {
        val argument = Operand.Argument(idGenerator.property, id)
        return idGenerator.property to argument
    }
}
