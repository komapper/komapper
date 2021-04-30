package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.PropertyMetamodel

interface SchemaStatementBuilder {
    fun create(metamodels: List<EntityMetamodel<*, *, *>>): Statement
    fun drop(metamodels: List<EntityMetamodel<*, *, *>>): Statement
    fun dropAll(): Statement
}

abstract class AbstractSchemaStatementBuilder<D : Dialect>(protected val dialect: D) : SchemaStatementBuilder {

    protected val buf = StatementBuffer(dialect::formatValue)

    override fun create(metamodels: List<EntityMetamodel<*, *, *>>): Statement {
        createSchema(metamodels)
        for (e in metamodels) {
            createTable(e)
            createSequence(e)
        }
        return buf.toStatement()
    }

    override fun drop(metamodels: List<EntityMetamodel<*, *, *>>): Statement {
        for (e in metamodels) {
            dropTable(e)
            dropSequence(e)
        }
        return buf.toStatement()
    }

    override fun dropAll(): Statement {
        throw UnsupportedOperationException()
    }

    protected open fun createSchema(metamodels: List<EntityMetamodel<*, *, *>>) {
        val schemaNames = extractSchemaNames(metamodels)
        for (name in schemaNames) {
            buf.append("create schema if not exists ${dialect.enquote(name)};")
        }
    }

    protected open fun createTable(metamodel: EntityMetamodel<*, *, *>) {
        val tableName = metamodel.getCanonicalTableName(dialect::enquote)
        buf.append("create table if not exists $tableName (")
        metamodel.properties().joinTo(buf) { p ->
            val columnName = p.getCanonicalColumnName(dialect::enquote)
            val dataTypeName = resolveDataTypeName(p)
            val notNull = if (p.nullable) "" else " not null"
            val identity = resolveIdentity(p)
            "$columnName ${dataTypeName}$notNull$identity"
        }
        buf.append(", ")
        buf.append("constraint pk_$tableName primary key")
        metamodel.idProperties().joinTo(buf, prefix = "(", postfix = ")") { p ->
            p.getCanonicalColumnName(dialect::enquote)
        }
        buf.append(");")
    }

    protected open fun resolveDataTypeName(property: PropertyMetamodel<*, *, *>): String {
        return dialect.getDataType(property.interiorClass).name
    }

    protected open fun resolveIdentity(property: PropertyMetamodel<*, *, *>): String {
        return if (property.idAssignment is Assignment.AutoIncrement<*, *, *>) " auto_increment" else ""
    }

    protected open fun createSequence(metamodel: EntityMetamodel<*, *, *>) {
        val idAssignment = metamodel.properties().map { it.idAssignment }.firstOrNull { it != null }
        if (idAssignment is Assignment.Sequence<*, *, *>) {
            buf.append("create sequence if not exists ${idAssignment.getCanonicalSequenceName(dialect::enquote)} start with ${idAssignment.startWith} increment by ${idAssignment.incrementBy};")
        }
    }

    protected open fun dropTable(metamodel: EntityMetamodel<*, *, *>) {
        buf.append("drop table if exists ${metamodel.getCanonicalTableName(dialect::enquote)};")
    }

    protected open fun dropSequence(metamodel: EntityMetamodel<*, *, *>) {
        val idAssignment = metamodel.properties().map { it.idAssignment }.firstOrNull { it != null }
        if (idAssignment is Assignment.Sequence<*, *, *>) {
            buf.append("drop sequence if exists ${idAssignment.getCanonicalSequenceName(dialect::enquote)};")
        }
    }

    protected open fun extractSchemaNames(metamodels: List<EntityMetamodel<*, *, *>>): List<String> {
        val tableSchemaNames = metamodels.map { it.schemaName() }
        val sequenceSchemaNames =
            metamodels.mapNotNull { it.idAssignment() }.filterIsInstance<Assignment.Sequence<*, *, *>>()
                .map { it.schemaName }
        return (tableSchemaNames + sequenceSchemaNames).distinct().filter { it.isNotBlank() }
    }
}

internal object DryRunSchemaStatementBuilder : SchemaStatementBuilder {

    private val statement = Statement(
        "Not supported to invoke the dryRun function with the default value."
    )

    override fun create(metamodels: List<EntityMetamodel<*, *, *>>) = statement
    override fun drop(metamodels: List<EntityMetamodel<*, *, *>>) = statement
    override fun dropAll() = statement
}
