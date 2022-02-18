package org.komapper.codegen;

import static org.komapper.codegen.ClassNameConstants.STRING;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

class PropertyTypeResolverImpl implements PropertyTypeResolver {

  private final Map<Integer, String> dataTypeMap;

  PropertyTypeResolverImpl(Map<Integer, String> dataTypeMap) {
    this.dataTypeMap = new HashMap<>(Objects.requireNonNull(dataTypeMap));
  }

  @NotNull
  @Override
  public String resolve(@NotNull Table table, @NotNull Column column) {
    var candidate = dataTypeMap.get(column.getDataType());
    if (candidate != null) {
      return candidate;
    }
    return STRING;
  }
}
