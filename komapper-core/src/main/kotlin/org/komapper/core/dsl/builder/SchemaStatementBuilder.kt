package org.komapper.core.dsl.builder

import org.komapper.core.BuilderDialect
import org.komapper.core.Statement
import org.komapper.core.StatementBuffer
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.metamodel.PropertyMetamodel
import org.komapper.core.dsl.metamodel.isAutoIncrement

interface SchemaStatementBuilder {
    fun create(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement>
    fun drop(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement>
}

abstract class AbstractSchemaStatementBuilder(
    protected val dialect: BuilderDialect,
) : SchemaStatementBuilder {

    override fun create(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement> {
        val statements = mutableListOf<Statement>()
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

    protected open fun createTable(metamodel: EntityMetamodel<*, *, *>): List<Statement> {
        val buf = StatementBuffer()
        val tableName = metamodel.getCanonicalTableName(dialect::enquote)
        buf.append("create table ")
        if (dialect.supportsCreateIfNotExists()) {
            buf.append("if not exists ")
        }
        buf.append("$tableName (")
        val columnDefinition = metamodel.properties().joinToString { p ->
            val columnName = p.getCanonicalColumnName(dialect::enquote)
            val dataTypeName = resolveDataTypeName(p)
            val notNull = if (p.nullable) "" else " not null"
            val identity = resolveIdentity(p)
            "$columnName ${dataTypeName}$identity$notNull"
        }
        buf.append(columnDefinition)
        val primaryKeys = metamodel.idProperties() - metamodel.virtualIdProperties().toSet()
        if (primaryKeys.isNotEmpty()) {
            buf.append(", ")
            val primaryKeyName = "pk_${metamodel.tableName()}"
            buf.append("constraint $primaryKeyName primary key(")
            val pkList = primaryKeys.joinToString { p ->
                p.getCanonicalColumnName(dialect::enquote)
            }
            buf.append(pkList)
            buf.append(")")
        }
        buf.append(")")
        return listOf(buf.toStatement())
    }

    protected open fun <INTERIOR : Any> resolveDataTypeName(property: PropertyMetamodel<*, *, INTERIOR>): String {
        return dialect.getDataTypeName<INTERIOR>(property.interiorType)
    }

    protected open fun resolveIdentity(property: PropertyMetamodel<*, *, *>): String {
        return if (property.isAutoIncrement()) " auto_increment" else ""
    }

    protected open fun createSequence(metamodel: EntityMetamodel<*, *, *>): List<Statement> {
        val buf = StatementBuffer()
        val idGenerator = metamodel.idGenerator()
        if (idGenerator is IdGenerator.Sequence<*, *>) {
            buf.append("create sequence ")
            if (dialect.supportsCreateIfNotExists()) {
                buf.append("if not exists ")
            }
            buf.append("${idGenerator.getCanonicalSequenceName(dialect::enquote)} start with ${idGenerator.startWith} increment by ${idGenerator.incrementBy}")
        }
        return listOf(buf.toStatement())
    }

    protected open fun dropTable(metamodel: EntityMetamodel<*, *, *>): List<Statement> {
        val buf = StatementBuffer()
        buf.append("drop table ")
        if (dialect.supportsDropIfExists()) {
            buf.append("if exists ")
        }
        buf.append(metamodel.getCanonicalTableName(dialect::enquote))
        return listOf(buf.toStatement())
    }

    protected open fun dropSequence(metamodel: EntityMetamodel<*, *, *>): List<Statement> {
        val buf = StatementBuffer()
        val idGenerator = metamodel.idGenerator()
        if (idGenerator is IdGenerator.Sequence<*, *>) {
            buf.append("drop sequence ")
            if (dialect.supportsDropIfExists()) {
                buf.append("if exists ")
            }
            buf.append(idGenerator.getCanonicalSequenceName(dialect::enquote))
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
    override fun create(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement> = emptyList()
    override fun drop(metamodels: List<EntityMetamodel<*, *, *>>): List<Statement> = emptyList()
}
