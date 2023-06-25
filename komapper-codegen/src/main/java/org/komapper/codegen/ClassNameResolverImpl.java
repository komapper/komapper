package org.komapper.codegen;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

class ClassNameResolverImpl implements ClassNameResolver {

  private final String prefix;
  private final String suffix;
  private final boolean singularize;

  ClassNameResolverImpl(@NotNull String prefix, @NotNull String suffix, boolean singularize) {
    this.prefix = Objects.requireNonNull(prefix);
    this.suffix = Objects.requireNonNull(suffix);
    this.singularize = singularize;
  }

  @Override
  public @NotNull String resolve(@NotNull Table table) {
    return prefix + StringUtil.snakeToUpperCamelCase(table.getName(), singularize) + suffix;
  }
}
