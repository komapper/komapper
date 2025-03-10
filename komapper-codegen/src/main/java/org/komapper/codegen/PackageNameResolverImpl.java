package org.komapper.codegen;

import org.jetbrains.annotations.NotNull;

class PackageNameResolverImpl implements PackageNameResolver {

  PackageNameResolverImpl() {}

  @Override
  public @NotNull String resolve(@NotNull String name) {
    final String[] parts = name.split("\\.");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append(".");
      }
      String part = parts[i];
      if (Constants.KEYWORDS.contains(part)) {
        sb.append("`").append(part).append("`");
      } else {
        sb.append(part);
      }
    }
    return sb.toString();
  }
}
