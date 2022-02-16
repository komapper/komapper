package org.komapper.codegen;

import static org.komapper.codegen.ClassNameConstants.ARRAY;
import static org.komapper.codegen.ClassNameConstants.BIG_DECIMAL;
import static org.komapper.codegen.ClassNameConstants.BLOB;
import static org.komapper.codegen.ClassNameConstants.BOOLEAN;
import static org.komapper.codegen.ClassNameConstants.BYTE_ARRAY;
import static org.komapper.codegen.ClassNameConstants.CLOB;
import static org.komapper.codegen.ClassNameConstants.DOUBLE;
import static org.komapper.codegen.ClassNameConstants.INT;
import static org.komapper.codegen.ClassNameConstants.LOCAL_DATE;
import static org.komapper.codegen.ClassNameConstants.LOCAL_DATE_TIME;
import static org.komapper.codegen.ClassNameConstants.LOCAL_TIME;
import static org.komapper.codegen.ClassNameConstants.LONG;
import static org.komapper.codegen.ClassNameConstants.NCLOB;
import static org.komapper.codegen.ClassNameConstants.OFFSET_DATE_TIME;
import static org.komapper.codegen.ClassNameConstants.SQLXML;
import static org.komapper.codegen.ClassNameConstants.STRING;

import java.sql.JDBCType;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface PropertyTypeResolver {

  @NotNull
  String resolve(@NotNull Table table, @NotNull Column column);

  @NotNull
  static PropertyTypeResolver of() {
    return new PropertyTypeResolverImpl(createDataTypeMap());
  }

  @NotNull
  static PropertyTypeResolver of(@NotNull Map<Integer, String> dataTypeMap) {
    var newDataTypeMap = new HashMap<>(createDataTypeMap());
    newDataTypeMap.putAll(dataTypeMap);
    return new PropertyTypeResolverImpl(newDataTypeMap);
  }

  private static Map<Integer, String> createDataTypeMap() {
    Map<Integer, String> map = new HashMap<>(JDBCType.values().length);
    map.put(JDBCType.ARRAY.getVendorTypeNumber(), ARRAY);
    map.put(JDBCType.BIGINT.getVendorTypeNumber(), LONG);
    map.put(JDBCType.BINARY.getVendorTypeNumber(), BYTE_ARRAY);
    map.put(JDBCType.BIT.getVendorTypeNumber(), BOOLEAN);
    map.put(JDBCType.BLOB.getVendorTypeNumber(), BLOB);
    map.put(JDBCType.BOOLEAN.getVendorTypeNumber(), BOOLEAN);
    map.put(JDBCType.CHAR.getVendorTypeNumber(), STRING);
    map.put(JDBCType.CLOB.getVendorTypeNumber(), CLOB);
    map.put(JDBCType.DATE.getVendorTypeNumber(), LOCAL_DATE);
    map.put(JDBCType.DECIMAL.getVendorTypeNumber(), BIG_DECIMAL);
    map.put(JDBCType.DOUBLE.getVendorTypeNumber(), DOUBLE);
    map.put(JDBCType.FLOAT.getVendorTypeNumber(), DOUBLE);
    map.put(JDBCType.INTEGER.getVendorTypeNumber(), INT);
    map.put(JDBCType.LONGNVARCHAR.getVendorTypeNumber(), STRING);
    map.put(JDBCType.LONGVARBINARY.getVendorTypeNumber(), BYTE_ARRAY);
    map.put(JDBCType.LONGVARCHAR.getVendorTypeNumber(), STRING);
    map.put(JDBCType.NCHAR.getVendorTypeNumber(), STRING);
    map.put(JDBCType.NCLOB.getVendorTypeNumber(), NCLOB);
    map.put(JDBCType.NUMERIC.getVendorTypeNumber(), BIG_DECIMAL);
    map.put(JDBCType.NVARCHAR.getVendorTypeNumber(), STRING);
    map.put(JDBCType.REAL.getVendorTypeNumber(), DOUBLE);
    map.put(JDBCType.SMALLINT.getVendorTypeNumber(), INT);
    map.put(JDBCType.SQLXML.getVendorTypeNumber(), SQLXML);
    map.put(JDBCType.TIME.getVendorTypeNumber(), LOCAL_TIME);
    map.put(JDBCType.TIMESTAMP.getVendorTypeNumber(), LOCAL_DATE_TIME);
    map.put(JDBCType.TIMESTAMP_WITH_TIMEZONE.getVendorTypeNumber(), OFFSET_DATE_TIME);
    map.put(JDBCType.TINYINT.getVendorTypeNumber(), INT);
    map.put(JDBCType.VARCHAR.getVendorTypeNumber(), STRING);
    return map;
  }
}
