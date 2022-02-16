package org.komapper.codegen;

import org.jetbrains.annotations.NotNull;

class MutableColumn implements Column {
  String name;
  int dataType;
  String typeName;
  int length;
  int scale;
  boolean nullable;
  boolean isPrimaryKey;
  boolean isAutoIncrement;

  @NotNull
  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getDataType() {
    return dataType;
  }

  @NotNull
  @Override
  public String getTypeName() {
    return typeName;
  }

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public int getScale() {
    return scale;
  }

  @Override
  public boolean isNullable() {
    return nullable;
  }

  @Override
  public boolean isPrimaryKey() {
    return isPrimaryKey;
  }

  @Override
  public boolean isAutoIncrement() {
    return isAutoIncrement;
  }
}
