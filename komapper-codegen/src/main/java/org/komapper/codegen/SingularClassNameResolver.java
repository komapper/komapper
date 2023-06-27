package org.komapper.codegen;

import io.github.encryptorcode.pluralize.Pluralize;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

class SingularClassNameResolver implements ClassNameResolver {

  private final String prefix;
  private final String suffix;

  SingularClassNameResolver(@NotNull String prefix, @NotNull String suffix) {
    this.prefix = Objects.requireNonNull(prefix);
    this.suffix = Objects.requireNonNull(suffix);
  }

  @Override
  public @NotNull String resolve(@NotNull Table table) {
    var words = table.getName().split("_");
    var singularTableName =
        Arrays.stream(words).map(Pluralize::singular).collect(Collectors.joining("_"));
    return prefix + StringUtil.snakeToUpperCamelCase(singularTableName) + suffix;
  }
}
