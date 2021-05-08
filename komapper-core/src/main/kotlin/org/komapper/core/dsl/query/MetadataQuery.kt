package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.Dialect
import org.komapper.core.jdbc.Column
import org.komapper.core.jdbc.Table
import org.komapper.core.jdbc.TableName
import java.sql.DatabaseMetaData

interface MetadataQuery : Query<List<Table>>

internal class MetadataQueryImpl(
    private val schemaName: String?,
    private val tableNamePattern: String?,
    private val tableTypes: List<String>
) : MetadataQuery {

    override fun run(config: DatabaseConfig): List<Table> {
        return config.session.connection.use { con ->
            val reader = MetadataReader(
                config.dialect,
                con.metaData,
                schemaName,
                tableNamePattern,
                tableTypes
            )
            reader.read()
        }
    }

    override fun dryRun(config: DatabaseConfig): String {
        return ""
    }
}

internal class MetadataReader(
    private val dialect: Dialect,
    private val metaData: DatabaseMetaData,
    private val schemaPattern: String?,
    private val tableNamePattern: String?,
    private val tableTypes: List<String>
) {
    fun read(): List<Table> {
        return getTableNames().map { tableName ->
            val columns = getColumns(tableName)
            val primaryKeys = getPrimaryKeys(tableName)
            Table(tableName, columns, primaryKeys)
        }
    }

    private fun getTableNames(): List<TableName> {
        val tableNames: MutableList<TableName> = mutableListOf()
        val rs = metaData.getTables(
            null,
            schemaPattern ?: dialect.getDefaultSchemaName(metaData.userName),
            tableNamePattern,
            tableTypes.toTypedArray()
        )
        while (rs.next()) {
            val tableName = TableName(
                name = rs.getString("TABLE_NAME"),
                catalog = rs.getString("TABLE_CAT"),
                schema = rs.getString("TABLE_SCHEM")
            )
            tableNames.add(tableName)
        }
        return tableNames
    }

    private fun getColumns(tableName: TableName): List<Column> {
        val columns: MutableList<Column> = mutableListOf()
        val rs = metaData.getColumns(
            tableName.catalog, tableName.schema, tableName.name, null
        )
        while (rs.next()) {
            val column = Column(
                name = rs.getString("COLUMN_NAME"),
                dataType = rs.getInt("DATA_TYPE"),
                typeName = rs.getString("TYPE_NAME").lowercase(),
                length = rs.getInt("COLUMN_SIZE"),
                scale = rs.getInt("DECIMAL_DIGITS"),
                isNullable = rs.getBoolean("NULLABLE"),
            )
            columns.add(column)
        }
        return columns
    }

    private fun getPrimaryKeys(tableName: TableName): List<String> {
        val keys = mutableListOf<String>()
        val rs = metaData.getPrimaryKeys(
            tableName.catalog, tableName.schema, tableName.name
        )
        while (rs.next()) {
            keys.add(rs.getString("COLUMN_NAME"))
        }
        return keys
    }
}
