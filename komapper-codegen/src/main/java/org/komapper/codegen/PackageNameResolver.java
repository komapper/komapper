package org.komapper.codegen;

import org.jetbrains.annotations.NotNull;

public interface PackageNameResolver {

  @NotNull
  String resolve(@NotNull String name);

  @NotNull
  static PackageNameResolver of() {
    return new PackageNameResolverImpl();
  }
}
