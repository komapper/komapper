package org.komapper.codegen;

import org.jetbrains.annotations.NotNull;

public interface Column {
  @NotNull
  String getName();

  int getDataType();

  @NotNull
  String getTypeName();

  int getLength();

  int getScale();

  boolean isNullable();

  boolean isPrimaryKey();

  boolean isAutoIncrement();
}
