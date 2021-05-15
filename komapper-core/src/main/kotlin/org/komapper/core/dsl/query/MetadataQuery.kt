package org.komapper.core.dsl.query

import org.komapper.core.DatabaseConfig
import org.komapper.core.jdbc.Column
import org.komapper.core.jdbc.Table
import java.sql.DatabaseMetaData

interface MetadataQuery : Query<List<Table>>

internal class MetadataQueryImpl(
    private val catalog: String?,
    private val schemaName: String?,
    private val tableNamePattern: String?,
    private val tableTypes: List<String>
) : MetadataQuery {

    override fun run(config: DatabaseConfig): List<Table> {
        return config.session.connection.use { con ->
            val reader = MetadataReader(
                con.metaData,
                catalog,
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
    private val metaData: DatabaseMetaData,
    private val catalog: String?,
    private val schemaPattern: String?,
    private val tableNamePattern: String?,
    private val tableTypes: List<String>
) {
    fun read(): List<Table> {
        return getTables().map { table ->
            table.copy(
                columns = getColumns(table),
                primaryKeys = getPrimaryKeys(table)
            )
        }
    }

    private fun getTables(): List<Table> {
        val tables: MutableList<Table> = mutableListOf()
        val rs = metaData.getTables(
            catalog,
            schemaPattern,
            tableNamePattern,
            tableTypes.toTypedArray()
        )
        while (rs.next()) {
            val table = Table(
                name = rs.getString("TABLE_NAME"),
                catalog = rs.getString("TABLE_CAT"),
                schema = rs.getString("TABLE_SCHEM")
            )
            tables.add(table)
        }
        return tables
    }

    private fun getColumns(table: Table): List<Column> {
        val columns: MutableList<Column> = mutableListOf()
        val rs = metaData.getColumns(
            table.catalog, table.schema, table.name, null
        )
        while (rs.next()) {
            val column = Column(
                name = rs.getString("COLUMN_NAME"),
                dataType = rs.getInt("DATA_TYPE"),
                typeName = rs.getString("TYPE_NAME"),
                length = rs.getInt("COLUMN_SIZE"),
                scale = rs.getInt("DECIMAL_DIGITS"),
                nullable = rs.getBoolean("NULLABLE"),
            )
            columns.add(column)
        }
        return columns
    }

    private fun getPrimaryKeys(table: Table): List<String> {
        val keys = mutableListOf<String>()
        val rs = metaData.getPrimaryKeys(
            table.catalog, table.schema, table.name
        )
        while (rs.next()) {
            keys.add(rs.getString("COLUMN_NAME"))
        }
        return keys
    }
}
