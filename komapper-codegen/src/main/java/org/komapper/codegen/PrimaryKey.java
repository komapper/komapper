package org.komapper.codegen;

import java.util.Objects;

class PrimaryKey {
  private final String name;
  private final boolean isAutoIncrement;

  public PrimaryKey(String name, boolean isAutoIncrement) {
    this.name = Objects.requireNonNull(name);
    this.isAutoIncrement = isAutoIncrement;
  }

  public String getName() {
    return name;
  }

  public boolean isAutoIncrement() {
    return isAutoIncrement;
  }
}
