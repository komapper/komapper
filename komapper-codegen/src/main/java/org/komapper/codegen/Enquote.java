package org.komapper.codegen;

import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public interface Enquote extends Function<String, String> {

  @NotNull
  String apply(@NotNull String identifier);

  @NotNull
  static Enquote of(@NotNull String url) {
    Objects.requireNonNull(url);
    return new EnquoteImpl(url);
  }
}
