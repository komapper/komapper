package org.komapper.codegen;

import org.jetbrains.annotations.NotNull;

class PropertyNameResolverImpl implements PropertyNameResolver {

  @Override
  public @NotNull String resolve(@NotNull Column column) {
    var candidate = StringUtil.snakeToLowerCamelCase(column.getName());
    if (Constants.KEYWORDS.contains(candidate)) {
      return "`" + candidate + "`";
    } else {
      return candidate;
    }
  }
}
