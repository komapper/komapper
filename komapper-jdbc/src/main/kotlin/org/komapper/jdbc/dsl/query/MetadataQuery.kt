package org.komapper.jdbc.dsl.query

import org.komapper.core.ThreadSafe
import org.komapper.jdbc.JdbcDatabaseConfig
import org.komapper.jdbc.JdbcDialect
import java.sql.DatabaseMetaData

interface MetadataQuery {
    fun run(config: JdbcDatabaseConfig): List<Table>

    @ThreadSafe
    data class Column(
        val name: String,
        val dataType: Int,
        val typeName: String,
        val length: Int = 0,
        val scale: Int = 0,
        val nullable: Boolean = false,
        val isPrimaryKey: Boolean = false,
        val isAutoIncrement: Boolean = false,
    )

    @ThreadSafe
    data class PrimaryKey(
        val name: String,
        val isAutoIncrement: Boolean = false
    )

    @ThreadSafe
    data class Table(
        val name: String,
        val catalog: String? = null,
        val schema: String? = null,
        val columns: List<Column> = emptyList(),
    ) {
        fun getCanonicalTableName(enquote: (String) -> String): String {
            return listOfNotNull(catalog, schema, name)
                .filter { it.isNotBlank() }
                .joinToString(".", transform = enquote)
        }
    }
}

internal class MetadataQueryImpl(
    private val catalog: String?,
    private val schemaName: String?,
    private val tableNamePattern: String?,
    private val tableTypes: List<String>
) : MetadataQuery {

    override fun run(config: JdbcDatabaseConfig): List<MetadataQuery.Table> {
        return config.session.connection.use { con ->
            val reader = MetadataReader(
                config.dialect,
                con.metaData,
                // TODO: Remove this workaround in the future
                catalog ?: if (config.dialect.driver == "mariadb") { "" } else null,
                schemaName,
                tableNamePattern,
                tableTypes
            )
            reader.read()
        }
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
    fun read(): List<MetadataQuery.Table> {
        return getTables().map { table ->
            val primaryKeys = getPrimaryKeys(table)
            table.copy(
                columns = getColumns(table, primaryKeys)
            )
        }
    }

    private fun getTables(): List<MetadataQuery.Table> {
        val tables: MutableList<MetadataQuery.Table> = mutableListOf()
        metaData.getTables(
            catalog,
            schemaPattern,
            tableNamePattern,
            tableTypes.toTypedArray()
        ).use { rs ->
            while (rs.next()) {
                val table = MetadataQuery.Table(
                    name = rs.getString("TABLE_NAME"),
                    catalog = rs.getString("TABLE_CAT"),
                    schema = rs.getString("TABLE_SCHEM")
                )
                tables.add(table)
            }
        }
        return tables
    }

    private fun getColumns(table: MetadataQuery.Table, primaryKeys: List<MetadataQuery.PrimaryKey>): List<MetadataQuery.Column> {
        val columns: MutableList<MetadataQuery.Column> = mutableListOf()
        metaData.getColumns(
            table.catalog, table.schema, table.name, null
        ).use { rs ->
            val pkMap = primaryKeys.associateBy { it.name }
            while (rs.next()) {
                val name = rs.getString("COLUMN_NAME")
                val primaryKey = pkMap[name]
                val column = MetadataQuery.Column(
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
        }
        return columns
    }

    private fun getPrimaryKeys(table: MetadataQuery.Table): List<MetadataQuery.PrimaryKey> {
        val keys = mutableListOf<String>()
        metaData.getPrimaryKeys(
            table.catalog, table.schema, table.name
        ).use { rs ->
            while (rs.next()) {
                keys.add(rs.getString("COLUMN_NAME"))
            }
        }
        return if (keys.size == 1) {
            val key = keys.first()
            val isAutoIncrement = isAutoIncrement(table, key)
            listOf(MetadataQuery.PrimaryKey(key, isAutoIncrement))
        } else {
            keys.map { MetadataQuery.PrimaryKey(it) }
        }
    }

    private fun isAutoIncrement(table: MetadataQuery.Table, key: String): Boolean {
        return metaData.connection.createStatement().use {
            val columnName = dialect.enquote(key)
            val tableName = table.getCanonicalTableName(dialect::enquote)
            it.executeQuery("select $columnName from $tableName where 1 = 0").use { rs ->
                rs.metaData.isAutoIncrement(1)
            }
        }
    }
}
