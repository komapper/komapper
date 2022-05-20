package org.komapper.codegen;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

class ClassNameResolverImpl implements ClassNameResolver {

  private final String prefix;
  private final String suffix;

  ClassNameResolverImpl(@NotNull String prefix, @NotNull String suffix) {
    this.prefix = Objects.requireNonNull(prefix);
    this.suffix = Objects.requireNonNull(suffix);
  }

  @Override
  public @NotNull String resolve(@NotNull Table table) {
    return prefix + StringUtil.snakeToUpperCamelCase(table.getName()) + suffix;
  }
}
