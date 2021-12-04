package org.komapper.core.dsl.runner

import org.komapper.core.DatabaseConfig
import org.komapper.core.Statement
import org.komapper.core.dsl.builder.RelationInsertValuesStatementBuilder
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

    override fun dryRun(config: DatabaseConfig): Statement {
        val idAssignment = when (val idGenerator = context.target.idGenerator()) {
            is IdGenerator.Sequence<ENTITY, ID> -> {
                val argument = Operand.Argument(idGenerator.property, null)
                idGenerator.property to argument
            }
            else -> null
        }
        val versionAssignment = context.target.versionAssignment()
        val clock = config.clockProvider.now()
        val createdAtAssignment = context.target.createdAtAssignment(clock)
        val updatedAtAssignment = context.target.updatedAtAssignment(clock)
        return buildStatement(config, idAssignment, versionAssignment, createdAtAssignment, updatedAtAssignment)
    }

    fun buildStatement(
        config: DatabaseConfig,
        idAssignment: Pair<PropertyMetamodel<ENTITY, ID, *>, Operand>? = null,
        versionAssignment: Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = null,
        createdAtAssignment: Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = null,
        updatedAtAssignment: Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = null
    ): Statement {
        val assignments = getAssignments(
            idAssignment,
            versionAssignment,
            createdAtAssignment,
            updatedAtAssignment
        )
        val builder = RelationInsertValuesStatementBuilder(config.dialect, context)
        return builder.build(assignments)
    }

    private fun getAssignments(
        idAssignment: Pair<PropertyMetamodel<ENTITY, ID, *>, Operand>? = null,
        versionAssignment: Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = null,
        createdAtAssignment: Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = null,
        updatedAtAssignment: Pair<PropertyMetamodel<ENTITY, *, *>, Operand>? = null
    ): List<Pair<PropertyMetamodel<ENTITY, *, *>, Operand>> {
        val assignments = context.getAssignments()
        val properties = assignments.map { it.first }
        val additionalAssignments = listOfNotNull(
            idAssignment,
            versionAssignment,
            createdAtAssignment,
            updatedAtAssignment
        ).filterNot { it.first in properties }
        return (assignments + additionalAssignments)
            .filter { !it.first.isAutoIncrement() }
    }
}
