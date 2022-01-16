package org.komapper.quarkus.jdbc;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class KomapperSettings {

  public final List<DataSourceDefinition> dataSourceDefinitions;

  public KomapperSettings(List<DataSourceDefinition> dataSourceDefinitions) {
    Objects.requireNonNull(dataSourceDefinitions);
    this.dataSourceDefinitions = Collections.unmodifiableList(dataSourceDefinitions);
  }

  @Override
  public String toString() {
    return "KomapperSettings{dataSourceDefinitions=" + dataSourceDefinitions + '}';
  }
}
