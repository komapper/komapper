package org.komapper.codegen;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class MetadataReader {
  private final Enquote enquote;
  private final DatabaseMetaData metaData;
  private final String catalog;
  private final String schemaPattern;
  private final String tableNamePattern;
  private final List<String> tableTypes;

  public MetadataReader(
      Enquote enquote,
      DatabaseMetaData metaData,
      String catalog,
      String schemaPattern,
      String tableNamePattern,
      List<String> tableTypes) {
    this.enquote = Objects.requireNonNull(enquote);
    this.metaData = Objects.requireNonNull(metaData);
    this.catalog = catalog;
    this.schemaPattern = schemaPattern;
    this.tableNamePattern = tableNamePattern;
    this.tableTypes = new ArrayList<>(Objects.requireNonNull(tableTypes));
  }

  @NotNull
  public List<Table> read() throws SQLException {
    List<MutableTable> tables = getTables();
    for (MutableTable table : tables) {
      var primaryKeys = getPrimaryKeys(table);
      table.columns = getColumns(table, primaryKeys);
    }
    return new ArrayList<>(tables);
  }

  private List<MutableTable> getTables() throws SQLException {
    var tables = new ArrayList<MutableTable>();
    try (var rs =
        metaData.getTables(
            catalog, schemaPattern, tableNamePattern, tableTypes.toArray(new String[] {}))) {
      while (rs.next()) {
        var table = new MutableTable();
        table.name = rs.getString("TABLE_NAME");
        table.catalog = rs.getString("TABLE_CAT");
        table.schema = rs.getString("TABLE_SCHEM");
        tables.add(table);
      }
    }
    return tables;
  }

  private List<Column> getColumns(Table table, List<PrimaryKey> primaryKeys) throws SQLException {
    var columns = new ArrayList<Column>();
    try (var rs =
        metaData.getColumns(table.getCatalog(), table.getSchema(), table.getName(), null)) {
      var pkMap = new HashMap<String, PrimaryKey>();
      for (PrimaryKey pk : primaryKeys) {
        pkMap.put(pk.getName(), pk);
      }
      while (rs.next()) {
        var name = rs.getString("COLUMN_NAME");
        var primaryKey = pkMap.get(name);
        var column = new MutableColumn();
        column.name = name;
        column.dataType = rs.getInt("DATA_TYPE");
        column.typeName = rs.getString("TYPE_NAME");
        column.length = rs.getInt("COLUMN_SIZE");
        column.scale = rs.getInt("DECIMAL_DIGITS");
        column.nullable = rs.getBoolean("NULLABLE");
        column.isPrimaryKey = primaryKey != null;
        column.isAutoIncrement = primaryKey != null && primaryKey.isAutoIncrement();
        columns.add(column);
      }
    }
    return columns;
  }

  private List<PrimaryKey> getPrimaryKeys(Table table) throws SQLException {
    var keys = new ArrayList<String>();
    try (var rs = metaData.getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getName())) {
      while (rs.next()) {
        keys.add(rs.getString("COLUMN_NAME"));
      }
    }
    if (keys.size() == 1) {
      var key = keys.get(0);
      var isAutoIncrement = isAutoIncrement(table, key);
      var primaryKey = new PrimaryKey(key, isAutoIncrement);
      return List.of(primaryKey);
    } else {
      return keys.stream().map(it -> new PrimaryKey(it, false)).collect(Collectors.toList());
    }
  }

  private boolean isAutoIncrement(Table table, String key) throws SQLException {
    // don't close this connection
    var con = metaData.getConnection();
    try (var statement = con.createStatement()) {
      var columnName = enquote.apply(key);
      var tableName = table.getCanonicalTableName(enquote);
      try (var rs =
          statement.executeQuery("select " + columnName + " from " + tableName + " where 1 = 0")) {
        return rs.getMetaData().isAutoIncrement(1);
      }
    }
  }
}
