package org.komapper.codegen;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

class MutableTable implements Table {
  String name;
  String catalog;
  String schema;
  List<Column> columns = Collections.emptyList();

  @NotNull
  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getCatalog() {
    return catalog;
  }

  @Override
  public String getSchema() {
    return schema;
  }

  @NotNull
  @Override
  public List<Column> getColumns() {
    return columns;
  }

  @NotNull
  @Override
  public String getCanonicalTableName(@NotNull Function<String, String> enquote) {
    return Stream.of(catalog, schema, name)
        .filter(it -> it != null && !it.isBlank())
        .map(enquote)
        .reduce((a, b) -> a + "." + b)
        .orElseThrow();
  }
}
