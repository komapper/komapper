package org.komapper.jdbc.h2

import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel
import java.io.PrintWriter
import java.io.StringWriter

open class H2SchemaStatementBuilder(private val dialect: H2Dialect) : SchemaStatementBuilder {

    private val sql = StringWriter()

    override fun create(entityMetamodels: List<EntityMetamodel<*, *>>): Statement {
        createSchema(entityMetamodels)
        for (e in entityMetamodels) {
            createTable(e)
            createSequence(e)
        }
        return Statement(sql.toString())
    }

    override fun drop(entityMetamodels: List<EntityMetamodel<*, *>>): Statement {
        for (e in entityMetamodels) {
            dropTable(e)
            dropSequence(e)
        }
        return Statement(sql.toString())
    }

    override fun dropAll(): Statement {
        return Statement("drop all objects;")
    }

    private fun createSchema(entityMetamodels: List<EntityMetamodel<*, *>>) {
        val w = PrintWriter(sql)
        val schemaNames = extractSchemaNames(entityMetamodels)
        for (name in schemaNames) {
            w.println("create schema if not exists ${dialect.enquote(name)};")
        }
    }

    private fun createTable(e: EntityMetamodel<*, *>) {
        val w = PrintWriter(sql)
        w.println("create table if not exists ${e.getCanonicalTableName(dialect::enquote)} (")
        val columns = e.properties().joinToString(",\n    ", prefix = "    ") { p ->
            val columnName = p.getCanonicalColumnName(dialect::enquote)
            val (_, dataTypeName) = dialect.getDataType(p.klass)
            val notNull = if (p.nullable) "" else " not null"
            val identity = if (p.idAssignment is Assignment.Identity<*, *>) " auto_increment" else ""
            val pk = if (p in e.idProperties()) " primary key" else ""
            "$columnName $dataTypeName$notNull$identity$pk"
        }
        w.println(columns)
        w.println(");")
    }

    private fun createSequence(e: EntityMetamodel<*, *>) {
        val w = PrintWriter(sql)
        val idAssignment = e.properties().map { it.idAssignment }.firstOrNull { it != null }
        if (idAssignment is Assignment.Sequence<*, *>) {
            w.println("create sequence if not exists ${idAssignment.getCanonicalSequenceName(dialect::enquote)} start with 1 increment by ${idAssignment.incrementBy};")
        }
    }

    private fun dropTable(e: EntityMetamodel<*, *>) {
        val w = PrintWriter(sql)
        w.println("drop table if exists ${e.getCanonicalTableName(dialect::enquote)};")
    }

    private fun dropSequence(e: EntityMetamodel<*, *>) {
        val w = PrintWriter(sql)
        val idAssignment = e.properties().map { it.idAssignment }.firstOrNull { it != null }
        if (idAssignment is Assignment.Sequence<*, *>) {
            w.println("drop sequence if exists ${idAssignment.getCanonicalSequenceName(dialect::enquote)};")
        }
    }

    private fun extractSchemaNames(entityMetamodels: List<EntityMetamodel<*, *>>): List<String> {
        val tableSchemaNames = entityMetamodels.map { it.schemaName() }
        val sequenceSchemaNames =
            entityMetamodels.mapNotNull { it.idAssignment() }.filterIsInstance<Assignment.Sequence<*, *>>()
                .map { it.schemaName }
        return (tableSchemaNames + sequenceSchemaNames).distinct().filter { it.isNotBlank() }
    }
}
