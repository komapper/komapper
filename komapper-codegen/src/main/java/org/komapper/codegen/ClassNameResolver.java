package org.komapper.codegen;

import org.jetbrains.annotations.NotNull;

public interface ClassNameResolver {

  @NotNull
  String resolve(@NotNull Table table);

  @NotNull
  static ClassNameResolver of(@NotNull String prefix, @NotNull String suffix, boolean singularize) {
    return new ClassNameResolverImpl(prefix, suffix, singularize);
  }
}
