package org.komapper.dialect.oracle

import org.komapper.core.dsl.builder.AbstractSchemaStatementBuilder
import org.komapper.core.dsl.metamodel.EntityMetamodel
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.core.dsl.metamodel.PropertyMetamodel

open class OracleSchemaStatementBuilder(dialect: OracleDialect) :
    AbstractSchemaStatementBuilder<OracleDialect>(dialect) {

    // TODO
    override fun createSchema(metamodels: List<EntityMetamodel<*, *, *>>) {
        val schemaNames = extractSchemaNames(metamodels)
        for (name in schemaNames) {
            buf.append("create user ${dialect.enquote(name)}")
            buf.append("identified by \"$name\" ")
            buf.append("default tablespace users ")
            buf.append("temporary tablespace temp;")
        }
    }

    override fun createTable(metamodel: EntityMetamodel<*, *, *>) {
        val canonicalTableName = metamodel.getCanonicalTableName(dialect::enquote)
        buf.append("create table $canonicalTableName (")
        val columnDefinition = metamodel.properties().joinToString { p ->
            val columnName = p.getCanonicalColumnName(dialect::enquote)
            val dataTypeName = resolveDataTypeName(p)
            val notNull = if (p.nullable) "" else " not null"
            val identity = resolveIdentity(p)
            "$columnName ${dataTypeName}$identity$notNull"
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
    }

    override fun resolveIdentity(property: PropertyMetamodel<*, *, *>): String {
        val idGenerator = when (val idAssignment = property.owner.idGenerator()) {
            is IdGenerator.AutoIncrement<*, *> -> idAssignment.property == property
            else -> false
        }
        return if (idGenerator) " generated always as identity" else ""
    }

    override fun createSequence(metamodel: EntityMetamodel<*, *, *>) {
        val idGenerator = metamodel.idGenerator()
        if (idGenerator is IdGenerator.Sequence<*, *>) {
            buf.append("create sequence ${idGenerator.getCanonicalSequenceName(dialect::enquote)} start with ${idGenerator.startWith} increment by ${idGenerator.incrementBy};")
        }
    }

    override fun dropTable(metamodel: EntityMetamodel<*, *, *>) {
        buf.append("drop table ${metamodel.getCanonicalTableName(dialect::enquote)};")
    }

    override fun dropSequence(metamodel: EntityMetamodel<*, *, *>) {
        val idGenerator = metamodel.idGenerator()
        if (idGenerator is IdGenerator.Sequence<*, *>) {
            buf.append("drop sequence ${idGenerator.getCanonicalSequenceName(dialect::enquote)};")
        }
    }
}
