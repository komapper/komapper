package org.komapper.jdbc.mysql

import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel
import java.io.PrintWriter
import java.io.StringWriter

open class MySqlSchemaStatementBuilder(private val dialect: MySqlDialect) : SchemaStatementBuilder {

    private val sql = StringWriter()

    override fun create(entityMetamodels: List<EntityMetamodel<*, *, *>>): Statement {
        createSchema(entityMetamodels)
        for (e in entityMetamodels) {
            createTable(e)
        }
        return Statement(sql.toString())
    }

    override fun drop(entityMetamodels: List<EntityMetamodel<*, *, *>>): Statement {
        for (e in entityMetamodels) {
            dropTable(e)
        }
        return Statement(sql.toString())
    }

    override fun dropAll(): Statement {
        throw UnsupportedOperationException()
    }

    private fun createSchema(entityMetamodels: List<EntityMetamodel<*, *, *>>) {
        val w = PrintWriter(sql)
        val schemaNames = extractSchemaNames(entityMetamodels)
        for (name in schemaNames) {
            w.println("create schema if not exists ${dialect.enquote(name)};")
        }
    }

    private fun createTable(e: EntityMetamodel<*, *, *>) {
        val w = PrintWriter(sql)
        val tableName = e.getCanonicalTableName(dialect::enquote)
        w.println("create table if not exists $tableName (")
        val columns = e.properties().map { p ->
            val columnName = p.getCanonicalColumnName(dialect::enquote)
            val (_, dataTypeName) = dialect.getDataType(p.klass)
            val notNull = if (p.nullable) "" else " not null"
            val identity = if (p.idAssignment is Assignment.Identity<*, *>) " auto_increment" else ""
            "$columnName $dataTypeName$notNull$identity"
        }
        for (column in columns) {
            w.println("    $column,")
        }
        val pkList = e.idProperties().joinToString { it.getCanonicalColumnName(dialect::enquote) }
        w.println("    constraint pk_$tableName primary key($pkList)")
        w.println(");")
    }

    private fun dropTable(e: EntityMetamodel<*, *, *>) {
        val w = PrintWriter(sql)
        w.println("drop table if exists ${e.getCanonicalTableName(dialect::enquote)};")
    }

    private fun extractSchemaNames(entityMetamodels: List<EntityMetamodel<*, *, *>>): List<String> {
        val tableSchemaNames = entityMetamodels.map { it.schemaName() }
        val sequenceSchemaNames =
            entityMetamodels.mapNotNull { it.idAssignment() }.filterIsInstance<Assignment.Sequence<*, *>>()
                .map { it.schemaName }
        return (tableSchemaNames + sequenceSchemaNames).distinct().filter { it.isNotBlank() }
    }
}
