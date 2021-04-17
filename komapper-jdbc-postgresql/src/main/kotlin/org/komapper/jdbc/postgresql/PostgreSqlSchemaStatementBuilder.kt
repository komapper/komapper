package org.komapper.jdbc.postgresql

import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel
import java.io.PrintWriter
import java.io.StringWriter

open class PostgreSqlSchemaStatementBuilder(private val dialect: PostgreSqlDialect) : SchemaStatementBuilder {

    private val sql = StringWriter()

    override fun create(entityMetamodels: List<EntityMetamodel<*>>): Statement {
        createSchema(entityMetamodels)
        for (e in entityMetamodels) {
            createTable(e)
            createSequence(e)
        }
        return Statement(sql.toString())
    }

    override fun drop(entityMetamodels: List<EntityMetamodel<*>>): Statement {
        for (e in entityMetamodels) {
            dropTable(e)
            dropSequence(e)
        }
        return Statement(sql.toString())
    }

    override fun dropAll(): Statement {
        throw UnsupportedOperationException()
    }

    private fun createSchema(entityMetamodels: List<EntityMetamodel<*>>) {
        val w = PrintWriter(sql)
        val schemaNames = extractSchemaNames(entityMetamodels)
        for (name in schemaNames) {
            w.println("create schema if not exists ${dialect.quote(name)};")
        }
    }

    private fun createTable(e: EntityMetamodel<*>) {
        val w = PrintWriter(sql)
        w.println("create table if not exists ${e.getCanonicalTableName(dialect::quote)} (")
        val columns = e.properties().joinToString(",\n    ", prefix = "    ") { p ->
            val columnName = p.getCanonicalColumnName(dialect::quote)
            val dataTypeName = if (p.idAssignment is Assignment.Identity<*, *>) {
                when (p.klass) {
                    Int::class -> "serial"
                    Long::class -> "bigserial"
                    else -> error("Illegal assignment type: ${p.klass.qualifiedName}")
                }
            } else {
                dialect.getDataType(p.klass).second
            }
            val notNull = if (p.nullable) "" else " not null"
            val pk = if (p in e.idProperties()) " primary key" else ""
            "$columnName $dataTypeName$notNull$pk"
        }
        w.println(columns)
        w.println(");")
    }

    private fun createSequence(e: EntityMetamodel<*>) {
        val w = PrintWriter(sql)
        val idAssignment = e.properties().map { it.idAssignment }.firstOrNull { it != null }
        if (idAssignment is Assignment.Sequence<*, *>) {
            w.println("create sequence if not exists ${idAssignment.getCanonicalSequenceName(dialect::quote)} start with 1 increment by ${idAssignment.incrementBy};")
        }
    }

    private fun dropTable(e: EntityMetamodel<*>) {
        val w = PrintWriter(sql)
        w.println("drop table if exists ${e.getCanonicalTableName(dialect::quote)};")
    }

    private fun dropSequence(e: EntityMetamodel<*>) {
        val w = PrintWriter(sql)
        val idAssignment = e.properties().map { it.idAssignment }.firstOrNull { it != null }
        if (idAssignment is Assignment.Sequence<*, *>) {
            w.println("drop sequence if exists ${idAssignment.getCanonicalSequenceName(dialect::quote)};")
        }
    }

    private fun extractSchemaNames(entityMetamodels: List<EntityMetamodel<*>>): List<String> {
        val tableSchemaNames = entityMetamodels.map { it.schemaName() }
        val sequenceSchemaNames =
            entityMetamodels.mapNotNull { it.idAssignment() }.filterIsInstance<Assignment.Sequence<*, *>>()
                .map { it.schemaName }
        return (tableSchemaNames + sequenceSchemaNames).distinct().filter { it.isNotBlank() }
    }
}
