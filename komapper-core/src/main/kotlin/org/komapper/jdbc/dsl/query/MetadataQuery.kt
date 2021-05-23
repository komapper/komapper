package org.komapper.jdbc.dsl.query

import org.komapper.jdbc.Column
import org.komapper.jdbc.DatabaseConfig
import org.komapper.jdbc.JdbcDialect
import org.komapper.jdbc.PrimaryKey
import org.komapper.jdbc.Table
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
                config.dialect,
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
    private val dialect: JdbcDialect,
    private val metaData: DatabaseMetaData,
    private val catalog: String?,
    private val schemaPattern: String?,
    private val tableNamePattern: String?,
    private val tableTypes: List<String>
) {
    fun read(): List<Table> {
        return getTables().map { table ->
            val primaryKeys = getPrimaryKeys(table)
            table.copy(
                columns = getColumns(table, primaryKeys)
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

    private fun getColumns(table: Table, primaryKeys: List<PrimaryKey>): List<Column> {
        val columns: MutableList<Column> = mutableListOf()
        val rs = metaData.getColumns(
            table.catalog, table.schema, table.name, null
        )
        val pkMap = primaryKeys.associateBy { it.name }
        while (rs.next()) {
            val name = rs.getString("COLUMN_NAME")
            val primaryKey = pkMap[name]
            val column = Column(
                name = name,
                dataType = rs.getInt("DATA_TYPE"),
                typeName = rs.getString("TYPE_NAME"),
                length = rs.getInt("COLUMN_SIZE"),
                scale = rs.getInt("DECIMAL_DIGITS"),
                nullable = rs.getBoolean("NULLABLE"),
                isPrimaryKey = primaryKey != null,
                isAutoIncrement = primaryKey?.isAutoIncrement == true
            )
            columns.add(column)
        }
        return columns
    }

    private fun getPrimaryKeys(table: Table): List<PrimaryKey> {
        val keys = mutableListOf<String>()
        val rs = metaData.getPrimaryKeys(
            table.catalog, table.schema, table.name
        )
        while (rs.next()) {
            keys.add(rs.getString("COLUMN_NAME"))
        }
        return if (keys.size == 1) {
            val key = keys.first()
            val isAutoIncrement = isAutoIncrement(table, key)
            listOf(PrimaryKey(key, isAutoIncrement))
        } else {
            keys.map { PrimaryKey(it) }
        }
    }

    private fun isAutoIncrement(table: Table, key: String): Boolean {
        return metaData.connection.createStatement().use {
            val columnName = dialect.enquote(key)
            val tableName = table.getCanonicalTableName(dialect::enquote)
            it.executeQuery("select $columnName from $tableName where 1 = 0").use { rs ->
                rs.metaData.isAutoIncrement(1)
            }
        }
    }
}
