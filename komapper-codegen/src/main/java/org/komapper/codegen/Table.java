package org.komapper.codegen;

import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Table {

  @NotNull
  String getName();

  @Nullable
  String getCatalog();

  @Nullable
  String getSchema();

  @NotNull
  List<Column> getColumns();

  @NotNull
  String getCanonicalTableName(@NotNull Function<String, String> enquote);
}
