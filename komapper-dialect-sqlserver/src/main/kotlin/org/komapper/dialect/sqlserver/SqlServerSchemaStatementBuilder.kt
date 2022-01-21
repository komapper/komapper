package org.komapper.dialect.sqlserver

import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.metamodel.PropertyMetamodel

open class SqlServerSchemaStatementBuilder(dialect: SqlServerDialect) :
    AbstractSchemaStatementBuilder<SqlServerDialect>(dialect) {

    override fun createSchema(metamodels: List<EntityMetamodel<*, *, *>>) {
        val schemaNames = extractSchemaNames(metamodels)
        for (name in schemaNames) {
            buf.append("if not exists (select * from information_schema.schemata where schema_name = '$name') begin ")
            buf.append("create schema if not exists ${dialect.enquote(name)};")
            buf.append("end;")
        }
    }

    override fun createTable(metamodel: EntityMetamodel<*, *, *>) {
        val tableSchema = "coalesce(nullif('', '${metamodel.schemaName()}'), schema_name())"
        val tableName = "'${metamodel.tableName()}'"
        buf.append("if not exists (")
        buf.append("select * from information_schema.tables where table_schema = $tableSchema and table_name = $tableName")
        buf.append(") begin ")
        val canonicalTableName = metamodel.getCanonicalTableName(dialect::enquote)
        buf.append("create table $canonicalTableName (")
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
        buf.append("));")
        buf.append("end;")
    }

    override fun resolveIdentity(property: PropertyMetamodel<*, *, *>): String {
        val idGenerator = when (val idAssignment = property.owner.idGenerator()) {
            is IdGenerator.AutoIncrement<*, *> -> idAssignment.property == property
            else -> false
        }
        return if (idGenerator) " identity" else ""
    }

    override fun createSequence(metamodel: EntityMetamodel<*, *, *>) {
        val idGenerator = metamodel.idGenerator()
        if (idGenerator is IdGenerator.Sequence<*, *>) {
            val sequenceSchema = "coalesce(nullif('', '${idGenerator.schemaName}'), schema_name())"
            val sequenceName = "'${idGenerator.name}'"
            buf.append("if (exists (")
            buf.append("select * from information_schema.sequences where sequence_schema = $sequenceSchema and sequence_name = $sequenceName")
            buf.append(")) begin ")
            buf.append("create sequence ${idGenerator.getCanonicalSequenceName(dialect::enquote)} start with ${idGenerator.startWith} increment by ${idGenerator.incrementBy};")
            buf.append("end;")
        }
    }
}
