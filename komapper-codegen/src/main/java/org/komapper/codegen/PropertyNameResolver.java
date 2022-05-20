package org.komapper.codegen;

import org.jetbrains.annotations.NotNull;

public interface PropertyNameResolver {

  @NotNull
  String resolve(@NotNull Column column);

  @NotNull
  static PropertyNameResolver of() {
    return new PropertyNameResolverImpl();
  }
}
