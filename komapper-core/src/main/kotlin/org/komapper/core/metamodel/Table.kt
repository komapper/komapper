package org.komapper.core.metamodel

interface Table {
    fun tableName(): String
    fun catalogName(): String
    fun schemaName(): String
    fun properties(): List<Column<*>>
    fun idAssignment(): Assignment<*>?
}
