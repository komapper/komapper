package org.komapper.jdbc.h2

import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.metamodel.Assignment
import org.komapper.core.dsl.metamodel.EntityMetamodel
import java.io.PrintWriter
import java.io.StringWriter
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Blob
import java.sql.Clob
import java.sql.NClob
import java.sql.SQLXML
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import kotlin.reflect.KClass

open class H2SchemaStatementBuilder(private val dialect: H2Dialect) : SchemaStatementBuilder {

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
        return Statement("drop all objects;")
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
            val dataType = getDataType(p.klass)
            val notNull = if (p.nullable) "" else " not null"
            val identity = if (p.idAssignment is Assignment.Identity<*, *>) " auto_increment" else ""
            val pk = if (p in e.idProperties()) " primary key" else ""
            "$columnName $dataType$notNull$identity$pk"
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

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun getDataType(klass: KClass<*>): String = when (klass) {
        Any::class -> "other"
        java.sql.Array::class -> "array"
        BigDecimal::class -> "bigint"
        BigInteger::class -> "bigint"
        Blob::class -> "blob"
        Boolean::class -> "bool"
        Byte::class -> "tinyint"
        ByteArray::class -> "binary"
        Double::class -> "double"
        Clob::class -> "clob"
        Float::class -> "float"
        Int::class -> "integer"
        LocalDateTime::class -> "timestamp"
        LocalDate::class -> "date"
        LocalTime::class -> "time"
        Long::class -> "bigint"
        NClob::class -> "nclob"
        OffsetDateTime::class -> "timestamp with time zone"
        Short::class -> "smallint"
        String::class -> "varchar(500)"
        SQLXML::class -> "clob"
        else -> error(
            "The dataType is not found for the klass \"${klass.qualifiedName}\"."
        )
    }
}
