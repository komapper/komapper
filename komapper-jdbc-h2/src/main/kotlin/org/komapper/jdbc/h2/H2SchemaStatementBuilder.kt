package org.komapper.jdbc.h2

import org.komapper.core.data.Statement
import org.komapper.core.dsl.builder.SchemaStatementBuilder
import org.komapper.core.dsl.getName
import org.komapper.core.metamodel.Assignment
import org.komapper.core.metamodel.EntityMetamodel
import org.komapper.core.metamodel.IdGeneratorDescriptor
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
import kotlin.reflect.full.isSubclassOf

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
        w.println("create table if not exists ${e.getName(dialect::quote)} (")
        val columns = e.properties().joinToString(",\n    ", prefix = "    ") { p ->
            val columnName = p.getName(dialect::quote)
            val dataType = getDataType(p.klass)
            val notNull = if (p.nullable) "" else " not null"
            val identity = if (p.idGenerator is IdGeneratorDescriptor.Identity<*, *>) " auto_increment" else ""
            val pk = if (p in e.idProperties()) " primary key" else ""
            "$columnName $dataType$notNull$identity$pk"
        }
        w.println(columns)
        w.println(");")
    }

    private fun createSequence(e: EntityMetamodel<*>) {
        val w = PrintWriter(sql)
        val idGenerator = e.properties().map { it.idGenerator }.firstOrNull { it != null }
        if (idGenerator is IdGeneratorDescriptor.Sequence<*, *>) {
            w.println("create sequence if not exists ${idGenerator.getName(dialect::quote)} start with 1 increment by ${idGenerator.incrementBy};")
        }
    }

    private fun dropTable(e: EntityMetamodel<*>) {
        val w = PrintWriter(sql)
        w.println("drop table if exists ${e.getName(dialect::quote)};")
    }

    private fun dropSequence(e: EntityMetamodel<*>) {
        val w = PrintWriter(sql)
        val idGenerator = e.properties().map { it.idGenerator }.firstOrNull { it != null }
        if (idGenerator is IdGeneratorDescriptor.Sequence<*, *>) {
            w.println("drop sequence if exists ${idGenerator.getName(dialect::quote)};")
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
    protected fun getDataType(klass: KClass<*>): String = when {
        klass == Any::class -> "other"
        klass == java.sql.Array::class -> "array"
        klass == BigDecimal::class -> "bigint"
        klass == BigInteger::class -> "bigint"
        klass == Blob::class -> "blob"
        klass == Boolean::class -> "bool"
        klass == Byte::class -> "tinyint"
        klass == ByteArray::class -> "binary"
        klass == Double::class -> "double"
        klass == Clob::class -> "clob"
        klass.isSubclassOf(Enum::class) -> "varchar(500)"
        klass == Float::class -> "float"
        klass == Int::class -> "integer"
        klass == LocalDateTime::class -> "timestamp"
        klass == LocalDate::class -> "date"
        klass == LocalTime::class -> "time"
        klass == Long::class -> "bigint"
        klass == NClob::class -> "nclob"
        klass == OffsetDateTime::class -> "timestamp with time zone"
        klass == Short::class -> "smallint"
        klass == String::class -> "varchar(500)"
        klass == SQLXML::class -> "clob"
        else -> error(
            "The dataType is not found for the klass \"${klass.qualifiedName}\"."
        )
    }
}
