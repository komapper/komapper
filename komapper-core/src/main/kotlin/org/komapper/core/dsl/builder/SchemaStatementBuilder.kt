package org.komapper.core.dsl.builder

import org.komapper.core.Dialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.metamodel.PropertyMetamodel

interface SchemaStatementBuilder {
    fun create(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement>
    fun drop(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement>
    fun dropAll(): List<Statement>
}

abstract class AbstractSchemaStatementBuilder<D : Dialect>(protected val dialect: D) : SchemaStatementBuilder {

    override fun create(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement> {
        val statements = mutableListOf<Statement>()
        statements.addAll(createSchema(metamodels))
        for (e in metamodels) {
            statements.addAll(createTable(e))
            statements.addAll(createSequence(e))
        }
        return statements.filter { it.parts.isNotEmpty() }
    }

    override fun drop(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement> {
        val statements = mutableListOf<Statement>()
        for (e in metamodels) {
            statements.addAll(dropTable(e))
            statements.addAll(dropSequence(e))
        }
        return statements.filter { it.parts.isNotEmpty() }
    }

    override fun dropAll(): List<Statement> {
        throw UnsupportedOperationException()
    }

    protected open fun createSchema(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement> {
        val buf = StatementBuffer()
        val schemaNames = extractSchemaNames(metamodels)
        for (name in schemaNames) {
            buf.append("create schema if not exists ${dialect.enquote(name)}")
        }
        return listOf(buf.toStatement())
    }

    protected open fun createTable(metamodel: EntityMetamodel<*, *, *>): List<Statement> {
        val buf = StatementBuffer()
        val tableName = metamodel.getCanonicalTableName(dialect::enquote)
        buf.append("create table if not exists $tableName (")
        val columnDefinition = metamodel.properties().joinToString { p ->
            val columnName = p.getCanonicalColumnName(dialect::enquote)
            val dataTypeName = resolveDataTypeName(p)
            val notNull = if (p.nullable) "" else " not null"
            val identity = resolveIdentity(p)
            "$columnName ${dataTypeName}$notNull$identity"
        }
        buf.append(columnDefinition)
        buf.append(", ")
        val primaryKeyName = "pk_${metamodel.tableName()}"
        buf.append("constraint $primaryKeyName primary key(")
        val idList = metamodel.idProperties().joinToString { p ->
            p.getCanonicalColumnName(dialect::enquote)
        }
        buf.append(idList)
        buf.append("))")
        return listOf(buf.toStatement())
    }

    protected open fun resolveDataTypeName(property: PropertyMetamodel<*, *, *>): String {
        return dialect.getDataTypeName(property.interiorClass)
    }

    protected open fun resolveIdentity(property: PropertyMetamodel<*, *, *>): String {
        val idGenerator = when (val idAssignment = property.owner.idGenerator()) {
            is IdGenerator.AutoIncrement<*, *> -> idAssignment.property == property
            else -> false
        }
        return if (idGenerator) " auto_increment" else ""
    }

    protected open fun createSequence(metamodel: EntityMetamodel<*, *, *>): List<Statement> {
        val buf = StatementBuffer()
        val idGenerator = metamodel.idGenerator()
        if (idGenerator is IdGenerator.Sequence<*, *>) {
            buf.append("create sequence if not exists ${idGenerator.getCanonicalSequenceName(dialect::enquote)} start with ${idGenerator.startWith} increment by ${idGenerator.incrementBy}")
        }
        return listOf(buf.toStatement())
    }

    protected open fun dropTable(metamodel: EntityMetamodel<*, *, *>): List<Statement> {
        val buf = StatementBuffer()
        buf.append("drop table if exists ${metamodel.getCanonicalTableName(dialect::enquote)}")
        return listOf(buf.toStatement())
    }

    protected open fun dropSequence(metamodel: EntityMetamodel<*, *, *>): List<Statement> {
        val buf = StatementBuffer()
        val idGenerator = metamodel.idGenerator()
        if (idGenerator is IdGenerator.Sequence<*, *>) {
            buf.append("drop sequence if exists ${idGenerator.getCanonicalSequenceName(dialect::enquote)}")
        }
        return listOf(buf.toStatement())
    }

    protected open fun extractSchemaNames(metamodels: List<EntityMetamodel<*, *, *>>): List<String> {
        val tableSchemaNames = metamodels.map { it.schemaName() }
        val sequenceSchemaNames =
            metamodels.mapNotNull { it.idGenerator() }.filterIsInstance<IdGenerator.Sequence<*, *>>()
                .map { it.schemaName }
        return (tableSchemaNames + sequenceSchemaNames).distinct().filter { it.isNotBlank() }
    }
}

object DryRunSchemaStatementBuilder : SchemaStatementBuilder {

    private val statement = Statement(
        "Not supported to invoke the dryRun function with the default value."
    )

    override fun create(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement> = emptyList()
    override fun drop(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement> = emptyList()
    override fun dropAll(): List<Statement> = emptyList()
}
